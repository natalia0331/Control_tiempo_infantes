package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleDetailScreen(
    vm: CircleDetailViewModel,
    circleId: String,
    onBack: () -> Unit,
    onAddChild: () -> Unit,
    onInviteMember: () -> Unit,
    onOpenLinkDevice: (String) -> Unit,
    onOpenChildDevices: (String, String) -> Unit      // 👈 NUEVO
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
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar"
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
                            val devicesForChild =
                                uiState.devicesByChild[child.id].orEmpty()

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

                                    Text(
                                        text = "Dispositivos vinculados:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (devicesForChild.isEmpty()) {
                                        Text(
                                            text = "Sin dispositivos vinculados todavía.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        devicesForChild.forEach { device ->
                                            val suffix =
                                                if (device.deviceId.isNotBlank())
                                                    " (${device.deviceId.takeLast(4)})"
                                                else
                                                    ""

                                            Text(
                                                text = "• ${device.model.ifBlank { "Dispositivo" }}$suffix",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
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

                                    TextButton(
                                        onClick = {
                                            onOpenChildDevices(child.id, child.name)
                                        }
                                    ) {
                                        Text("Ver dispositivos de ${child.name}")
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
