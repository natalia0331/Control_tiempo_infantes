package com.example.control_tiempo_infantes.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onOpenCircles,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis círculos familiares (supervisor)")
        }

        Button(
            onClick = onOpenInvitations,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis invitaciones (si eres infante)")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}
