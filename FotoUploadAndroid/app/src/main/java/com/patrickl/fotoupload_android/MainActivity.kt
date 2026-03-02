package com.patrickl.fotoupload_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.patrickl.fotoupload_android.ui.theme.FotoUploadAndroidTheme
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.patrickl.fotoupload_android.gui.HomeScreen
import com.patrickl.fotoupload_android.gui.SettingsScreen
import com.patrickl.fotoupload_android.navigation.AppNavigation

import android.util.Log
import com.patrickl.fotoupload_android.security.KeyStoreManager
import com.patrickl.fotoupload_android.security.CsrGenerator



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FotoUploadAndroidTheme {
                App()
            }
        }
    }
}
//override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//
//    KeyStoreManager.generateKeyPairIfNeeded()
//
//    val csr = CsrGenerator.generateCsr("patrick-test-device")
//    Log.d("CSR_TEST", csr)
//
//    setContent {
//        FotoUploadAndroidTheme {
//            App()
//        }
//    }
//}



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
//@Composable
//fun AppNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "main"
//    ) {
//        composable("main") {
//            HomeScreen(navController)
//        }
//
//        composable("settings") {
//            SettingsScreen(navController)
//        }
//    }
//}

