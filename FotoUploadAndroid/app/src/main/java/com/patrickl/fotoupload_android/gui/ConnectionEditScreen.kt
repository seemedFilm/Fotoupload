package com.patrickl.fotoupload_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionEditScreen(
    navController: NavHostController,
    viewModel: ConnectionViewModel
) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var extUrl by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8080") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Neue Verbindung") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val profile = ConnectionProfile(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        baseUrl = baseUrl,
                        extUrl = extUrl,
                        port = port.toIntOrNull() ?: 80,
                        username = "",
                        password = "",
                        useSsl = false
                    )

                    viewModel.add(profile)

                    navController.popBackStack()
                }
            ) {
                Text("Speichern")
            }
        }
    }
}