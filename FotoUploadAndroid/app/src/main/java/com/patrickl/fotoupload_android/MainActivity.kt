package com.patrickl.fotoupload_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.patrickl.fotoupload_android.ui.theme.FotoUploadAndroidTheme
import com.patrickl.fotoupload_android.gui.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            FotoUploadAndroidTheme {
                App()
            }
        }
    }
}
@Preview
@Composable
fun App() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Aktion */ }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        HomeScreen(
            modifier = Modifier.padding(innerPadding)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AppPreview() {
    FotoUploadAndroidTheme {
        App()
    }
}
