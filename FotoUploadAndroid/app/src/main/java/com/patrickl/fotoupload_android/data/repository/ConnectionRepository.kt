package com.patrickl.fotoupload_android.data.repository

import com.patrickl.fotoupload_android.data.storage.ConnectionStorage
import com.patrickl.fotoupload_android.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class ConnectionRepository(
    private val storage: ConnectionStorage
) {

    val store: Flow<ConnectionStore> = storage.storeFlow

    val connections: Flow<List<ConnectionProfile>> =
        storage.storeFlow.map { store ->
            store.connections
        }

    val activeConnection: Flow<ConnectionProfile?> =
        storage.storeFlow.map { store ->
            store.connections.find { it.id == store.activeId }
        }

    suspend fun add(profile: ConnectionProfile) {
        val current = storage.storeFlow.first()
        val updated = current.copy(
            connections = current.connections + profile
        )
        storage.saveStore(updated)
    }

    suspend fun delete(id: String) {
        val current = storage.storeFlow.first()
        val updated = current.copy(
            connections = current.connections.filterNot { it.id == id },
            activeId = if (current.activeId == id) null else current.activeId
        )
        storage.saveStore(updated)
    }

    suspend fun setActive(id: String) {
        val current = storage.storeFlow.first()
        storage.saveStore(current.copy(activeId = id))
    }
}