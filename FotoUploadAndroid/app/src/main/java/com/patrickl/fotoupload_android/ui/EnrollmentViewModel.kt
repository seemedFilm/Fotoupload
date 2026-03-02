package com.patrickl.fotoupload_android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.patrickl.fotoupload_android.data.EnrollmentRepository

class EnrollmentViewModel(
    private val repository: EnrollmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val state: StateFlow<EnrollmentState> = _state

    fun enroll(token: String) {
        viewModelScope.launch {
            try {
                _state.value = EnrollmentState.Loading
                repository.enroll(token)
                _state.value = EnrollmentState.Success
            } catch (e: Exception) {
                _state.value = EnrollmentState.Error(e.message ?: "Unknown error")
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