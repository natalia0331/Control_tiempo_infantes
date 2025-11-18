package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.Child
import com.example.control_tiempo_infantes.domain.model.Device
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CircleDetailUiState(
    val loading: Boolean = false,
    val children: List<Child> = emptyList(),
    val devicesByChild: Map<String, List<Device>> = emptyMap(),
    val error: String? = null,
    val childCreated: Boolean = false,
    val lastGeneratedCode: String? = null
)

@HiltViewModel
class CircleDetailViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircleDetailUiState())
    val uiState: StateFlow<CircleDetailUiState> = _uiState

    private var circleId: String? = null

    fun init(circleId: String) {
        if (this.circleId == circleId && _uiState.value.children.isNotEmpty()) return
        this.circleId = circleId
        loadChildrenAndDevices()
    }

    fun refresh() {
        loadChildrenAndDevices()
    }

    private fun loadChildrenAndDevices() {
        val id = circleId ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    loading = true,
                    error = null,
                    childCreated = false
                )

                // 1. Cargar infantes del círculo
                val snap = db.collection("children")
                    .whereEqualTo("circleId", id)
                    .get()
                    .await()

                val children = snap.documents.mapNotNull { doc ->
                    val childId = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val age = (doc.getLong("age") ?: 0L).toInt()

                    Child(
                        id = childId,
                        circleId = id,
                        name = name,
                        age = age
                    )
                }

                // 2. Cargar dispositivos por infante
                val devicesByChild = mutableMapOf<String, List<Device>>()

                for (child in children) {
                    val devSnap = db.collection("devices")
                        .whereEqualTo("childId", child.id)
                        .get()
                        .await()

                    val devices = devSnap.documents.mapNotNull { d ->
                        val devId = d.getString("id") ?: d.id
                        val deviceId = d.getString("deviceId") ?: ""
                        val model = d.getString("model") ?: ""
                        val os = d.getString("androidVersion") ?: ""
                        val createdAt = d.getLong("createdAt") ?: 0L

                        Device(
                            id = devId,
                            childId = child.id,
                            deviceId = deviceId,
                            model = model,
                            androidVersion = os,
                            createdAt = createdAt
                        )
                    }

                    devicesByChild[child.id] = devices
                }

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    children = children,
                    devicesByChild = devicesByChild
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error cargando infantes y dispositivos."
                )
            }
        }
    }

    fun addChild(name: String, ageText: String) {
        val circle = circleId ?: return

        if (name.isBlank() || ageText.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Por favor completa todos los campos."
            )
            return
        }

        val age = ageText.toIntOrNull()
        if (age == null || age <= 0) {
            _uiState.value = _uiState.value.copy(
                error = "La edad debe ser un número válido."
            )
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "Usuario no autenticado."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    loading = true,
                    error = null,
                    childCreated = false
                )

                val doc = db.collection("children").document()
                val id = doc.id

                val data = hashMapOf(
                    "id" to id,
                    "circleId" to circle,
                    "name" to name,
                    "age" to age,
                    "createdBy" to currentUser.uid,
                    "createdAt" to System.currentTimeMillis()
                )

                doc.set(data).await()

                // Recargar todo
                loadChildrenAndDevices()

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    childCreated = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error al registrar infante."
                )
            }
        }
    }

    fun resetChildCreatedFlag() {
        _uiState.value = _uiState.value.copy(childCreated = false)
    }

    fun generateLinkCode(childId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    loading = true,
                    error = null,
                    lastGeneratedCode = null
                )

                val code = (100000..999999).random().toString()
                val now = System.currentTimeMillis()
                val expiresAt = now + 10 * 60_000 // 10 minutos

                val data = hashMapOf(
                    "code" to code,
                    "childId" to childId,
                    "createdAt" to now,
                    "expiresAt" to expiresAt,
                    "used" to false
                )

                db.collection("link_requests")
                    .document(code)
                    .set(data)
                    .await()

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    lastGeneratedCode = code
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error generando código."
                )
            }
        }
    }

    fun clearCode() {
        _uiState.value = _uiState.value.copy(lastGeneratedCode = null)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
