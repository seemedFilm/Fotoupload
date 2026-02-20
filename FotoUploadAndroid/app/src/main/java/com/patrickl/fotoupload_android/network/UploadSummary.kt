package com.patrickl.fotoupload_android.network

data class UploadSummary(
    val total: Int,
    val success: Int,
    val failed: Int,
    val errorMessage: String? = null
)