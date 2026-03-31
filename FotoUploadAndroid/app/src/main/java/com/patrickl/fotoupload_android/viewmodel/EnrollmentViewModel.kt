package com.patrickl.fotoupload_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.data.EnrollmentRepository
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EnrollmentViewModel(
    private val repository: EnrollmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val state: StateFlow<EnrollmentState> = _state
    private var pendingProfile: ConnectionProfile? = null

    fun enroll(profile: ConnectionProfile) {
        pendingProfile = profile
        viewModelScope.launch {
            _state.value = EnrollmentState.Loading
            try {
                repository.enroll(profile)
                _state.value = EnrollmentState.Success
            } catch (e: Exception) {
                _state.value = EnrollmentState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun consumeProfile(): ConnectionProfile? {
        val profile = pendingProfile
        pendingProfile = null
        return profile
    }
}


sealed class EnrollmentState {
    object Idle : EnrollmentState()
    object Loading : EnrollmentState()
    object Success : EnrollmentState()
    data class Error(val message: String) : EnrollmentState()
}