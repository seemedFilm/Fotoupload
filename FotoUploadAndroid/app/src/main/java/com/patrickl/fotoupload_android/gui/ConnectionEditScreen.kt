package com.patrickl.fotoupload_android.ui

import android.util.Log
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
    var intUrl by remember { mutableStateOf("") }
    var extUrl by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Neue Verbindung") })
        }
    ) { padding ->
        Log.d("CSR_DEBUG", "ConnectionEditScreen")
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Namee") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = intUrl,
                onValueChange = { intUrl = it },
                label = { Text("internal URL") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = extUrl,
                onValueChange = { extUrl = it },
                label = { Text("external URL") },
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
                        intUrl = intUrl,
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