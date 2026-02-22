package com.patrickl.fotoupload_android.data.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


private val Context.dataStore by preferencesDataStore("connections")

class ConnectionStorage(private val context: Context) {

    private val CONNECTIONS_KEY = stringPreferencesKey("connections_json")
    private val ACTIVE_ID_KEY = stringPreferencesKey("active_connection_id")

    private val json = Json { ignoreUnknownKeys = true }

    val connectionsFlow: Flow<List<ConnectionProfile>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[CONNECTIONS_KEY] ?: "[]"
            json.decodeFromString(raw)
        }

    val activeIdFlow: Flow<String?> =
        context.dataStore.data.map { prefs ->
            prefs[ACTIVE_ID_KEY]
        }

    suspend fun saveConnections(list: List<ConnectionProfile>) {
        context.dataStore.edit { prefs ->
            prefs[CONNECTIONS_KEY] =
                json.encodeToString<List<ConnectionProfile>>(list)
        }
    }

    suspend fun setActive(id: String) {
        context.dataStore.edit {
            it[ACTIVE_ID_KEY] = id
        }
    }
}