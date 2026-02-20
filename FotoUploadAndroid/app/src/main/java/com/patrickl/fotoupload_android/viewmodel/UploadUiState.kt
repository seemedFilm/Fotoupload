package com.patrickl.fotoupload_android.viewmodel

import com.patrickl.fotoupload_android.network.UploadSummary

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Loading : UploadUiState()
    data class Success(val summary: UploadSummary) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}