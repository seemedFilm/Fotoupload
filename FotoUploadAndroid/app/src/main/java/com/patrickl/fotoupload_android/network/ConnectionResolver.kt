package com.patrickl.fotoupload_android.network

import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private const val TAG = "ConnectionResolver.kt"

class ConnectionResolver {

    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS) // Short timeout for probing
        .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun resolve(profile: ConnectionProfile): String = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        val candidates = mutableListOf<String>()

        val protocol = if (profile.useSsl) "https" else "http"
        val defaultPort = if (profile.useSsl) 443 else 80
        val portPart = if (profile.port == defaultPort || profile.port == 0) "" else ":${profile.port}"

        // Prioritize External URL as requested
        if (profile.extUrl.isNotBlank()) {
            candidates.add("$protocol://${profile.extUrl}$portPart")
        }
        
        // Internal URL acts as fallback
        //if (profile.intUrl.isNotBlank()) {
        //    candidates.add("$protocol://${profile.intUrl}$portPart")
        //}

        for (baseUrl in candidates) {
            try {
                Log.d(TAG, "[resolve]: calling request.Bilder)")
                // We use a simple GET request to check if the server is alive
                val request = Request.Builder()
                    .url("$baseUrl/login.php")
                    .get()
                    .build()

                probeClient.newCall(request).execute().use { response ->
                    // 200, 400, 401, 405, and even 500 prove the host is REACHABLE.
                    // If the server crashes (500), it's still the correct host.
                    if (response.code < 600) {
                        Log.d(TAG, "Host reachable: $baseUrl (Status: ${response.code})")
                        return@withContext baseUrl
                    }
                }
            } catch (e: IOException) {
                errors.add("$baseUrl: ${e.message}")
            } catch (e: Exception) {
                errors.add("$baseUrl: ${e.javaClass.simpleName}")
            }
        }
        
        val errorDetail = if (errors.isEmpty()) "Keine URLs konfiguriert" else errors.joinToString("\n")
        Log.e(TAG, "All candidates failed:\n$errorDetail")
        throw Exception("Verbindung fehlgeschlagen:\n$errorDetail")
    }
}
