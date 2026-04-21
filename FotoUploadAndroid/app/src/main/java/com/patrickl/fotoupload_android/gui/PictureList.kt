package com.patrickl.fotoupload_android.gui

import com.patrickl.fotoupload_android.viewmodel.UploadViewModel
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import com.patrickl.fotoupload_android.ui.theme.Green80
import com.patrickl.fotoupload_android.network.NetworkUtils


@Composable
fun PictureList(
    modifier: Modifier = Modifier,
    uploadViewModel: UploadViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel? = null,
    onOpenConnections: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val activeConnection =
        connectionViewModel?.activeConnection?.collectAsState()?.value
    val uiState by uploadViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Fetch images when connection is available
    LaunchedEffect(activeConnection) {
        activeConnection?.let {
            uploadViewModel.fetchRemoteImages(context, it)
        }
    }

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { expanded = true }
                ) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Verbindungen verwalten") },
                        onClick = {
                            expanded = false
                            onOpenConnections()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Allgemeine Settings") },
                        onClick = {
                            expanded = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Foto Auflistung (Server)",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                color = Green80
            )
            Text(
                text = activeConnection?.name ?: "Keine Verbindung gewählt",
                style = MaterialTheme.typography.headlineLarge,
                color = if (activeConnection != null)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Red,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isFetchingRemote) {
                CircularProgressIndicator()
            } else if (uiState.remoteImages.isEmpty()) {
                Text("Keine Bilder auf dem Server gefunden.")
            } else {
                val isWifi = NetworkUtils.isWifiConnected(context)
                val host = if (isWifi) activeConnection?.intUrl else activeConnection?.extUrl?.ifBlank { activeConnection.intUrl }
                val protocol = if (activeConnection?.useSsl == true) "https" else "http"
                val portStr = if (activeConnection?.port == 80 || activeConnection?.port == 443) "" else ":${activeConnection?.port}"
                val baseUrl = "$protocol://$host$portStr"

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(uiState.remoteImages) { filename ->
                        val imageUrl = "${baseUrl.trimEnd('/')}/media/$filename"
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = filename,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(120.dp)
                        )
                    }
                }
            }
        }
    }
}
