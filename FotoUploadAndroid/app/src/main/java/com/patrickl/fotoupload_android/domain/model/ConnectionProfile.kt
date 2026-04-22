package com.patrickl.fotoupload_android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionProfile(
    val id: String,
    val name: String,
    val intUrl: String,
    val extUrl: String = "",
    val port: Int,
    val username: String = "",
    val password: String = "",
    val useSsl: Boolean = false,
    val resoW: Int = 0,
    val resoH: Int = 0
) {
    /**
     * Returns the base URL for a given host, handling default ports (80/443).
     */
    fun getBaseUrl(host: String): String {
        val protocol = if (useSsl) "https" else "http"
        val defaultPort = if (useSsl) 443 else 80
        val portPart = if (port == defaultPort || port == 0) "" else ":$port"
        return "$protocol://$host$portPart".trimEnd('/')
    }
}
