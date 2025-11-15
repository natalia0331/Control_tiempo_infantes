package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * HU-01 – Registro sin forzar verificación.
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(name,  { name  = it }, label = { Text("Nombre") })
            OutlinedTextField(email, { email = it }, label = { Text("Correo") })
            OutlinedTextField(pass,  { pass  = it }, label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation())
            OutlinedTextField(pass2, { pass2 = it }, label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation())

            if (msg != null) Text(msg!!, color = MaterialTheme.colorScheme.error)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Cancelar") }
                Button(
                    onClick = {
                        if (pass != pass2) {
                            msg = "Las contraseñas no coinciden."
                        } else {
                            vm.register(name.ifBlank { null }, email, pass) { m ->
                                if (m.startsWith("Cuenta creada")) onRegistered() else msg = m
                            }
                        }
                    }
                ) { Text("Crear") }
            }
        }
    }
}
