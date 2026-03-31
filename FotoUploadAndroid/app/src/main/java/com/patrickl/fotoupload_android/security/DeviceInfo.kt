package com.patrickl.fotoupload_android.security

import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceInfo {

    fun getDeviceName(context: Context): String {

        val systemName = Settings.Global.getString(
            context.contentResolver,
            Settings.Global.DEVICE_NAME
        )

        val model = Build.MODEL ?: "UnknownModel"
        val manufacturer = Build.MANUFACTURER ?: "UnknownVendor"

        val name = when {
            !systemName.isNullOrBlank() -> systemName
            else -> "$manufacturer-$model"
        }
        return sanitize(name)
    }

    private fun sanitize(input: String): String {
        return input
            .replace("[^a-zA-Z0-9-_]".toRegex(), "_")
            .take(64) // CN-Limit beachten
    }
}