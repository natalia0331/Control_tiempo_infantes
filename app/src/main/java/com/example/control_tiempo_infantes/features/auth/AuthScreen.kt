package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(onLoggedIn: () -> Unit, onGoToRegister: () -> Unit, vm: AuthViewModel) {
    val loading by vm.loading.collectAsState()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.login(email, pass) { e -> if (e == null) onLoggedIn() else err = e } }, enabled = !loading) {
            Text("Entrar")
        }
        TextButton(onClick = onGoToRegister) { Text("Crear cuenta") }
        if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error)
    }
}
