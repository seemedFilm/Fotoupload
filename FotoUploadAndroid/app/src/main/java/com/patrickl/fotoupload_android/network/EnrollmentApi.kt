package com.patrickl.fotoupload_android.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class EnrollmentResponse(
    val certificate: String
)

class EnrollmentApi(
    private val client: OkHttpClient,
    private val intUrl: String
) {

    suspend fun login(
        username: String,
        password: String
    ): String {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }
        val body = json.toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$intUrl/login.php")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Login failed: ${response.code}")
            }
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response")
            val jsonResponse = JSONObject(responseBody)
            return jsonResponse.getString("token")
        }
    }

    suspend fun enroll(
        token: String,
        csr: String
    ): EnrollmentResponse {
        val json = JSONObject().apply {
            put("token", token)
            put("csr", csr)
        }
        val body = json.toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$intUrl/enroll.php")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string()
                ?: throw Exception("Empty response")

            println("HTTP Code: ${response.code}")
            println("Response Body: $responseBody")
            if (!response.isSuccessful) {
                throw Exception("Enrollment failed: ${response.code} - $responseBody")
            }
            val jsonResponse = JSONObject(responseBody)
            return EnrollmentResponse(
                certificate = jsonResponse.getString("certificate")
            )
        }
    }
}