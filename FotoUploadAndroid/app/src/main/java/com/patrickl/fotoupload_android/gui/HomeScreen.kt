package com.patrickl.fotoupload_android

import com.patrickl.fotoupload_android.network.UploadService
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState


@Preview
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var serverUrl by remember { mutableStateOf("192.168.1.190") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        selectedImages = uris
    }
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        //verticalArrangement = Arrangement.Center,
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
            text = "url",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 50.sp,
            color = Color.Red
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
                scope.launch {
                    val result = UploadService.uploadMultipleImages(
                        context,
                        selectedImages,
                        ApiConfig.BASE_URL

                    )
                    println(result)
                }
            }
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


    }
}