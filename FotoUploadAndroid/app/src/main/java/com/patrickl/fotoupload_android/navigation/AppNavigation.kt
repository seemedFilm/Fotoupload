package com.patrickl.fotoupload_android.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.patrickl.fotoupload_android.data.storage.ConnectionStorage
import com.patrickl.fotoupload_android.data.repository.ConnectionRepository
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModelFactory
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.patrickl.fotoupload_android.gui.HomeScreen
import com.patrickl.fotoupload_android.gui.ConnectionListScreen
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import androidx.compose.runtime.remember
import com.patrickl.fotoupload_android.gui.ConnectionSettingsScreen
import com.patrickl.fotoupload_android.gui.PictureList

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val storage = remember { ConnectionStorage(context) }
    val repository = remember { ConnectionRepository(storage) }
    val connectionViewModel: ConnectionViewModel =
        viewModel(factory = ConnectionViewModelFactory(repository))

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                connectionViewModel = connectionViewModel,

                onOpenConnections = {
                    navController.navigate("connections")
                },
                onOpenSettings = { navController.navigate("settings") },
                onOpenPictureList = { navController.navigate("picture_list") } // Handle navigation here
            )
        }
        composable("connections") {
            ConnectionListScreen(navController, connectionViewModel)
        }
        composable("connection_add") {
            ConnectionSettingsScreen(
                navController = navController,
                viewModel = connectionViewModel
            )
        }
        composable("picture_list") {
            PictureList(
                connectionViewModel = connectionViewModel,
                onOpenConnections = {
                    navController.navigate("connections")
                },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("connection_edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            ConnectionSettingsScreen(
                navController = navController,
                viewModel = connectionViewModel,
                connectionId = id
            )
        }
    }
}
