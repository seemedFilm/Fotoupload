package com.patrickl.fotoupload_android.gui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.patrickl.fotoupload_android.BuildConfig
import com.patrickl.fotoupload_android.domain.model.ConnectionProfile
import com.patrickl.fotoupload_android.viewmodel.ConnectionViewModel
import com.patrickl.fotoupload_android.viewmodel.EnrollmentViewModel
import com.patrickl.fotoupload_android.viewmodel.EnrollmentViewModelFactory
import com.patrickl.fotoupload_android.viewmodel.EnrollmentState
import java.util.UUID

private const val TAG = "ConnectionSettingsScreen.kt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    navController: NavHostController,
    viewModel: ConnectionViewModel,
    connectionId: String? = null
) {
    val context = LocalContext.current
    val enrollmentViewModel: EnrollmentViewModel = viewModel(
        factory = EnrollmentViewModelFactory(context)
    )
    val enrollmentState by enrollmentViewModel.state.collectAsState()

    // Default values for Debugging
    val isDebug = BuildConfig.DEBUG && connectionId == null
    
    var name by remember { mutableStateOf(if (isDebug) "Test Server" else "") }
    var intUrl by remember { mutableStateOf(if (isDebug) "192.168.1.190" else "") }
    var extUrl by remember { mutableStateOf(if (isDebug) "bilder.diefamilielang.de" else "") }
    var port by remember { mutableStateOf(if (isDebug) "443" else "") }
    var username by remember { mutableStateOf(if (isDebug) "patrick" else "") }
    var password by remember { mutableStateOf(if (isDebug) "patrick" else "") }
    var useSsl by remember { mutableStateOf(if (isDebug) true else false) }
    
    // Hidden internal fields to preserve them during Edit
    var resoW by remember { mutableIntStateOf(0) }
    var resoH by remember { mutableIntStateOf(0) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Daten laden bei Edit
    LaunchedEffect(connectionId) {
        connectionId?.let { id ->
            viewModel.connections.value.find { it.id == id }?.let { connection ->
                name = connection.name
                intUrl = connection.intUrl
                extUrl = connection.extUrl
                port = connection.port.toString()
                username = connection.username
                password = connection.password
                useSsl = connection.useSsl
                resoW = connection.resoW
                resoH = connection.resoH
            }
        }
    }

    // React to enrollment state changes
    LaunchedEffect(enrollmentState) {
        when (enrollmentState) {
            is EnrollmentState.Success -> {
                isLoading = false
                val profile = enrollmentViewModel.consumeProfile()
                if (profile != null) {
                    viewModel.addConnection(profile)
                    navController.popBackStack()
                }
            }
            is EnrollmentState.Error -> {
                isLoading = false
                errorMessage = (enrollmentState as EnrollmentState.Error).message
            }
            is EnrollmentState.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (connectionId == null) "Neue Verbindung" else "Verbindung bearbeiten",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 24.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
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
                label = { Text("Interne URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = extUrl,
                onValueChange = { extUrl = it },
                label = { Text("Externe URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Passwort") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = useSsl,
                    onCheckedChange = { useSsl = it }
                )
                Text("SSL verwenden")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    val portInt = port.toIntOrNull() ?: 0
                    errorMessage = null

                    val profile = ConnectionProfile(
                        id = connectionId ?: UUID.randomUUID().toString(),
                        name = name,
                        intUrl = intUrl,
                        extUrl = extUrl,
                        port = portInt,
                        username = username,
                        password = password,
                        useSsl = useSsl,
                        resoW = resoW,
                        resoH = resoH
                    )

                    if (connectionId == null) {
                        enrollmentViewModel.enroll(profile)
                    } else {
                        viewModel.updateConnection(profile)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Bitte warten..." else if (connectionId == null) "Speichern" else "Aktualisieren")
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Abbrechen")
            }
        }
    }
}
