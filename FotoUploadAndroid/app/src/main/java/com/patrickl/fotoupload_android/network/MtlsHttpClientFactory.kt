package com.patrickl.fotoupload_android.network

import android.content.Context
import com.patrickl.fotoupload_android.security.KeyStoreManager
import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class MtlsHttpClientFactory(
    private val context: Context
) {

    fun create(): OkHttpClient {

        val keyStore: KeyStore = KeyStoreManager.getKeyStore()

        val keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        keyManagerFactory.init(keyStore, null)

        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            keyManagerFactory.keyManagers,
            trustManagerFactory.trustManagers,
            null
        )

        return OkHttpClient.Builder()
            .sslSocketFactory(
                sslContext.socketFactory,
                trustManagerFactory.trustManagers[0] as javax.net.ssl.X509TrustManager
            )
            .build()
    }
}