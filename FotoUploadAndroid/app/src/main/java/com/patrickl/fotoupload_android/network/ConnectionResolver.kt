package com.patrickl.fotoupload_android.network

import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ConnectionResolver(
    private val client: OkHttpClient
) {

    suspend fun resolve(profile: ConnectionProfile): String = withContext(Dispatchers.IO) {
        val candidates = listOfNotNull(
            profile.intUrl.takeIf { it.isNotBlank() }?.let {
                "http://$it:${profile.port}"
            },
            profile.extUrl.takeIf { it.isNotBlank() }?.let {
                "https://$it:${profile.port}"
            }
        )
        for (baseUrl in candidates) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/login.php")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 401) {
                        return@withContext baseUrl
                    }
                }
            } catch (_: Exception) {
            }
        }
        throw Exception("Keine Verbindung möglich")
    }
}