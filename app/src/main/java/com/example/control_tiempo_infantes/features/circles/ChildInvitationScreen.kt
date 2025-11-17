package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildInvitationsScreen(
    vm: ChildInvitationsViewModel,
    onBack: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis invitaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (uiState.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.message?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.error?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (uiState.invitations.isEmpty() && !uiState.loading) {
                Spacer(Modifier.padding(top = 16.dp))
                Text("No tienes invitaciones pendientes.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.invitations) { inv ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = inv.circleName ?: "Círculo familiar",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Te invita: ${inv.inviterName ?: "Supervisor"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                inv.inviterEmail?.let { mail ->
                                    Text(
                                        text = "Correo: $mail",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    text = "Miembros actuales: ${inv.circleMembersCount ?: 0}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    text = "Código de invitación: ${inv.code}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(Modifier.padding(top = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { vm.acceptInvitation(inv) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Aceptar")
                                    }

                                    OutlinedButton(
                                        onClick = { vm.rejectInvitation(inv) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
