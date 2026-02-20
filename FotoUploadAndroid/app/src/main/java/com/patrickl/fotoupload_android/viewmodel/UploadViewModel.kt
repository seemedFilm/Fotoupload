package com.patrickl.fotoupload_android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.config.ApiConfig
import com.patrickl.fotoupload_android.network.UploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<UploadUiState>(UploadUiState.Idle)

    val uiState: StateFlow<UploadUiState> = _uiState

    fun uploadImages(
        context: Context,
        images: List<Uri>
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = UploadUiState.Loading

                val result = UploadService.uploadMultipleImages(
                    context,
                    images,
                    ApiConfig.BASE_URL
                )

                _uiState.value = UploadUiState.Success(result)

            } catch (e: Exception) {

                _uiState.value = UploadUiState.Error(
                    e.message ?: "Unbekannter Fehler"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = UploadUiState.Idle
    }
}