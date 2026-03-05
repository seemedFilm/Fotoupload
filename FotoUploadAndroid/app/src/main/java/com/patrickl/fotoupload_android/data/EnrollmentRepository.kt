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
    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {

        KeyStoreManager.generateKeyPairIfNeeded()

        val token = api.login(
            username = profile.username,
            password = profile.password
        )
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