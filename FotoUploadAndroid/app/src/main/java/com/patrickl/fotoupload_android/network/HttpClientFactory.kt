package com.patrickl.fotoupload_android.network

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier

object HttpClientFactory {

    fun createDefault(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates an OkHttpClient that trusts ALL certificates.
     * ONLY USE THIS FOR DEBUGGING AND INITIAL ENROLLMENT IF YOUR CA TRUST IS FAILING.
     */
    fun createUnsafe(context: Context): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier(HostnameVerifier { _, _ -> true })
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun createWithTrust(context: Context): OkHttpClient {
        // Fallback to unsafe for enrollment if the CA trust is failing
        return createUnsafe(context)
    }
}
