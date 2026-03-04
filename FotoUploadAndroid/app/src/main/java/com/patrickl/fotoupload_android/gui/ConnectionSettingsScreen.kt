package com.patrickl.fotoupload_android.gui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.data.ConnectionDataStore
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import com.patrickl.fotoupload_android.gui.EnrollmentViewModel
//import com.patrickl.fotoupload_android.viewmodel.EnrollmentState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    navController: NavHostController,
    connectionViewModel: ConnectionViewModel
) {
    Log.d("CSR_DEBUG", "ConnectionSettingsScreen")
    val context = LocalContext.current
    val enrollmentViewModel = remember {
        EnrollmentViewModel(context.applicationContext)
    }
    val state by enrollmentViewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var intUrl by remember { mutableStateOf("") }
    var extUrl by remember { mutableStateOf("") }
    var portInput by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var useSsl by remember { mutableStateOf(false) }

    var portError by remember { mutableStateOf<String?>(null) }
    var profileToSave by remember { mutableStateOf<ConnectionProfile?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verbindung hinzufügen",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 25.sp,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = intUrl,
            onValueChange = { intUrl = it },
            label = { Text("Interne URL (ohne http/https)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = extUrl,
            onValueChange = { extUrl = it },
            label = { Text("Externe URL (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = portInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    portInput = input
                    portError = null
                }
            },
            label = { Text("Port") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = portError != null,
            modifier = Modifier.fillMaxWidth()
        )

        portError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useSsl,
                onCheckedChange = { useSsl = it }
            )
            Text("SSL verwenden")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val defaultPort = if (useSsl) 443 else 80

                val port: Int = if (portInput.isBlank()) {
                    defaultPort
                } else {
                    val parsed = portInput.toIntOrNull()
                    if (parsed == null || parsed !in 1..65535) {
                        portError = "Port muss zwischen 1 und 65535 liegen"
                        return@Button
                    }
                    parsed
                }

                val profile = ConnectionProfile(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    intUrl = intUrl,
                    extUrl = extUrl,
                    port = port,
                    username = username,
                    password = password,
                    useSsl = useSsl
                )

                profileToSave = profile
                enrollmentViewModel.enroll(profile)
            },
            enabled = state !is EnrollmentState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verbindung prüfen & speichern")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (state) {
            is EnrollmentState.Loading -> {
                CircularProgressIndicator()
            }
            is EnrollmentState.Success -> {
                Text("Enrollment erfolgreich")
                profileToSave?.let { profile ->
                    val dataStore = ConnectionDataStore(context)
                    LaunchedEffect(profile) {
                        dataStore.saveProfile(profile)
                        navController.popBackStack()
                    }
                }
            }

            is EnrollmentState.Error -> {
                Text(
                    text = "Fehler: ${(state as EnrollmentState.Error).message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}