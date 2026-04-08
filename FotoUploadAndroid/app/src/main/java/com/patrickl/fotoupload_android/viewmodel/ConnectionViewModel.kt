package com.patrickl.fotoupload_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
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

    fun addConnection(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.add(profile)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun setActive(id: String) {
        viewModelScope.launch {
            repository.setActive(id)
        }
    }

    fun updateConnection(profile: ConnectionProfile) {
        viewModelScope.launch {
            // Since repository doesn't have a specific update, 
            // and add() currently just appends, we should ideally have a replace logic.
            // For now, let's assume 'add' handles replacing if ID exists or we fix repository.
            repository.add(profile) 
        }
    }
}
