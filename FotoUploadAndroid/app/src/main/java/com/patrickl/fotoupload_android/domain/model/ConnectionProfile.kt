package com.patrickl.fotoupload_android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionProfile(
    val id: String,
    val name: String,
    val intUrl: String,
    val extUrl: String = "",
    val port: Int = 80,
    val username: String = "",
    val password: String = "",
    val useSsl: Boolean = false
)