package com.patrickl.fotoupload_android.gui

import com.patrickl.fotoupload_android.viewmodel.UploadViewModel
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onOpenConnections = {},
        onOpenSettings = {}
    )
}

@Composable
fun HomeScreen(
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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uploadViewModel.setSelectedImages(uris)
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
            Spacer(modifier = Modifier.height(25.dp))
            Text(
                text = "Foto Upload zu",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 50.sp,
                color = Color.Red
            )
            Text(
                text = activeConnection?.name ?: "Keine Verbindung gewählt",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 30.sp,
                color = Color.Green
            )
            Spacer(modifier = Modifier.height(50.dp))
            Button(onClick = {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }) {
                Text("Mehrere Bilder auswählen")
            }
            Button(
                onClick = { 
                    activeConnection?.let { uploadViewModel.uploadImages(context, it) }
                },
                enabled = !uiState.isUploading &&
                        uiState.selectedImages.isNotEmpty() &&
                        activeConnection != null
            ) {
                Text("Hochladen")
            }
            if (uiState.isUploading) {
                CircularProgressIndicator()
                Text("Upload läuft...")
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(uiState.selectedImages) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(120.dp)
                    )
                }
            }
        }

        uiState.uploadSummary?.let { summary ->
            AlertDialog(
                onDismissRequest = { uploadViewModel.dismissDialog() },
                confirmButton = {
                    TextButton(onClick = { uploadViewModel.dismissDialog() }) {
                        Text("OK")
                    }
                },
                title = { Text("Upload Status") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Gesamt: ${summary.total}")
                        Text("Erfolgreich: ${summary.success}", color = Color(0xFF4CAF50))
                        Text("Fehlerhaft: ${summary.failed}", color = if (summary.failed > 0) Color.Red else Color.Unspecified)
                        
                        summary.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Details / Server Response:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            SelectionContainer {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
