package com.patrickl.fotoupload_android.network
import com.patrickl.fotoupload_android.network.UploadSummary
//import com.patrickl.fotoupload_android.config.ApiConfig
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object UploadService {

    private val client = OkHttpClient()

    suspend fun uploadMultipleImages(
        context: Context,
        uris: List<Uri>,
        serverUrl: String
    ): UploadSummary = withContext(Dispatchers.IO) {

        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        uris.forEachIndexed { index, uri ->

            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@forEachIndexed

            val tempFile = File.createTempFile("upload_$index", ".jpg", context.cacheDir)

            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }

            val requestFile = tempFile
                .asRequestBody("image/*".toMediaTypeOrNull())

            multipartBuilder.addFormDataPart(
                "files[]",
                tempFile.name,
                requestFile
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
                    failed = uris.size
                )
            }

            var successCount = 0
            var failCount = 0

            Regex("\"status\"\\s*:\\s*\"(ok|error)\"")
                .findAll(body)
                .forEach {
                    if (it.groupValues[1] == "ok") successCount++
                    else failCount++
                }

            UploadSummary(
                total = uris.size,
                success = successCount,
                failed = failCount
            )
        }
    }
}
