@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CirclesScreen(
    vm: CirclesViewModel,
    onBack: () -> Unit
) {
    val items   by vm.items.collectAsState(initial = emptyList())
    val message by vm.message.collectAsState(initial = null)
    val loading by vm.loading.collectAsState(initial = false)

    val snackbar = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    // Scroll behavior para LargeTopAppBar
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topBarState)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Círculos familiares",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (items.isEmpty()) {
                Text(
                    "Aún no tienes círculos. Crea el primero con el botón +",
                    modifier = Modifier.padding(16.dp)
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { c ->
                    ElevatedCard {
                        Column(Modifier.padding(12.dp)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium)
                            if (c.description.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(c.description)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Miembros: ${c.members.size}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!loading) showDialog = false },
            title = { Text("Nuevo círculo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        enabled = !loading
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Descripción (opcional)") },
                        enabled = !loading
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.create(name, desc) {
                            name = ""; desc = ""; showDialog = false
                        }
                    },
                    enabled = name.isNotBlank() && !loading
                ) { Text(if (loading) "Creando..." else "Crear") }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!loading) showDialog = false },
                    enabled = !loading
                ) { Text("Cancelar") }
            }
        )
    }
}
