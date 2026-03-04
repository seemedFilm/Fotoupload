package com.patrickl.fotoupload_android.security

import com.patrickl.fotoupload_android.R
import android.content.Context
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import java.security.SecureRandom
import okhttp3.OkHttpClient

object CertificateInstaller {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "upload_client_key"

    fun installCertificate(
        context: Context,
        clientCertPem: String
    ) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        val privateKey = keyStore.getKey(KEY_ALIAS, null)
            ?: throw IllegalStateException("PrivateKey not found")
        val clientCert = parseX509(clientCertPem)
        val caCert = loadCaCertificate(context)
        val chain = arrayOf(clientCert, caCert)
        keyStore.setKeyEntry(
            KEY_ALIAS,
            privateKey,
            null,
            chain
        )
    }

    private fun parseX509(pem: String): X509Certificate {
        val cleaned = pem
            .replace("-----BEGIN CERTIFICATE-----", "")
            .replace("-----END CERTIFICATE-----", "")
            .replace("\\s".toRegex(), "")
        val decoded = Base64.getDecoder().decode(cleaned)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(decoded.inputStream()) as X509Certificate
    }
    private fun loadCaCertificate(context: Context): X509Certificate {
        val input = context.resources.openRawResource(R.raw.ca_cert)
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(input) as X509Certificate
    }
}