package com.example.control_tiempo_infantes.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import com.example.control_tiempo_infantes.data.repository.CircleRepositoryImpl


@Composable
fun JoinByCodeScreen(
    onJoined: () -> Unit,
    userUid: String,
    vm: JoinByCodeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Introduce el código que te dio tu tutor")

        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código") }
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            scope.launch {
                val result = vm.joinByCode(code, userUid)
                message = result
                if (result == "OK") onJoined()
            }
        }) {
            Text("Unirme")
        }

        if (message != null) Text(message!!)
    }
}
