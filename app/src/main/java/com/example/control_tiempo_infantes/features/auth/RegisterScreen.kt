package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBack: () -> Unit, vm: AuthViewModel) {
    val loading by vm.loading.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("ADULT") } // for now default ADULT; could select CHILD if needed
    var err by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") })
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") })
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            vm.register(email, pass, name, role) { e ->
                if (e == null) onRegistered() else err = e
            }
        }, enabled = !loading) {
            Text("Registrar")
        }
        TextButton(onClick = onBack) { Text("Volver") }
        if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error)
    }
}
