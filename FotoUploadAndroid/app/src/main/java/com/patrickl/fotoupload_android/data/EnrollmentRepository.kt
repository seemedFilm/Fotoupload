package com.patrickl.fotoupload_android.data

import android.content.Context
import android.util.Log
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

private const val TAG = "EnrollmentRepository.kt"

class EnrollmentRepository(
    private val context: Context
) {

    suspend fun enroll(profile: ConnectionProfile) = withContext(Dispatchers.IO) {

        val alias = "client_cert_${profile.id}"
        val client = HttpClientFactory.createWithTrust(context)
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
        Log.d(TAG, "api.enroll: ${response.toString()}")
        KeyStoreManager.generateKeyPairIfNeeded(alias)
        CertificateInstaller.installCertificate(
            context = context,
            clientCertPem = response.certificate
        )
    }
}
