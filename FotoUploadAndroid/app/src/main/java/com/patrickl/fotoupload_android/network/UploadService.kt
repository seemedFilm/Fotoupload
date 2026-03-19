package com.patrickl.fotoupload_android.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
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
            uris.forEachIndexed { index, uri ->
                val stream = context.contentResolver.openInputStream(uri)
                    ?: return@forEachIndexed
                val requestBody = object : RequestBody() {
                    override fun contentType(): MediaType {
                        return "image/*".toMediaType()
                    }
                    override fun writeTo(sink: okio.BufferedSink) {
                        stream.use { input ->
                            sink.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }

                multipartBuilder.addFormDataPart(
                    "files[]",
                    "image_$index.jpg",
                    requestBody
                )
            }

            val requestBody = multipartBuilder.build()
            val request = Request.Builder()
                .url("$serverUrl/upload.php")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->

                val body = response.body?.string()

                println("=========== SERVER RESPONSE ===========")
                println(body)
                println("HTTP CODE: ${response.code}")
                println("=======================================")

                if (!response.isSuccessful || body == null) {

                    return@use UploadSummary(
                        total = uris.size,
                        success = 0,
                        failed = uris.size,
                        errorMessage = "HTTP ${response.code}"
                    )
                }

                var successCount = 0
                var failCount = 0

                Regex("\"status\"\\s*:\\s*\"(ok|error)\"")
                    .findAll(body)
                    .forEach {

                        if (it.groupValues[1] == "ok")
                            successCount++
                        else
                            failCount++
                    }

                UploadSummary(
                    total = uris.size,
                    success = successCount,
                    failed = failCount
                )
            }

        } catch (e: IOException) {

            UploadSummary(
                total = uris.size,
                success = 0,
                failed = uris.size,
                errorMessage = e.message
            )
        }
    }
}