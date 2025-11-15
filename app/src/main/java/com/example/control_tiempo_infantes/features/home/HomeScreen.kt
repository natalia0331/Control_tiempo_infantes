package com.example.control_tiempo_infantes.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun HomeScreen(
    onOpenCircles: () -> Unit,
    onLogout: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Home", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.size(16.dp))
            Button(onClick = onOpenCircles) { Text("Círculos familiares") }
            Spacer(Modifier.size(12.dp))
            OutlinedButton(onClick = onLogout) { Text("Cerrar sesión") }
        }
    }
}
