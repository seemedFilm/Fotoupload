package com.patrickl.fotoupload_android.gui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.patrickl.fotoupload_android.ui.theme.Green80
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import com.patrickl.fotoupload_android.viewmodel.UploadViewModel


@Preview(showBackground = true)
@Composable
fun UploadingPreview() {
    Uploading(
        onOpenConnections = {},
        onOpenSettings = {},
        onOpenPictureList = {}
    )
}

@Composable
fun Uploading(
    modifier: Modifier = Modifier,
    uploadViewModel: UploadViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel? = null,
    onOpenConnections: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPictureList: () -> Unit
) {
    val activeConnection =
        connectionViewModel?.activeConnection?.collectAsState()?.value
    val uiState by uploadViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    // Launcher for picking multiple images and videos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                uploadViewModel.setSelectedImages(uris)
            }
        }
    )

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
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Uploading",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                color = Green80
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Launch the photo picker for images and videos
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                enabled =  activeConnection != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                val buttonText = if (uiState.selectedImages.isEmpty()) {
                    "Bilder/Videos auswählen"
                } else {
                    "${uiState.selectedImages.size} Dateien ausgewählt"
                }
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -- HOCHLADEN BUTTON --
            Button(
                onClick = {
                    activeConnection?.let { uploadViewModel.uploadImages(context, it) }
                },
                enabled = activeConnection != null && !uiState.isUploading && uiState.selectedImages.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Wird hochgeladen...", fontSize = 18.sp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Hochladen", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (uiState.uploadSummary != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Upload abgeschlossen",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Erfolgreich: ${uiState.uploadSummary!!.success}")
                        Text("Fehlgeschlagen: ${uiState.uploadSummary!!.failed}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { uploadViewModel.dismissDialog() }) {
                            Text("OK")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // -- PREVIEW GRID --
            if (uiState.selectedImages.isNotEmpty()) {
                Text(
                    text = "Vorschau:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(4.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(uiState.selectedImages) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
