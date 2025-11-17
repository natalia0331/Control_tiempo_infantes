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

/**
 * Home genérica que dibuja un dashboard distinto según si es infante o adulto.
 *
 * @param displayName Nombre del usuario (para el saludo)
 * @param isChild true = infante, false = adulto/supervisor
 */
@Composable
fun HomeScreen(
    displayName: String,
    isChild: Boolean,
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
        // Saludo
        Text(
            text = "Hola, $displayName",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        Text(
            text = if (isChild)
                "Este es tu espacio para ver tus círculos, tu tiempo de pantalla y tu mascota."
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
                onLogout = onLogout
            )
        }
    }
}

/**
 * Dashboard para ADULTO / SUPERVISOR
 *
 * - Ver mis círculos familiares
 * - Ver tiempos de pantalla
 * - Establecer metas de uso
 */
@Composable
private fun AdultDashboard(
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit
) {
    // Tarjeta de acciones principales
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

            // Si más adelante tienes una pantalla para tiempos de pantalla globales, navegas desde aquí
            OutlinedButton(
                onClick = { /* TODO: Navegar a pantalla de tiempos */ },
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

    // Solo un poco de espacio antes del botón de logout
    Spacer(modifier = Modifier.height(24.dp))

    // Cerrar sesión
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cerrar sesión")
    }
}

/**
 * Dashboard para INFANTE
 *
 * - Ver círculos familiares (solo lectura)
 * - Mi tiempo de pantalla
 * - Mis metas
 * - Mi mascota
 */
@Composable
private fun ChildDashboard(
    onOpenCircles: () -> Unit,
    onOpenInvitations: () -> Unit,
    onLogout: () -> Unit
) {
    // Círculos (solo ver, sin administrar)
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
                text = "Mis círculos familiares",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onOpenCircles,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis círculos")
            }

            // Texto explicativo
            Text(
                text = "Aquí ves los círculos a los que perteneces. Los adultos son quienes los administran.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

    // Tarjeta de tiempo y metas
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
                text = "Mi uso de pantalla",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = { /* TODO: Navegar a pantalla "Mi tiempo de pantalla" */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mi tiempo de pantalla")
            }

            OutlinedButton(
                onClick = { /* TODO: Navegar a pantalla "Mis metas" */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mis metas")
            }
        }
    }

    // Tarjeta de mascota
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
                text = "Mi mascota",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Tu mascota crece y se pone feliz cuando cumples tus metas de uso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Button(
                onClick = { /* TODO: Navegar a pantalla de mascota */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mi mascota")
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
