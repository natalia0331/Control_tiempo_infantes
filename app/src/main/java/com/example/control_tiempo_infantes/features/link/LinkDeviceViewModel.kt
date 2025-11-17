package com.example.control_tiempo_infantes.features.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LinkDeviceUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LinkDeviceViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkDeviceUiState())
    val uiState: StateFlow<LinkDeviceUiState> = _uiState

    /**
     * Vincula ESTE dispositivo (deviceId) a un infante usando un código temporal.
     *
     * - Un mismo infante puede tener VARIOS dispositivos.
     * - Cada dispositivo queda registrado en:
     *   children/{childId}/devices/{deviceId}
     */
    fun linkDevice(code: String, childId: String, deviceId: String) {
        if (code.isBlank()) {
            _uiState.value = LinkDeviceUiState(error = "Ingresa el código.")
            return
        }

        if (deviceId.isBlank()) {
            _uiState.value = LinkDeviceUiState(error = "No se pudo detectar el dispositivo.")
            return
        }

        if (childId.isBlank()) {
            _uiState.value = LinkDeviceUiState(error = "Infante no válido.")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = LinkDeviceUiState(loading = true)

                val normalizedCode = code.trim()
                val ref = db.collection("link_requests").document(normalizedCode)
                val snap = ref.get().await()

                if (!snap.exists()) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        error = "Código inválido."
                    )
                    return@launch
                }

                val used = snap.getBoolean("used") ?: false
                val expiresAt = snap.getLong("expiresAt") ?: 0L
                val storedChildId = snap.getString("childId")
                val now = System.currentTimeMillis()

                if (used || now > expiresAt || storedChildId != childId) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        error = "Código expirado, usado o no corresponde a este infante."
                    )
                    return@launch
                }

                // Registrar o actualizar el dispositivo para este infante
                val deviceDoc = db.collection("children")
                    .document(childId)
                    .collection("devices")
                    .document(deviceId)

                val deviceData = hashMapOf(
                    "id" to deviceId,
                    "childId" to childId,
                    "platform" to "android",
                    "name" to "", // nombre amigable opcional
                    "createdAt" to now,
                    "lastSeenAt" to now
                )

                deviceDoc.set(deviceData, SetOptions.merge()).await()

                // Marcar el código como usado
                ref.update(
                    mapOf(
                        "used" to true,
                        "usedAt" to now,
                        "deviceId" to deviceId
                    )
                ).await()

                _uiState.value = LinkDeviceUiState(
                    loading = false,
                    success = true
                )
            } catch (e: Exception) {
                _uiState.value = LinkDeviceUiState(
                    loading = false,
                    error = e.message ?: "Error al vincular dispositivo."
                )
            }
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
