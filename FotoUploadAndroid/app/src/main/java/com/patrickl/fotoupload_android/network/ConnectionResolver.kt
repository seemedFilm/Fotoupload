package com.patrickl.fotoupload_android.network

import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private const val TAG = "ConnectionResolver.kt"

class ConnectionResolver {

    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun resolve(profile: ConnectionProfile): String = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        val candidates = mutableListOf<String>()

        val intProtocol = if (profile.useSsl) "https" else "http"
        if (profile.intUrl.isNotBlank()) {
            candidates.add("$intProtocol://${profile.intUrl}:${profile.port}")
        }
        
        if (profile.extUrl.isNotBlank()) {
            val extProtocol = if (profile.useSsl) "https" else "http"
            candidates.add("$extProtocol://${profile.extUrl}:${profile.port}")
        }

        for (baseUrl in candidates) {
            try {
                // We use an empty POST request to probe the server, as login.php only accepts POST.
                // An empty POST will likely return 400 (Invalid JSON) or 401, but not 405.
                val emptyBody = "".toRequestBody(null)
                val request = Request.Builder()
                    .url("$baseUrl/login.php")
                    .post(emptyBody)
                    .build()

                probeClient.newCall(request).execute().use { response ->
                    // 200, 400, 401 all indicate the server is responsive and the endpoint exists.
                    if (response.isSuccessful || response.code == 400 || response.code == 401) {
                        return@withContext baseUrl
                    } else {
                        errors.add("$baseUrl: HTTP ${response.code}")
                    }
                }
            } catch (e: IOException) {
                errors.add("$baseUrl: ${e.message}")
            } catch (e: Exception) {
                errors.add("$baseUrl: ${e.javaClass.simpleName}")
            }
        }
        
        val errorDetail = if (errors.isEmpty()) "Keine URLs konfiguriert" else errors.joinToString("\n")
        Log.e(TAG, errorDetail)
        throw Exception("Verbindung fehlgeschlagen:\n$errorDetail")
    }
}
