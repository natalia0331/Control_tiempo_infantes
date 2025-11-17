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
        // Si quieres usar el usuario logueado más adelante, ya tienes auth
        val userId = auth.currentUser?.uid
        // De momento no se usa userId en InvitationRepositoryImpl, pero lo puedes aprovechar después

        val ok = repo.acceptInvitation(code)

        _result.value = if (ok) {
            "Miembro incorporado al círculo"
        } else {
            "Código inválido o ya utilizado"
        }
    }

    fun clear() { _result.value = null }
}
