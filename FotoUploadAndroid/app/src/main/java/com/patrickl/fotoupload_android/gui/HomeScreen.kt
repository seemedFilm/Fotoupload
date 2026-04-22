package com.patrickl.fotoupload_android.gui

import com.patrickl.fotoupload_android.viewmodel.UploadViewModel
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoLibrary
import com.patrickl.fotoupload_android.ui.theme.Green80

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onOpenConnections = {},
        onOpenSettings = {},
        onOpenPictureList = {},
        onOpenUploading = {}
    )
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uploadViewModel: UploadViewModel = viewModel(),
    connectionViewModel: ConnectionViewModel? = null,
    onOpenPictureList: () -> Unit,
    onOpenConnections: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUploading: () -> Unit
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
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Foto Upload zu",
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
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    onOpenPictureList()
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
                Text(
                    text ="Bilder Auflisten",
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -- HOCHLADEN BUTTON --
            Button(
                onClick = {
                    // activeConnection?.let { uploadViewModel.uploadImages(context, it) }
                    onOpenUploading()
                },
                enabled = activeConnection != null,
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
            
            Spacer(modifier = Modifier.height(32.dp))

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
            }
        }
    }
}
