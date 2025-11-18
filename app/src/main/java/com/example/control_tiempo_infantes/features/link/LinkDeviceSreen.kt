package com.example.control_tiempo_infantes.features.link

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDeviceScreen(
    vm: LinkDeviceViewModel,
    childId: String,
    deviceId: String,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    var code by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(ctx, "Dispositivo vinculado correctamente", Toast.LENGTH_SHORT).show()
            vm.consumeSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Vincular dispositivo") },
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
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código de vinculación") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val model = "${Build.MANUFACTURER} ${Build.MODEL}"
                    val androidVersion = Build.VERSION.RELEASE ?: "unknown"

                    vm.linkDevice(
                        code = code.trim(),
                        deviceId = deviceId,
                        model = model,
                        androidVersion = androidVersion
                    )
                },
                enabled = code.isNotBlank() && !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (state.loading) "Vinculando..." else "Vincular dispositivo")
            }

            state.error?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Ingresa el código que ves en la pantalla del adulto para vincular este dispositivo.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
