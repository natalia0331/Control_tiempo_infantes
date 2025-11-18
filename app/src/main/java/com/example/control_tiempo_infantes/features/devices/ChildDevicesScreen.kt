package com.example.control_tiempo_infantes.features.devices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDevicesScreen(
    vm: DevicesViewModel,
    childId: String,
    childName: String,
    onBack: () -> Unit
) {
    LaunchedEffect(childId) {
        vm.loadDevices(childId)
    }

    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Dispositivos de $childName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh(childId) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar"
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
        ) {
            if (state.loading && state.devices.isEmpty()) {
                CircularProgressIndicator()
            } else if (state.devices.isEmpty()) {
                Text("Este infante aún no tiene dispositivos vinculados.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.devices) { device ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = device.model.ifBlank { "Dispositivo" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (device.androidVersion.isNotBlank()) {
                                    Text("Android: ${device.androidVersion}")
                                }
                                if (device.deviceId.isNotBlank()) {
                                    Text("ID: ${device.deviceId}")
                                }
                            }
                        }
                    }
                }
            }

            state.error?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
