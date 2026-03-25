package com.patrickl.fotoupload_android.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object UploadService {

    suspend fun uploadMultipleImages(
        context: Context,
        uris: List<Uri>,
        serverUrl: String
    ): UploadSummary = withContext(Dispatchers.IO) {

        val client = MtlsHttpClientFactory(context).create()
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
                .url("${serverUrl.trimEnd('/')}/upload.php")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()

                println("=========== SERVER RESPONSE ===========")
                println("HTTP CODE: ${response.code}")
                println("BODY: $body")
                println("=======================================")

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

                if (body.contains("\"status\":\"ok\"") || body.contains("\"status\":\"completed\"")) {
                   successCount = Regex("\"status\"\\s*:\\s*\"ok\"").findAll(body).count()
                   val errorCount = Regex("\"status\"\\s*:\\s*\"error\"").findAll(body).count()
                   val duplicateCount = Regex("\"status\"\\s*:\\s*\"duplicate\"").findAll(body).count()
                   failCount = errorCount + duplicateCount
                } else if (body.contains("\"error\"")) {
                    return@use UploadSummary(
                        total = uris.size,
                        success = 0,
                        failed = uris.size,
                        errorMessage = body
                    )
                }

                UploadSummary(
                    total = uris.size,
                    success = successCount,
                    failed = failCount,
                    errorMessage = if (failCount > 0) "Some files failed to upload" else null
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
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
