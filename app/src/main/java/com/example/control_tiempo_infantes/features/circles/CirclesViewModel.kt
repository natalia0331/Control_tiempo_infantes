package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.Circle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CirclesUiState(
    val loading: Boolean = false,
    val circles: List<Circle> = emptyList(),
    val isChild: Boolean = false,          // 👈 nuevo
    val error: String? = null
)

@HiltViewModel
class CirclesViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CirclesUiState())
    val uiState: StateFlow<CirclesUiState> = _uiState

    init {
        loadCircles()
    }

    fun loadCircles() {
        val user = auth.currentUser ?: run {
            _uiState.value = CirclesUiState(
                loading = false,
                circles = emptyList(),
                isChild = false,
                error = "Usuario no autenticado."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true, error = null)

                // Leemos el perfil del usuario para saber su rol y sus círculos
                val userSnap = db.collection("users").document(user.uid).get().await()
                val roleRaw = userSnap.getString("role") ?: "ADULT"
                val role = roleRaw.uppercase()

                if (role == "CHILD") {
                    // Para infante: sus círculos vienen de la lista circleIds
                    val circleIdsAny = userSnap.get("circleIds") as? List<*>
                    val circleIds = circleIdsAny
                        ?.filterIsInstance<String>()
                        ?: emptyList()

                    if (circleIds.isEmpty()) {
                        _uiState.value = CirclesUiState(
                            loading = false,
                            circles = emptyList(),
                            isChild = true,
                            error = null
                        )
                        return@launch
                    }

                    // Firestore whereIn máx 10 elementos, asumimos pocos círculos
                    val snap = db.collection("circles")
                        .whereIn("id", circleIds)
                        .get()
                        .await()

                    val list = snap.toObjects(Circle::class.java)

                    _uiState.value = CirclesUiState(
                        loading = false,
                        circles = list,
                        isChild = true,
                        error = null
                    )
                } else {
                    // Adulto / supervisor: círculos que él es owner
                    val snap = db.collection("circles")
                        .whereEqualTo("ownerId", user.uid)
                        .get()
                        .await()

                    val list = snap.toObjects(Circle::class.java)

                    _uiState.value = CirclesUiState(
                        loading = false,
                        circles = list,
                        isChild = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CirclesUiState(
                    loading = false,
                    circles = emptyList(),
                    isChild = _uiState.value.isChild,
                    error = e.message ?: "Error cargando círculos."
                )
            }
        }
    }

    fun createCircle(name: String, description: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "El nombre del círculo es obligatorio."
            )
            return
        }

        val user = auth.currentUser ?: run {
            _uiState.value = _uiState.value.copy(
                error = "Usuario no autenticado."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(loading = true, error = null)

                // Verificamos rol: los infantes NO pueden crear círculos
                val userSnap = db.collection("users").document(user.uid).get().await()
                val roleRaw = userSnap.getString("role") ?: "ADULT"
                val role = roleRaw.uppercase()

                if (role == "CHILD") {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Los infantes no pueden crear círculos familiares."
                    )
                    return@launch
                }

                val doc = db.collection("circles").document()
                val id = doc.id

                val data = hashMapOf(
                    "id" to id,
                    "name" to name,
                    "description" to description,
                    "ownerId" to user.uid,
                    "createdAt" to System.currentTimeMillis()
                )

                doc.set(data).await()

                loadCircles()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error creando círculo."
                )
            }
        }
    }
}
