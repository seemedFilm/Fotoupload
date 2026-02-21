package com.patrickl.fotoupload_android.gui

import com.patrickl.fotoupload_android.viewmodel.UploadViewModel
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
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import com.patrickl.fotoupload_android.config.ApiConfig

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onOpenSettings = {}
    )
}
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: UploadViewModel = viewModel(),
    onOpenSettings: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        viewModel.setSelectedImages(uris)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenSettings() }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
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
                text = ApiConfig.BASE_URL,
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
                onClick = { viewModel.uploadImages(context) },
                enabled = !uiState.isUploading &&
                        uiState.selectedImages.isNotEmpty()
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
                onDismissRequest = { viewModel.dismissDialog() },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) {
                        Text("OK")
                    }
                },
                title = { Text("Upload abgeschlossen") },
                text = {
                    Text(
                        """
                    Gesamt: ${summary.total}
                    Erfolgreich: ${summary.success}
                    Fehlerhaft: ${summary.failed}
                    """.trimIndent()
                    )
                }
            )
        }
    }
}