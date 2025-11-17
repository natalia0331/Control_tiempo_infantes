package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InviteUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val invitations: InvitationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState: StateFlow<InviteUiState> = _uiState

    fun sendInvitation(circleId: String, email: String, type: String) {
        if (email.isBlank()) {
            _uiState.value = InviteUiState(
                error = "Por favor escribe un correo."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = InviteUiState(loading = true)

                // Asumo que tu repositorio devuelve Boolean (true si ok)
                val ok = invitations.createInvitation(circleId, email, type)

                _uiState.value = if (ok) {
                    InviteUiState(
                        loading = false,
                        message = "Invitación enviada a $email."
                    )
                } else {
                    InviteUiState(
                        loading = false,
                        error = "No se pudo enviar la invitación."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = InviteUiState(
                    loading = false,
                    error = e.message ?: "Error al enviar invitación."
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = InviteUiState()
    }
}
