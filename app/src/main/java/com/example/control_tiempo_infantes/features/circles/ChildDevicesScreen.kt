// features/circles/ChildDevicesScreen.kt
package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.control_tiempo_infantes.domain.model.Device
import com.example.control_tiempo_infantes.features.devices.DevicesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDevicesScreen(
    vm: DevicesViewModel,
    childName: String,
    childId: String,
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
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.devices.isEmpty()) {
                Text(
                    text = "Este infante aún no tiene dispositivos vinculados.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.devices) { device ->
                        DeviceItem(device = device)
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

@Composable
private fun DeviceItem(device: Device) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = device.model.ifBlank { "Dispositivo sin nombre" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (device.androidVersion.isNotBlank()) {
                Text(
                    text = "Android: ${device.androidVersion}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (device.lastSyncAt > 0L) {
                val date = remember(device.lastSyncAt) {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(device.lastSyncAt))
                }
                Text(
                    text = "Última sincronización: $date",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "ID interno: ${device.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
