package com.patrickl.fotoupload_android.data

import android.content.Context
import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.enroll.CsrHelper
import com.patrickl.fotoupload_android.network.ConnectionResolver
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.network.HttpClientProvider
import com.patrickl.fotoupload_android.security.CertificateInstaller
import com.patrickl.fotoupload_android.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "EnrollmentRepository.kt"

class EnrollmentRepository(
    private val context: Context
) {

    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {
        Log.d(TAG, "[enroll]: Starting enrollment for profile ${profile.name}")

        val alias = "client_cert_${profile.id}"

        try {
            val resolver = ConnectionResolver()
            val baseUrl = resolver.resolve(profile)
            val client = HttpClientProvider.getClient(baseUrl)
            val api = EnrollmentApi(client, baseUrl)

            // 🔐 1. Login → Token holen
            val loginResponse = api.login(
                username = profile.username,
                password = profile.password
            )
            Log.d(TAG, "[enroll]: Login successful")

            // 🔐 2. KeyPair sicherstellen
            KeyStoreManager.generateKeyPairIfNeeded(alias)

            // 📱 Device ID
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )

            // 📄 3. CSR erzeugen
            val csr = CsrHelper.createCsr(alias, deviceId)
            Log.d(TAG, "[enroll]: CSR created")

            // 🌐 4. Enrollment (NEU!)
            val response = api.enroll(
                token = loginResponse.token,
                csr = csr
            )

            Log.d(TAG, "[enroll]: Enrolled successfully.")

            // 🔐 5. Zertifikat installieren
            CertificateInstaller.installCertificate(
                context = context,
                alias = alias,
                clientCertPem = response.certificate
            )

        } catch (e: Exception) {
            Log.e(TAG, "[enroll]: Enrollment failed", e)
            throw e
        }
    }
}