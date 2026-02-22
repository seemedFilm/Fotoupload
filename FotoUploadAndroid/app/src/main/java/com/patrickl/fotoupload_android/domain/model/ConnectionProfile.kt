package com.patrickl.fotoupload_android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionProfile(
    val id: String,
    val name: String,
    val baseUrl: String,
    val port: Int,
    val username: String,
    val password: String,
    val useSsl: Boolean
)