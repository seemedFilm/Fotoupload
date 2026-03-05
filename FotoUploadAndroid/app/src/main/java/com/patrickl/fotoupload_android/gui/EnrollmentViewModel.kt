package com.patrickl.fotoupload_android.gui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.data.EnrollmentRepository
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.network.HttpClientFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnrollmentViewModel(
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val state: StateFlow<EnrollmentState> = _state

    fun enroll(profile: ConnectionProfile) {
        val scheme = if (profile.useSsl) "https" else "http"
        val baseUrl = "$scheme://${profile.extUrl}:${profile.port}"
        val api = EnrollmentApi(
            client = HttpClientFactory.createDefault(),
            intUrl = baseUrl
        )
        val repository = EnrollmentRepository(
            context = context,
            api = api
        )

        viewModelScope.launch {
            try {
                _state.value = EnrollmentState.Loading
                repository.enroll(profile)
                _state.value = EnrollmentState.Success
            } catch (e: Exception) {
                _state.value = EnrollmentState.Error(
                    e.message ?: "Enrollment failed"
                )
            }
        }
    }
}

sealed class EnrollmentState {
    object Idle : EnrollmentState()
    object Loading : EnrollmentState()
    object Success : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}