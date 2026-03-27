package com.patrickl.fotoupload_android.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private const val TAG = "UploadService.kt"

object UploadService {

    suspend fun uploadMultipleImages(
        context: Context,
        uris: List<Uri>,
        baseUrl: String,
        profile: ConnectionProfile
    ): UploadSummary = withContext(Dispatchers.IO) {
        val uploadUrl = "${baseUrl.trimEnd('/')}/upload.php"
        Log.d(TAG, "[uploadMultipleImages]: Starting upload of ${uris.size} files to $uploadUrl")

        val alias = "client_cert_${profile.id}"
        val factory = MtlsHttpClientFactory(context)
        val client = HttpClientProvider.getClient(uploadUrl)

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
            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()

            Log.d(TAG, "Executing POST request to: $uploadUrl")
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()

                Log.d(TAG, "Server responded with HTTP ${response.code}")
                if (response.code != 200) {
                    Log.w(TAG, "Non-200 response received. Body snippet: ${body?.take(200)}")
                }

                if (!response.isSuccessful || body == null) {
                    Log.e(TAG, "Request failed. Code: ${response.code}, Body: $body")
                    return@use UploadSummary(
                        total = uris.size,
                        success = 0,
                        failed = uris.size,
                        errorMessage = "Server error (${response.code}): $body"
                    )
                }

                var successCount = 0
                var failCount = 0

                if (body.contains("\"status\":\"ok\"") || body.contains("\"status\":\"completed\"")) {
                   successCount = Regex("\"status\"\\s*:\\s*\"ok\"").findAll(body).count()
                   val errorCount = Regex("\"status\"\\s*:\\s*\"error\"").findAll(body).count()
                   val duplicateCount = Regex("\"status\"\\s*:\\s*\"duplicate\"").findAll(body).count()
                   failCount = errorCount + duplicateCount
                } else if (body.contains("\"error\"")) {
                    Log.w(TAG, "Server returned an error JSON: $body")
                    return@use UploadSummary(
                        total = uris.size,
                        success = 0,
                        failed = uris.size,
                        errorMessage = body
                    )
                }

                val summary = UploadSummary(
                    total = uris.size,
                    success = successCount,
                    failed = failCount,
                    errorMessage = if (failCount > 0) "Some files failed to upload" else null
                )
                Log.d(TAG, "Upload summary: $summary")
                summary
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

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        Log.d(TAG, "[getFileName]: getFileName: $uri")
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
