package com.patrickl.fotoupload_android

import com.patrickl.fotoupload_android.viewmodel.UploadViewModel
import com.patrickl.fotoupload_android.viewmodel.UploadUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
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
import com.patrickl.fotoupload_android.network.UploadSummary

@Preview
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: UploadViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        selectedImages = uris
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            text = "${ApiConfig.BASE_URL}",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 30.sp,
            color = Color.Green
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        ) {
            Text("Mehrere Bilder auswählen")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                viewModel.uploadImages(context, selectedImages)
            },
            enabled = uiState !is UploadUiState.Loading &&
                    selectedImages.isNotEmpty()
        ) {
            Text("Hochladen")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(selectedImages) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(120.dp)
                )
            }
        }

        if (uiState is UploadUiState.Loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Upload läuft...")
        }
    }

    when (uiState) {
        is UploadUiState.Success -> {
            val summary = (uiState as UploadUiState.Success).summary
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetState()
                        selectedImages = emptyList()
                    }) {
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

        is UploadUiState.Error -> {
            val message = (uiState as UploadUiState.Error).message
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text("OK")
                    }
                },
                title = { Text("Fehler") },
                text = { Text(message) }
            )
        }
        else -> {}
    }
}