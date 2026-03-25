package com.patrickl.fotoupload_android.network

import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ConnectionResolver(
    private val client: OkHttpClient
) {

    suspend fun resolve(profile: ConnectionProfile): String = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        val candidates = mutableListOf<String>()

        if (profile.intUrl.isNotBlank()) {
            val protocol = if (profile.useSsl) "https" else "http"
            candidates.add("$protocol://${profile.intUrl}:${profile.port}")
        }
        if (profile.extUrl.isNotBlank()) {
            val protocol = if (profile.useSsl) "https" else "http"
            candidates.add("$protocol://${profile.extUrl}:${profile.port}")
        }

        for (baseUrl in candidates) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/login.php")
                    .get()
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 401) {
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
        throw Exception("Verbindung fehlgeschlagen:\n$errorDetail")
    }
}
