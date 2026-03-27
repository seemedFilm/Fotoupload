package com.patrickl.fotoupload_android.data

import android.content.Context
import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.ConnectionResolver
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.network.HttpClientProvider
import com.patrickl.fotoupload_android.security.CertificateInstaller
import com.patrickl.fotoupload_android.security.CsrGenerator
import com.patrickl.fotoupload_android.security.DeviceInfo
import com.patrickl.fotoupload_android.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "EnrollmentRepository.kt"

class EnrollmentRepository(
    private val context: Context
) {

    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {
        Log.d(TAG, "enroll: Starting enrollment for profile ${profile.name}")
        val alias = "client_cert_${profile.id}"
        
        try {

            val resolver = ConnectionResolver()
            val baseUrl = resolver.resolve(profile)
            val client = HttpClientProvider.getClient(baseUrl)

            Log.d(TAG, "enroll: Resolved base URL to $baseUrl")

            val api = EnrollmentApi(client, baseUrl)
            val token = api.login(
                username = profile.username,
                password = profile.password
            )
            Log.d(TAG, "enroll: Login successful")

            // IMPORTANT: KeyPair MUST be generated before generating CSR
            Log.d(TAG, "enroll: Generating key pair for alias: $alias")
            KeyStoreManager.generateKeyPairIfNeeded(alias)

            val deviceName = DeviceInfo.getDeviceName(context)
            Log.d(TAG, "enroll: Generating CSR for device: $deviceName")
            val csr = CsrGenerator.generateCsr(deviceName, alias)

            val response = api.enroll(
                token = token,
                csr = csr
            )
            Log.d(TAG, "enroll: CSR enrolled successfully. Server response: $response")

            CertificateInstaller.installCertificate(
                context = context,
                alias = alias,
                clientCertPem = response.certificate
            )
            Log.d(TAG, "enroll: Certificate installed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "enroll: Enrollment failed", e)
            throw e
        }
    }
}
