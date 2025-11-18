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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeScreen(
    vm: ScreenTimeViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        vm.loadToday()
    }

    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Tiempos de pantalla") },
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

            Text(
                text = state.dateLabel,
                style = MaterialTheme.typography.titleMedium
            )

            if (state.loading && state.devices.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            } else if (state.devices.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Hoy aún no hay uso registrado.")
            } else {
                val totalGlobal = state.devices.sumOf { it.totalMinutes }
                if (totalGlobal > 0) {
                    val hours = totalGlobal / 60
                    val minutes = totalGlobal % 60
                    Text(
                        text = "Total hoy: ${hours}h ${minutes}min",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.devices) { dev ->
                        DeviceUsageCard(dev)
                    }
                }
            }

            state.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DeviceUsageCard(item: DeviceUsageItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.model,
                style = MaterialTheme.typography.titleMedium
            )

            val total = item.totalMinutes
            if (total > 0) {
                val h = total / 60
                val m = total % 60
                Text(
                    text = "Uso total: ${h}h ${m}min",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Sin uso registrado hoy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (item.apps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Por aplicación",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                val maxMinutes = item.apps.maxOf { it.totalMinutes }.coerceAtLeast(1)

                item.apps.forEach { app ->
                    val fraction = app.totalMinutes.toFloat() / maxMinutes.toFloat()
                    val h = app.totalMinutes / 60
                    val m = app.totalMinutes % 60

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                        Text(
                            text = if (app.totalMinutes > 0)
                                "${h}h ${m}min"
                            else
                                "0 min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
