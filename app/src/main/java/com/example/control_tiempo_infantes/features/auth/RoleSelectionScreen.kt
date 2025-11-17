package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(onChooseSupervisor: () -> Unit, onChooseKid: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Button(onClick = onChooseSupervisor, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Soy tutor")
        }
        Button(onClick = onChooseKid, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Soy infante")
        }
    }
}
