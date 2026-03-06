package com.patrickl.fotoupload_android.data

import android.content.Context
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.network.HttpClientFactory
import com.patrickl.fotoupload_android.security.CertificateInstaller
import com.patrickl.fotoupload_android.security.CsrGenerator
import com.patrickl.fotoupload_android.security.DeviceInfo
import com.patrickl.fotoupload_android.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnrollmentRepository(
    private val context: Context
) {
    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {

        KeyStoreManager.generateKeyPairIfNeeded()
        val client = HttpClientFactory.createDefault()
        val urls = listOfNotNull(
            profile.intUrl.takeIf { it.isNotBlank() }?.let {
                "http://$it:${profile.port}"
            },
            profile.extUrl.takeIf { it.isNotBlank() }?.let {
                "https://$it:${profile.port}"
            }
        )
        var lastError: Exception? = null
        for (baseUrl in urls) {
            try {
                val api = EnrollmentApi(client, baseUrl)
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
                return@withContext
            } catch (e: Exception) {
                lastError = e
            }
        }
        throw lastError ?: Exception("Keine Verbindung möglich")
    }
}