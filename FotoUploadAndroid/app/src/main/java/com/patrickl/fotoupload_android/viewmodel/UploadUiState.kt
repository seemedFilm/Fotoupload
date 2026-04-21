package com.patrickl.fotoupload_android.viewmodel

import android.net.Uri
import com.patrickl.fotoupload_android.network.UploadSummary

data class UploadUiState(
    val selectedImages: List<Uri> = emptyList(),
    val remoteImages: List<String> = emptyList(),
    val isUploading: Boolean = false,
    val isFetchingRemote: Boolean = false,
    val uploadSummary: UploadSummary? = null,
    val errorMessage: String? = null
)
