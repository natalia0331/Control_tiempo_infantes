package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * HU-01 – Login sin forzar verificación.
 */
@Composable
fun AuthScreen(
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    vm: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var pass  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun handle(msg: String?) { if (msg == null) onLoggedIn() else error = msg }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Acceso", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation()
            )
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)

            Button(
                onClick = { vm.login(email, pass, ::handle) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Iniciar sesión") }

            TextButton(onClick = onGoToRegister, modifier = Modifier.align(Alignment.End)) {
                Text("Crear cuenta", textDecoration = TextDecoration.Underline)
            }
        }
    }
}
