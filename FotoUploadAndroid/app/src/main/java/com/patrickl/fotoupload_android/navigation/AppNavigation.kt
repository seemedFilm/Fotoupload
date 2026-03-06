package com.patrickl.fotoupload_android.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.patrickl.fotoupload_android.data.storage.ConnectionStorage
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModelFactory
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.patrickl.fotoupload_android.gui.HomeScreen
//import com.patrickl.fotoupload_android.gui.SettingsScreen
import com.patrickl.fotoupload_android.ui.ConnectionEditScreen
import com.patrickl.fotoupload_android.gui.ConnectionListScreen
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import androidx.compose.runtime.remember
import com.patrickl.fotoupload_android.gui.ConnectionSettingsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current

    // Repository manuell erstellen
    val storage = remember { ConnectionStorage(context) }
    val repository = remember { ConnectionRepository(storage) }

    // ViewModel manuell erzeugen
    val connectionViewModel: ConnectionViewModel =
        viewModel(factory = ConnectionViewModelFactory(repository))

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onOpenConnections = {
                    navController.navigate("connections")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("connection_add") {
            ConnectionSettingsScreen(navController, connectionViewModel)
        }

        composable("connections") {
            ConnectionListScreen(navController, connectionViewModel)
        }

        composable("connection_edit") {
            ConnectionEditScreen(navController, connectionViewModel)
        }
        composable("connection_add") {
            ConnectionSettingsScreen(navController, connectionViewModel)
        }
    }
}