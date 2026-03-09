package com.patrickl.fotoupload_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.patrickl.fotoupload_android.ui.theme.FotoUploadAndroidTheme
import com.patrickl.fotoupload_android.navigation.AppNavigation
import android.util.Log

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {

            val dataStore = ConnectionDataStore(applicationContext)
            val profiles = dataStore.getProfiles()

            if (profiles.isEmpty() && KeyStoreManager.hasCertificate()) {
                KeyStoreManager.deleteKey()
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


