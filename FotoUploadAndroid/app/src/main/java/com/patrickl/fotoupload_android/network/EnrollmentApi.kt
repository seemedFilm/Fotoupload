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

private const val TAG = "EnrollmentApi.kt"

class EnrollmentApi(
    private val client: OkHttpClient,
    private val intUrl: String
) {

    suspend fun login(
        username: String,
        password: String
    ): String {
        Log.d(TAG, "[login]: Attempting login to $intUrl/login.php for user: $username")
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
            
        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d(TAG, "[login]: Server responded with HTTP ${response.code}")
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "[login]: Login failed. Code: ${response.code}, Body: $responseBody")
                    throw Exception("[Login] failed: ${response.code}")
                }
                
                if (responseBody == null) {
                    Log.e(TAG, "[login]: Received empty response body")
                    throw Exception("Empty response")
                }
                
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse.getString("token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "login: Exception during login process", e)
            throw e
        }
    }

    suspend fun enroll(
        token: String,
        csr: String
    ): EnrollmentResponse {
        Log.d(TAG, "[enroll]: Attempting enrollment to $intUrl/enroll.php")
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

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.d(TAG, "enroll: Server responded with HTTP ${response.code}")
                
                if (responseBody == null) {
                    Log.e(TAG, "[enroll]: Received empty response body")
                    throw Exception("Empty response")
                }

                if (!response.isSuccessful) {
                    Log.e(TAG, "[enroll]: Enrollment failed. Code: ${response.code}, Body: $responseBody")
                    throw Exception("Enrollment failed: ${response.code} - $responseBody")
                }
                
                val jsonResponse = JSONObject(responseBody)
                return EnrollmentResponse(
                    certificate = jsonResponse.getString("certificate")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "[enroll]: Exception during enrollment process", e)
            throw e
        }
    }
}
