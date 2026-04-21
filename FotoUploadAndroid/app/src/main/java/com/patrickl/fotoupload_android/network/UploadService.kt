package com.patrickl.fotoupload_android.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

private const val TAG = "UploadService.kt"

object UploadService {

    private fun getBaseUrl(profile: ConnectionProfile): String {
        val host = profile.extUrl.ifBlank { profile.intUrl }
        val protocol = if (profile.useSsl) "https" else "http"
        val defaultPort = if (profile.useSsl) 443 else 80
        val portPart = if (profile.port == defaultPort || profile.port == 0) "" else ":${profile.port}"
        return "$protocol://$host$portPart"
    }

    suspend fun uploadMultipleImages(
        context: Context,
        uris: List<Uri>,
        profile: ConnectionProfile
    ): UploadSummary = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(profile)
        val uploadUrl = "${baseUrl.trimEnd('/')}/upload.php"
        val alias = "client_cert_${profile.id}"
        
        Log.d(TAG, "[uploadMultipleImages]: Starting upload of ${uris.size} files to $uploadUrl using alias: $alias")

        var certPem: String? = null
        try {
            certPem = KeyStoreManager.getClientCertPem(alias)
            Log.d(TAG, "[uploadMultipleImages]: Certificate retrieved successfully")
        } catch (e: Exception) {
            Log.w(TAG, "[uploadMultipleImages]: Could not retrieve certificate: ${e.message}")
        }

        // 1. Create client with mTLS handshake support
        val client = MtlsHttpClientFactory(context).create(alias)

        try {
            val multipartBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

            uris.forEach { uri ->
                val fileName = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@forEach
                val bytes = inputStream.use { it.readBytes() }
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                multipartBuilder.addFormDataPart(
                    "files[]",
                    fileName,
                    requestBody
                )
            }
            
            val requestBody = multipartBuilder.build()
            val requestBuilder = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)

            // Optional: If your server specifically checks for this header even with mTLS
            if (certPem != null) {
                requestBuilder.addHeader("X-Client-Cert", certPem)
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                Log.d(TAG, "[uploadMultipleImages]: Server responded with HTTP ${response.code}: $body")

                if (!response.isSuccessful || body == null) {
                    return@use UploadSummary(
                        total = uris.size,
                        success = 0,
                        failed = uris.size,
                        errorMessage = "Server error (${response.code}): $body"
                    )
                }
                
                var successCount = 0
                var failCount = 0
                // Simple parsing for JSON response
                if (body.contains("\"status\":\"ok\"") || body.contains("\"status\":\"completed\"")) {
                   successCount = Regex("\"status\"\\s*:\\s*\"ok\"").findAll(body).count()
                   val errorCount = Regex("\"status\"\\s*:\\s*\"error\"").findAll(body).count()
                   val duplicateCount = Regex("\"status\"\\s*:\\s*\"duplicate\"").findAll(body).count()
                   failCount = errorCount + duplicateCount
                } else if (response.code == 200) {
                    // Fallback if the body doesn't match expected JSON but HTTP was 200
                    successCount = uris.size
                }

                UploadSummary(
                    total = uris.size,
                    success = successCount,
                    failed = failCount,
                    errorMessage = if (failCount > 0) "Some files failed to upload" else null
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception during upload process", e)
            UploadSummary(
                total = uris.size,
                success = 0,
                failed = uris.size,
                errorMessage = "Network error: ${e.message}"
            )
        }
    }

    suspend fun fetchUploadedImages(
        context: Context,
        profile: ConnectionProfile
    ): List<String> = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl(profile)
        val listUrl = "${baseUrl.trimEnd('/')}/list.php"
        val alias = "client_cert_${profile.id}"

        var certPem: String? = null
        try {
            certPem = KeyStoreManager.getClientCertPem(alias)
        } catch (e: Exception) {
            Log.w(TAG, "[fetchUploadedImages]: Could not retrieve certificate: ${e.message}")
        }

        val client = MtlsHttpClientFactory(context).create(alias)
        
        Log.d(TAG, "[fetchUploadedImages]: connecting to: $listUrl")
        try {
            val requestBuilder = Request.Builder()
                .url(listUrl)
                .get()
            
            if (certPem != null) {
                requestBuilder.addHeader("X-Client-Cert", certPem)
            }

            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@use emptyList()
                if (response.isSuccessful) {
                    return@use try {
                        Json.decodeFromString<List<String>>(body)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing JSON list: $body")
                        emptyList()
                    }
                } else {
                    Log.e(TAG, "[fetchUploadedImages] failed: ${response.code} $body")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching images", e)
            emptyList()
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
