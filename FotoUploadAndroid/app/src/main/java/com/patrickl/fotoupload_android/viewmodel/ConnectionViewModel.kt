package com.patrickl.fotoupload_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val repository: ConnectionRepository
) : ViewModel() {

    val connections = repository.connections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeConnection = repository.activeConnection
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActive(id: String) {
        viewModelScope.launch {
            repository.setActive(id)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}