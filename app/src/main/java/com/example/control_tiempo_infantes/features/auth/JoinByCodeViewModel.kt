package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinByCodeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class JoinByCodeViewModel @Inject constructor(
    // TODO: aquí más adelante puedes inyectar repositorios para validar el código
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinByCodeUiState())
    val uiState: StateFlow<JoinByCodeUiState> = _uiState

    fun joinWithCode(code: String) {
        if (code.isBlank()) {
            _uiState.value = JoinByCodeUiState(
                error = "Ingresa el código de invitación."
            )
            return
        }

        viewModelScope.launch {
            // Por ahora simulamos éxito. Aquí luego conectas Firebase / backend.
            _uiState.value = JoinByCodeUiState(loading = true)

            // TODO: lógica real de validación del código
            _uiState.value = JoinByCodeUiState(
                loading = false,
                success = true
            )
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
