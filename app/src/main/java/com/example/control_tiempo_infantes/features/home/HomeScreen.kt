package com.example.control_tiempo_infantes.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    displayName: String,
    isChild: Boolean,
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onOpenScreenTime: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saludo
        Text(
            text = "Hola, $displayName",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        Text(
            text = if (isChild)
                "Este es tu espacio para ver tu mascota, tus círculos y tu tiempo de pantalla."
            else
                "Administra tus círculos familiares, metas y tiempo de pantalla de los infantes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isChild) {
            ChildDashboard(
                onOpenCircles = onOpenCircles,
                onOpenInvitations = onOpenInvitations,
                onLogout = onLogout
            )
        } else {
            AdultDashboard(
                onOpenCircles = onOpenCircles,
                onOpenInvitations = onOpenInvitations,
                onOpenScreenTime = onOpenScreenTime,
                onLogout = onLogout
            )
        }
    }
}

/**
 * Dashboard para ADULTO / SUPERVISOR
 */
@Composable
private fun AdultDashboard(
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onOpenScreenTime: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Gestión familiar",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onOpenCircles,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis círculos familiares")
            }

            OutlinedButton(
                onClick = onOpenScreenTime,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver tiempos de pantalla")
            }

            OutlinedButton(
                onClick = { /* TODO: Navegar a pantalla de metas de uso */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Establecer metas de uso")
            }
        }
    }

    // Tarjeta secundaria: invitaciones / control de accesos
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Invitaciones y accesos",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedButton(
                onClick = onOpenInvitations,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver invitaciones para infantes")
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cerrar sesión")
    }
}
@Composable
private fun ChildDashboard(
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tu mascota digital",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Aquí pronto verás a tu mascota. " +
                        "Ella se pondrá más feliz cuando cumplas tus metas de uso de pantalla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "(Espacio reservado para la mascota)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mi espacio",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onOpenCircles,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis círculos familiares")
            }

            OutlinedButton(
                onClick = onOpenInvitations,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis invitaciones")
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cerrar sesión")
    }
}
