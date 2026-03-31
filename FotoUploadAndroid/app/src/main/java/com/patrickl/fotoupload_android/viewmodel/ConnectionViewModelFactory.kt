package com.patrickl.fotoupload_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository

class ConnectionViewModelFactory(
    private val repository: ConnectionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}