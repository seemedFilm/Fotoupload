package com.patrickl.fotoupload_android.data.repository

import com.patrickl.fotoupload_android.data.storage.ConnectionStorage
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.flow.combine

import kotlinx.coroutines.flow.first

class ConnectionRepository(private val storage: ConnectionStorage) {

    val connections = storage.connectionsFlow

    val activeConnection =
        combine(storage.connectionsFlow, storage.activeIdFlow) { list, activeId ->
            list.find { it.id == activeId }
        }

    suspend fun add(profile: ConnectionProfile) {
        val current = storage.connectionsFlow.first()
        storage.saveConnections(current + profile)
    }

    suspend fun delete(id: String) {
        val current = storage.connectionsFlow.first()
        storage.saveConnections(current.filterNot { it.id == id })
    }

    suspend fun setActive(id: String) {
        storage.setActive(id)
    }
}