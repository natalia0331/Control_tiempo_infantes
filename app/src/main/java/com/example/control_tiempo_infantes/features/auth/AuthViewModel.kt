package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.UserProfile
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
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
    private val users: UserRepository
) : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    val isLoggedIn: StateFlow<Boolean> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser != null)
        }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, auth.currentUser != null)


    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {

        auth.currentUser?.uid?.let { uid ->
            loadUserProfile(uid)
        }
    }


    fun login(email: String, pass: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                auth.signInWithEmailAndPassword(email, pass).await()
                val uid = auth.currentUser?.uid

                if (uid != null) {
                    loadUserProfile(uid)
                }

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
                val user = res.user ?: throw Exception("No se pudo obtener el usuario.")
                val uid = user.uid


                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "role" to "ADULT",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(data).await()

                // Actualizamos el perfil en memoria
                _profile.value = UserProfile(
                    uid = uid,
                    email = email,
                    displayName = name,
                    role = "ADULT",
                    circleIds = emptyList()
                )

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

    // =========================
    // REGISTRO INFANTE (CHILD)
    // =========================
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

                val res = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = res.user ?: throw Exception("No se pudo obtener el usuario.")
                val uid = user.uid

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "birthDate" to birthDate,
                    "email" to email,
                    "role" to "CHILD",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(data).await()

                _profile.value = UserProfile(
                    uid = uid,
                    email = email,
                    displayName = name,
                    role = "CHILD",
                    circleIds = emptyList()
                )

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

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val snap = db.collection("users").document(uid).get().await()
                if (!snap.exists()) {
                    _profile.value = null
                    return@launch
                }

                val email = snap.getString("email")
                val name = snap.getString("name")
                val role = snap.getString("role") ?: "ADULT"
                val circleIdsAny = snap.get("circleIds") as? List<*>
                val circleIds = circleIdsAny
                    ?.filterIsInstance<String>()
                    ?: emptyList()

                _profile.value = UserProfile(
                    uid = uid,
                    email = email,
                    displayName = name,
                    role = role,
                    circleIds = circleIds
                )
            } catch (e: Exception) {
                _profile.value = null
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        auth.signOut()
        _profile.value = null
    }
}
