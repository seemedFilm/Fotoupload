package com.patrickl.fotoupload_android.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.NetworkUtils
import com.patrickl.fotoupload_android.network.UploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


private const val TAG = "UploadViewModel.kt"
class UploadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState

    fun setSelectedImages(images: List<Uri>) {
        _uiState.value = _uiState.value.copy(
            selectedImages = images
        )
    }

    fun fetchRemoteImages(context: Context, profile: ConnectionProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingRemote = true)
            try {
                val remote = UploadService.fetchUploadedImages(context, profile)
                _uiState.value = _uiState.value.copy(
                    remoteImages = remote,
                    isFetchingRemote = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching remote images", e)
                _uiState.value = _uiState.value.copy(isFetchingRemote = false)
            }
        }
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
                val result = UploadService.uploadMultipleImages(
                    context,
                    images,
                    profile
                )
                
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadSummary = result,
                    // Only clear selection if there are NO failures
                    selectedImages = if (result.failed == 0) emptyList() else _uiState.value.selectedImages
                )

            } catch (e: Exception) {
                Log.e(TAG, "[uploadImages]: Exception during upload", e)
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
