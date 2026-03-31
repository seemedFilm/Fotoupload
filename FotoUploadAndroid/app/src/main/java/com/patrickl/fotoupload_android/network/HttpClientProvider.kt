package com.patrickl.fotoupload_android.network

import android.util.Log
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
private const val TAG = "HttpClientProvider.kt"
object HttpClientProvider {

    fun getClient(baseUrl: String): OkHttpClient {
        Log.d(TAG, "[getClient]: Connection is Local? ${isLocal(baseUrl)}")
        return if (isLocal(baseUrl)) {
            createLocalClient()
        } else {
            createRemoteClient()
        }
    }

    private fun isLocal(url: String): Boolean {
        return url.contains(".zuhause") || url.contains("192.168.")
    }

    private fun createRemoteClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private fun createLocalClient(): OkHttpClient {
        val (sslContext, trustManager) = buildMtlsContext()

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .build()
    }

    private fun buildMtlsContext(): Pair<SSLContext, X509TrustManager> {
        // dein bestehender mTLS Code hier rein
        TODO("reuse MtlsHttpClientFactory logic")
    }
}