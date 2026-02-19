package com.patrickl.fotoupload_android.network

import com.patrickl.fotoupload_android.config.ApiConfig
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
    ): String = withContext(Dispatchers.IO) {

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
                "files[]",    // Muss zu deiner PHP passen
                tempFile.name,
                requestFile
            )
        }

        val requestBody = multipartBuilder.build()

        val request = Request.Builder()
//            .url("http://$serverUrl/upload.php")
            .url("${ApiConfig.BASE_URL}/upload.php")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            response.body?.string() ?: "Keine Serverantwort"
        }
    }
}
