package com.patrickl.fotoupload_android.data

import android.content.Context
import com.patrickl.fotoupload_android.security.KeyStoreManager
import com.patrickl.fotoupload_android.security.CsrGenerator
import com.patrickl.fotoupload_android.security.CertificateInstaller
import com.patrickl.fotoupload_android.network.EnrollmentApi

class EnrollmentRepository(
    private val context: Context,
    private val api: EnrollmentApi
) {

    suspend fun enroll(token: String) {

        // 1. KeyPair sicherstellen
        KeyStoreManager.generateKeyPairIfNeeded()

        // 2. CSR erzeugen
        val deviceName = DeviceInfo.getDeviceName(context)
        val csr = CsrGenerator.generateCsr(deviceName)

        // 3. Server aufrufen
        val response = api.enroll(token, csr)

        // 4. Zertifikat installieren
        CertificateInstaller.installCertificate(
            context,
            response.certificate
        )
    }
}