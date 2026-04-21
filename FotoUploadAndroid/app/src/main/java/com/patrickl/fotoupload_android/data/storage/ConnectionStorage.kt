package com.patrickl.fotoupload_android.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.patrickl.fotoupload_android.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("connection_store")

class ConnectionStorage(private val context: Context) {

    private val STORE_KEY = stringPreferencesKey("store_json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    val storeFlow: Flow<ConnectionStore> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[STORE_KEY]
            if (raw == null) {
                ConnectionStore()
            } else {
                migrateIfNeeded(json.decodeFromString(raw))
            }
        }

    suspend fun saveStore(store: ConnectionStore) {
        context.dataStore.edit { prefs ->
            prefs[STORE_KEY] =
                json.encodeToString(store)
        }
    }

    private fun migrateIfNeeded(store: ConnectionStore): ConnectionStore {
        return when (store.schemaVersion) {

            1 -> store

            // Sample for migration
            //2 -> migrateV1toV2(store)

            else -> store
        }
    }
}