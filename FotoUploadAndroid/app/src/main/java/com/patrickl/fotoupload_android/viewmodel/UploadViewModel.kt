package com.patrickl.fotoupload_android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.NetworkUtils
import com.patrickl.fotoupload_android.network.UploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState

    fun setSelectedImages(images: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            selectedImages = images
        )
    }

    fun uploadImages(context: Context, profile: ConnectionProfile) {
        val images = _uiState.value.selectedImages
        if (images.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorMessage = null
            )

            try {
                val isWifi = NetworkUtils.isWifiConnected(context)
                val host = if (isWifi) profile.intUrl else profile.extUrl.ifBlank { profile.intUrl }
                val protocol = if (profile.useSsl) "https" else "http"
                val baseUrl = "$protocol://$host:${profile.port}"

                val result = UploadService.uploadMultipleImages(
                    context,
                    images,
                    baseUrl
                )

                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadSummary = result,
                    selectedImages = emptyList()
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            uploadSummary = null,
            errorMessage = null
        )
    }
}
