package com.patrickl.fotoupload_android.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "connection_store")

class ConnectionDataStore(private val context: Context) {
    private val PROFILE_KEY = stringPreferencesKey("connection_profile")
    suspend fun saveProfile(profile: ConnectionProfile) {

        context.dataStore.edit { prefs ->

            prefs[stringPreferencesKey("connection_name")] = profile.name
            prefs[stringPreferencesKey("int_url")] = profile.intUrl
            prefs[stringPreferencesKey("ext_url")] = profile.extUrl
            prefs[intPreferencesKey("port")] = profile.port
            prefs[stringPreferencesKey("username")] = profile.username
            prefs[stringPreferencesKey("password")] = profile.password
            prefs[booleanPreferencesKey("use_ssl")] = profile.useSsl
        }
    }

    fun getProfile(): Flow<ConnectionProfile?> {
        return context.dataStore.data.map { prefs ->
            prefs[PROFILE_KEY]?.let {
                Json.decodeFromString(ConnectionProfile.serializer(), it)
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}