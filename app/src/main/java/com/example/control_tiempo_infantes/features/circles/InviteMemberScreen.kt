package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMemberScreen(
    vm: InviteViewModel,
    circleId: String,
    onBack: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("child") } // "child" o "adult"

    val feedbackMessage = uiState.error ?: uiState.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitar miembro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo del invitado") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { type = "child" },
                    enabled = type != "child",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Infante")
                }

                OutlinedButton(
                    onClick = { type = "adult" },
                    enabled = type != "adult",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Adulto")
                }
            }

            Button(
                onClick = {
                    vm.sendInvitation(circleId, email.trim(), type)
                },
                enabled = email.isNotBlank() && !uiState.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.loading) "Enviando..." else "Enviar invitación")
            }

            if (feedbackMessage != null) {
                val color = if (uiState.error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary

                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}
