package com.patrickl.fotoupload_android.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    fun createDefault(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}