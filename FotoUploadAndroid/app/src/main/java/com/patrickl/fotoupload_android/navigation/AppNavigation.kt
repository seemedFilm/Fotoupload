package com.patrickl.fotoupload_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.patrickl.fotoupload_android.gui.HomeScreen
import com.patrickl.fotoupload_android.gui.SettingsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            SettingsScreen(navController)
        }
    }
}