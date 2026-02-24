package com.patrickl.fotoupload_android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionStore(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val connections: List<ConnectionProfile> = emptyList(),
    val activeId: String? = null
)

const val CURRENT_SCHEMA_VERSION = 1