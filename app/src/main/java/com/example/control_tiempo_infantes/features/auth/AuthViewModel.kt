package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val users: UserRepository // por si ya lo usas en otros lados, lo dejamos
) : ViewModel() {

    // Estado de sesión
    val isLoggedIn: StateFlow<Boolean> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser != null)
        }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, auth.currentUser != null)

    // Loading y error genéricos para pantallas de auth
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LOGIN (supervisor o infante: FirebaseAuth no distingue rol)
    fun login(email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                auth.signInWithEmailAndPassword(email, pass).await()

                _loading.value = false
                onResult(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Error al iniciar sesión."
                _loading.value = false
                _error.value = msg
                onResult(msg)
            }
        }
    }

    // REGISTRO SUPERVISOR (ya existente si tu RegisterScreen la usa)
    fun register(
        name: String,
        email: String,
        pass: String,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || pass.isBlank()) {
                val msg = "Completa todos los campos."
                _error.value = msg
                onResult(msg)
                return@launch
            }

            try {
                _loading.value = true
                _error.value = null

                val res = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = res.user?.uid ?: throw Exception("No se pudo obtener el usuario.")

                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to "supervisor",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(data).await()

                _loading.value = false
                onResult(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Error al registrar supervisor."
                _loading.value = false
                _error.value = msg
                onResult(msg)
            }
        }
    }

    // REGISTRO INFANTE
    fun registerChild(
        name: String,
        birthDate: String,
        email: String,
        pass: String,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || birthDate.isBlank() || email.isBlank() || pass.isBlank()) {
                val msg = "Completa todos los campos."
                _error.value = msg
                onResult(msg)
                return@launch
            }

            try {
                _loading.value = true
                _error.value = null

                // Crear usuario en FirebaseAuth
                val res = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = res.user?.uid ?: throw Exception("No se pudo obtener el usuario.")

                // Guardar perfil en colección "users"
                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "birthDate" to birthDate,
                    "email" to email,
                    "role" to "child",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(data).await()

                _loading.value = false
                onResult(null)
            } catch (e: Exception) {
                val msg = e.message ?: "Error al registrar infante."
                _loading.value = false
                _error.value = msg
                onResult(msg)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        auth.signOut()
    }
}
