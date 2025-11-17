package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await





@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val users: UserRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { a -> trySend(a.currentUser != null) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, auth.currentUser != null)

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    fun login(email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val u = auth.currentUser!!
                // ensure profile exists
                users.createIfMissing(u.uid, u.email, u.displayName, "ADULT")
                onResult(null)
            } catch (e: Exception) { onResult(e.localizedMessage) } finally { _loading.value = false }
        }
    }

    fun register(email: String, pass: String, name: String?, role: String = "ADULT", onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val res = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val u = res.user ?: throw Exception("No user")
                if (!name.isNullOrBlank()) {
                    val pr = com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                    u.updateProfile(pr).await()
                }
                users.createIfMissing(u.uid, u.email, u.displayName, role)
                onResult(null)
            } catch (e: Exception) { onResult(e.localizedMessage) } finally { _loading.value = false }
        }
    }

    fun logout() { auth.signOut() }
}
