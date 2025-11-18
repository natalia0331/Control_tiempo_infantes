package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding


@ExperimentalMaterial3Api
@Composable
fun CircleDetailScreen(
    vm: CircleDetailViewModel,
    circleId: String,
    onBack: () -> Unit,
    onAddChild: () -> Unit,
    onInviteMember: () -> Unit,
    onOpenLinkDevice: (String) -> Unit
) {
    LaunchedEffect(circleId) {
        vm.init(circleId)
    }

    val uiState by vm.uiState.collectAsState()

    var showCodeDialog by remember { mutableStateOf(false) }
    var codeToShow by remember { mutableStateOf("") }

    LaunchedEffect(uiState.lastGeneratedCode) {
        uiState.lastGeneratedCode?.let { code ->
            codeToShow = code
            showCodeDialog = true
            vm.clearCode()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Detalle del círculo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón para refrescar la lista de infantes
                    IconButton(onClick = { vm.refreshChildren() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar infantes"
                        )
                    }

                    TextButton(onClick = onInviteMember) {
                        Text("Invitar miembro")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddChild) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar infante"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.loading && uiState.children.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = "Infantes en este círculo:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.children.isEmpty()) {
                    Text("Aún no has registrado infantes.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.children) { child ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = child.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = "Edad: ${child.age}")

                                    Spacer(modifier = Modifier.height(8.dp))

                                    androidx.compose.foundation.layout.Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        TextButton(
                                            onClick = { vm.generateLinkCode(child.id) }
                                        ) {
                                            Text("Generar código")
                                        }
                                        TextButton(
                                            onClick = { onOpenLinkDevice(child.id) }
                                        ) {
                                            Text("Vincular dispositivo")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.error?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Código de vinculación") },
            text = {
                Text(
                    "Comparte este código con el dispositivo del infante. " +
                            "Es válido solo por unos minutos.\n\nCódigo: $codeToShow"
                )
            }
        )
    }
}
