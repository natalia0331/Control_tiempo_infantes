package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildInvitationsUiState(
    val loading: Boolean = false,
    val invitations: List<Invitation> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ChildInvitationsViewModel @Inject constructor(
    private val repo: InvitationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildInvitationsUiState())
    val uiState: StateFlow<ChildInvitationsUiState> = _uiState

    fun loadInvitations() {
        viewModelScope.launch {
            val user = auth.currentUser
            val email = user?.email
            if (email.isNullOrBlank()) {
                _uiState.value = ChildInvitationsUiState(
                    loading = false,
                    invitations = emptyList(),
                    error = "Debes iniciar sesión con un correo válido."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                loading = true,
                error = null,
                message = null
            )

            val list = repo.getPendingInvitationsForEmail(email)

            _uiState.value = _uiState.value.copy(
                loading = false,
                invitations = list,
                message = if (list.isNotEmpty()) "Tienes invitaciones pendientes" else null
            )
        }
    }

    fun acceptInvitation(invitation: Invitation) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            val email = auth.currentUser?.email
            if (userId == null || email.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para aceptar invitaciones."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                loading = true,
                error = null,
                message = null
            )

            val ok = repo.acceptInvitation(invitation.code, userId)

            if (ok) {
                val list = repo.getPendingInvitationsForEmail(email)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    invitations = list,
                    message = "Invitación aceptada. Ahora perteneces al círculo."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "No se pudo aceptar la invitación (quizá ya fue usada o expiró)."
                )
            }
        }
    }

    fun rejectInvitation(invitation: Invitation) {
        viewModelScope.launch {
            val email = auth.currentUser?.email
            if (email.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para gestionar invitaciones."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                loading = true,
                error = null,
                message = null
            )

            val ok = repo.rejectInvitation(invitation.code)

            if (ok) {
                val list = repo.getPendingInvitationsForEmail(email)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    invitations = list,
                    message = "Invitación rechazada."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "No se pudo rechazar la invitación."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
