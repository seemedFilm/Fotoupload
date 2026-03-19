package com.patrickl.fotoupload_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.patrickl.fotoupload_android.ui.theme.FotoUploadAndroidTheme
import com.patrickl.fotoupload_android.navigation.AppNavigation
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository
import com.patrickl.fotoupload_android.data.storage.ConnectionStorage
import com.patrickl.fotoupload_android.security.KeyStoreManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val storage = ConnectionStorage(applicationContext)
            val repository = ConnectionRepository(storage)
            val profiles = repository.connections.first()

            if (profiles.isEmpty() && KeyStoreManager.hasAnyCertificate()) {
                KeyStoreManager.deleteAllCertificates()
            }
        }

        setContent {
            FotoUploadAndroidTheme {
                App()
            }
        }
    }
}


@Preview
@Composable
fun App() {
    AppNavigation()
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FotoUploadAndroidTheme {
        App()
    }
}
