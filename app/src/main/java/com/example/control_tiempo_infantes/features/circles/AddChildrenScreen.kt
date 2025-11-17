package com.example.control_tiempo_infantes.features.circles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions   // 👈 ESTE es el bueno

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChildrenScreen(
    vm: CircleDetailViewModel,
    circleId: String,
    onBack: () -> Unit
) {
    LaunchedEffect(circleId) {
        vm.init(circleId)
    }

    val uiState by vm.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    LaunchedEffect(uiState.childCreated) {
        if (uiState.childCreated) {
            vm.resetChildCreatedFlag()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Registrar infante") },
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
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del infante") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Edad") },
                keyboardOptions = KeyboardOptions(   // 👈 ya SIN paquete
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Button(
                onClick = { vm.addChild(name, age) },
                enabled = !uiState.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text(if (uiState.loading) "Guardando..." else "Guardar")
            }

            uiState.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
