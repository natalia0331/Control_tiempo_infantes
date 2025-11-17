package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcceptInvitationViewModel @Inject constructor(
    private val repo: InvitationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result

    fun acceptInvitation(code: String) = viewModelScope.launch {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _result.value = "Debes iniciar sesión para aceptar una invitación"
            return@launch
        }

        val ok = repo.acceptInvitation(code, userId)

        _result.value = if (ok) {
            "Miembro incorporado al círculo"
        } else {
            "Código inválido, expirado o ya utilizado"
        }
    }

    fun clear() {
        _result.value = null
    }
}
