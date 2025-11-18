@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.control_tiempo_infantes.domain.model.Circle

@Composable
fun CirclesScreen(
    vm: CirclesViewModel,
    onBack: () -> Unit,
    onOpenCircle: (String) -> Unit,
    onOpenInvitations: () -> Unit        // 👈 nuevo callback
) {
    val state by vm.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = if (state.isChild) "Mis círculos familiares"
                        else "Círculos familiares"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Solo ADULTO puede crear círculos
            if (!state.isChild) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear círculo"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // Para INFANTE: botón para ver invitaciones
            if (state.isChild) {
                Button(
                    onClick = onOpenInvitations,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Ver mis invitaciones")
                }
            }

            if (state.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (state.circles.isEmpty()) {
                    Text(
                        text = if (state.isChild)
                            "Aún no perteneces a ningún círculo familiar."
                        else
                            "Todavía no has creado círculos familiares.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.circles) { circle ->
                            CircleItem(circle = circle) {
                                onOpenCircle(circle.id)
                            }
                        }
                    }
                }
            }

            state.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }

    // Diálogo para crear círculo (solo se puede abrir si es adulto)
    if (showCreateDialog && !state.isChild) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo círculo familiar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.createCircle(newName.trim(), newDesc.trim())
                        if (newName.isNotBlank()) {
                            showCreateDialog = false
                            newName = ""
                            newDesc = ""
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CircleItem(
    circle: Circle,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = circle.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            if (circle.description.isNotBlank()) {
                Text(
                    text = circle.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
