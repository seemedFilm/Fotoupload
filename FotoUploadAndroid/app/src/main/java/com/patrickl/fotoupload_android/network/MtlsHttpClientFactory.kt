package com.patrickl.fotoupload_android.network

import android.content.Context
import com.patrickl.fotoupload_android.R
import com.patrickl.fotoupload_android.security.KeyStoreManager
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MtlsHttpClientFactory(
    private val context: Context
) {

    fun create(): OkHttpClient {
        // 1. Client Keys/Certs from KeyStore
        val keyStore: KeyStore = KeyStoreManager.getKeyStore()
        val keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        keyManagerFactory.init(keyStore, null)

        // 2. Trust our own CA for the Server Certificate
        val trustStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
        }
        
        try {
            val caInput = context.resources.openRawResource(R.raw.ca_cert)
            val cf = CertificateFactory.getInstance("X.509")
            val caCert = cf.generateCertificate(caInput) as X509Certificate
            trustStore.setCertificateEntry("ca", caCert)
            caInput.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(trustStore)

        // 3. Initialize SSL Context
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null
        )

        return OkHttpClient.Builder()
            .sslSocketFactory(
                sslContext.socketFactory,
                trustManagerFactory.trustManagers[0] as X509TrustManager
            )
            .build()
    }
}
