package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

            Text(
                text = "Escribe el correo del miembro que quieres invitar a este círculo familiar.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo del invitado") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "¿A quién estás invitando?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectableRoleCard(
                    title = "Infante",
                    description = "Cuenta para niño/niña que será monitoreada.",
                    selected = type == "child",
                    onClick = { type = "child" },
                    modifier = Modifier.weight(1f)
                )

                SelectableRoleCard(
                    title = "Adulto",
                    description = "Padre, madre u otro adulto con acceso a los reportes.",
                    selected = type == "adult",
                    onClick = { type = "adult" },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    vm.sendInvitation(circleId, email.trim(), type)
                },
                enabled = email.isNotBlank() && !uiState.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(if (uiState.loading) "Enviando..." else "Enviar invitación")
            }

            feedbackMessage?.let { msg ->
                val color = if (uiState.error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary

                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectableRoleCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
