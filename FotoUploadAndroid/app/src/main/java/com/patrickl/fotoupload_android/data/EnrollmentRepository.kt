package com.patrickl.fotoupload_android.data

import android.content.Context
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.ConnectionResolver
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

        val alias = "client_cert_${profile.id}"
        val client = HttpClientFactory.createDefault()
        val resolver = ConnectionResolver(client)
        val baseUrl = resolver.resolve(profile)
        val api = EnrollmentApi(client, baseUrl)
        val token = api.login(
            username = profile.username,
            password = profile.password
        )
        val deviceName = DeviceInfo.getDeviceName(context)
        val csr = CsrGenerator.generateCsr(deviceName, alias)
        val response = api.enroll(
            token = token,
            csr = csr
        )

        KeyStoreManager.generateKeyPairIfNeeded(alias)
        CertificateInstaller.installCertificate(
            context = context,
            clientCertPem = response.certificate
        )
    }
}