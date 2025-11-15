package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HU-01 – Maneja login/registro sin exigir verificación de email.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    val isLoggedIn: Flow<Boolean> = repo.isLoggedIn

    fun login(email: String, pass: String, onResult: (String?) -> Unit) = viewModelScope.launch {
        val r = repo.login(email, pass)
        onResult(r.exceptionOrNull()?.localizedMessage)
    }

    fun register(name: String?, email: String, pass: String, onMessage: (String) -> Unit) =
        viewModelScope.launch {
            val r = repo.register(email, pass, name)
            onMessage(
                r.exceptionOrNull()?.localizedMessage
                    ?: "Cuenta creada. ¡Bienvenido/a!"

            )
        }

    fun logout() = viewModelScope.launch { repo.logout() }
}
