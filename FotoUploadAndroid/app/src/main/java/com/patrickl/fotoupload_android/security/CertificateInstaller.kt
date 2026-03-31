package com.patrickl.fotoupload_android.security

import com.patrickl.fotoupload_android.R
import android.content.Context
import android.util.Log
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

object CertificateInstaller {

    private const val TAG = "CertificateInstaller"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun installCertificate(
        context: Context,
        alias: String,
        clientCertPem: String
    ) {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

            val privateKey = keyStore.getKey(alias, null)
            if (privateKey == null) {
                Log.e(TAG, "PrivateKey for alias '$alias' not found in keystore")
                throw IllegalStateException("PrivateKey not found in keystore")
            }

            val clientCert = parseX509(clientCertPem)
            val caCert = loadCaCertificate(context)
            val chain = arrayOf(clientCert, caCert)

            keyStore.setKeyEntry(
                alias,
                privateKey,
                null,
                chain
            )
            Log.d(TAG, "Successfully installed certificate for alias: $alias")
        } catch (e: Exception) {
            Log.e(TAG, "Error installing certificate for alias '$alias': ${e.message}", e)
            throw e
        }
    }

    private fun parseX509(pem: String): X509Certificate {
        try {
            val cleaned = pem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\\s".toRegex(), "")
            val decoded = Base64.getDecoder().decode(cleaned)
            val factory = CertificateFactory.getInstance("X.509")
            return factory.generateCertificate(decoded.inputStream()) as X509Certificate
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse X509 certificate: ${e.message}")
            throw e
        }
    }

    private fun loadCaCertificate(context: Context): X509Certificate {
        try {
            val input = context.resources.openRawResource(R.raw.ca_cert)
            val factory = CertificateFactory.getInstance("X.509")
            val cert = factory.generateCertificate(input) as X509Certificate
            input.close()
            return cert
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load CA certificate from resources: ${e.message}")
            throw e
        }
    }
}
