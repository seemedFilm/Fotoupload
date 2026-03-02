package com.patrickl.fotoupload_android.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.patrickl.fotoupload_android.data.EnrollmentRepository
import com.patrickl.fotoupload_android.network.EnrollmentApi
import com.patrickl.fotoupload_android.network.HttpClientFactory

class EnrollmentViewModel(
    private val context: Context
) : ViewModel() {

    private val api = EnrollmentApi(
        client = HttpClientFactory.createDefault(),
        baseUrl = "https://deinserver.de"
    )

    private val repository = EnrollmentRepository(
        context = context,
        api = api
    )

    private val _state = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val state: StateFlow<EnrollmentState> = _state

    fun enroll(token: String) {
        viewModelScope.launch {
            try {
                _state.value = EnrollmentState.Loading
                repository.enroll(token)
                _state.value = EnrollmentState.Success
            } catch (e: Exception) {
                _state.value = EnrollmentState.Error(e.message ?: "Error")
            }
        }
    }
}