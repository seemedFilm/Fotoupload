package com.patrickl.fotoupload_android.data

import android.content.Context
import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.security.CertificateInstaller
import com.patrickl.fotoupload_android.security.CsrGenerator
import com.patrickl.fotoupload_android.security.DeviceInfo
import com.patrickl.fotoupload_android.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnrollmentRepository(
    private val context: Context,
    private val api: EnrollmentApi
) {
//    suspend fun enroll(profile: ConnectionProfile) {
//        try {
//            Log.d("CSR_DEBUG", "enrollRepo")
//            // 2️⃣ KeyPair sicherstellen
//            KeyStoreManager.generateKeyPairIfNeeded()
//            Log.d("CSR_DEBUG", "KeyStoreManager")
//
//            // 1️⃣ Login durchführen → Token holen
//            val token = api.login(
//                username = profile.username,
//                password = profile.password
//            )
//            Log.d("CSR_DEBUG", token)
//
//
//        // 3️⃣ CSR erzeugen
//        val deviceName = DeviceInfo.getDeviceName(context)
//        val csr = CsrGenerator.generateCsr(deviceName)
//
//        Log.d("CSR_DEBUG", csr)
//
//        // 4️⃣ Enrollment durchführen
//        val response = api.enroll(
//            token = token,
//            csr = csr
//        )
//
//        // 5️⃣ Zertifikat installieren
//        CertificateInstaller.installCertificate(
//            context = context,
//            clientCertPem = response.certificate
//        )
//        } catch (e: Exception) {
//
//            Log.e("CSR_DEBUG", "Login failed", e)
//
//        }
//
//    }
    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {

        Log.d("CSR_DEBUG", "enrollRepo")

        KeyStoreManager.generateKeyPairIfNeeded()
        Log.d("CSR_DEBUG", "KeyStoreManager")

        val token = api.login(
            username = profile.username,
            password = profile.password
        )

        Log.d("CSR_DEBUG", "TOKEN: $token")

        val deviceName = DeviceInfo.getDeviceName(context)
        val csr = CsrGenerator.generateCsr(deviceName)

        val response = api.enroll(
            token = token,
            csr = csr
        )

        CertificateInstaller.installCertificate(
            context = context,
            clientCertPem = response.certificate
        )
    }
}