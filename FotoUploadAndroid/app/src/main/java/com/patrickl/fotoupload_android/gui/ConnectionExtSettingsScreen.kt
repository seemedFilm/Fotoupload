package com.patrickl.fotoupload_android.gui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel

private const val TAG = "ConnectionExtSettingsScreen.kt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionExtSettingsScreen(
    navController: NavHostController,
    viewModel: ConnectionViewModel,
    connectionId: String? = null
) {
    Log.i(TAG, "ConnectionExtSettingsScreen loaded for id: $connectionId")
    
    var resoH by remember { mutableStateOf("") }
    var resoW by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load existing data if editing
    LaunchedEffect(connectionId) {
        connectionId?.let { id ->
            viewModel.connections.value.find { it.id == id }?.let { connection ->
                resoW = connection.resoW.toString()
                resoH = connection.resoH.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Erweiterte Einstellungen",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 24.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = resoW,
                onValueChange = { resoW = it },
                label = { Text("Auflösung Breite (resoW)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = resoH,
                onValueChange = { resoH = it },
                label = { Text("Auflösung Höhe (resoH)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
            }

            // Primary Action: Save/Update
            Button(
                onClick = {
                    val rW = resoW.toIntOrNull() ?: 0
                    val rH = resoH.toIntOrNull() ?: 0
                    errorMessage = null
                    
                    if (connectionId != null) {
                        viewModel.connections.value.find { it.id == connectionId }?.let { current ->
                            val updatedProfile = current.copy(
                                resoW = rW,
                                resoH = rH
                            )
                            viewModel.updateConnection(updatedProfile)
                            navController.popBackStack()
                        }
                    } else {
                        errorMessage = "Fehler: Keine Verbindung ausgewählt."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && connectionId != null
            ) {
                Text(if (isLoading) "Bitte warten..." else "Aktualisieren")
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Action: Cancel
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Abbrechen")
            }
        }
    }
}
