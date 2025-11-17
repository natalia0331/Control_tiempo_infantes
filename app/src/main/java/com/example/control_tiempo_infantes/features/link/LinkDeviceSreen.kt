package com.example.control_tiempo_infantes.features.link

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDeviceScreen(
    vm: LinkDeviceViewModel,
    childId: String,
    deviceId: String,
    onBack: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    var code by remember { mutableStateOf("") }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            vm.resetSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Vincular dispositivo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ingresa el código temporal que generaste para este infante. " +
                        "El dispositivo actual quedará asociado a su cuenta."
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código de 6 dígitos") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.linkDevice(code, childId, deviceId) },
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.loading) "Vinculando..." else "Vincular")
            }

            uiState.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
