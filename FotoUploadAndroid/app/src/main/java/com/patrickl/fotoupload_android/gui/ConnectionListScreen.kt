package com.patrickl.fotoupload_android.gui

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.ui.theme.activeStatus
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
private const val TAG = "ConnectionListScreen.kt"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    navController: NavHostController,
    viewModel: ConnectionViewModel
) {
    val connections by viewModel.connections.collectAsState()
    val active by viewModel.activeConnection.collectAsState()
    Log.d(TAG, "$TAG loaded")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verbindungen", style = MaterialTheme.typography.headlineLarge) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("connection_add")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(connections) { connection ->
                val isActive = connection.id == active?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp) // External spacing between cards
                        .then(
                            if (isActive) Modifier.border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.activeStatus,
                                shape = CardDefaults.shape
                            ) else Modifier
                        ),
                    onClick = {
                        viewModel.setActive(connection.id)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(connection.name, style = MaterialTheme.typography.titleMedium)
                            Text("${connection.intUrl}:${connection.port}", style = MaterialTheme.typography.titleMedium)
                            Text("${connection.extUrl}:${connection.port}", style = MaterialTheme.typography.titleLarge)
//                            if (connection.id == active?.id) {
//                                Text("Aktiv", color = MaterialTheme.colorScheme.activeStatus)
//                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.delete(connection.id)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}