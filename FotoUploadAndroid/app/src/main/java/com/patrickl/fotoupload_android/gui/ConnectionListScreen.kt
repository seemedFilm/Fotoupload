package com.patrickl.fotoupload_android.gui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel

private const val TAG = "ConnectionListScreen.kt"

@Composable
fun ConnectionListScreen(
    navController: NavHostController,
    viewModel: ConnectionViewModel
) {
    val connections by viewModel.connections.collectAsState()
    val active by viewModel.activeConnection.collectAsState()
    
    Log.d(TAG, "Screen loaded")
    
    ConnectionListContent(
        connections = connections,
        activeConnection = active,
        onAddClick = { navController.navigate("connection_add") },
        onSetActive = { viewModel.setActive(it) },
        onDelete = { viewModel.delete(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListContent(
    connections: List<ConnectionProfile>,
    activeConnection: ConnectionProfile?,
    onAddClick: () -> Unit,
    onSetActive: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verbindungen") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Connection")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(connections) { connection ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = { onSetActive(connection.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(connection.name, style = MaterialTheme.typography.titleMedium)
                            Text("${connection.intUrl}:${connection.port}", style = MaterialTheme.typography.bodySmall)
                            if (connection.extUrl.isNotBlank()) {
                                Text("${connection.extUrl}:${connection.port}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (connection.id == activeConnection?.id) {
                                Text("Aktiv", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        IconButton(onClick = { onDelete(connection.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionListPreview() {
    val mockConnections = listOf(
        ConnectionProfile("1", "Zuhause", "192.168.1.100", "home.example.com", 443),
        ConnectionProfile("2", "Büro", "10.0.0.5", "", 80)
    )
    MaterialTheme {
        ConnectionListContent(
            connections = mockConnections,
            activeConnection = mockConnections[0],
            onAddClick = {},
            onSetActive = {},
            onDelete = {}
        )
    }
}
