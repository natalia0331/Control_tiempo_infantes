package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    onSelectSupervisor: () -> Unit,
    onSelectChild: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TopAppBar(
                title = { Text("Selecciona tu rol") }
            )

            Text(
                text = "¿Quién está usando la aplicación?",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onSelectSupervisor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Soy padre / supervisor")
            }

            Button(
                onClick = onSelectChild,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Soy infante")
            }
        }
    }
}
