package com.patrickl.fotoupload_android.gui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    var counter by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Button gedrückt: $counter mal",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { counter++ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Erhöhen")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { counter = 0 },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurücksetzen")
        }
    }
}
