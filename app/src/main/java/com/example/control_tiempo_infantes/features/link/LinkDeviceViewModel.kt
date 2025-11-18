package com.example.control_tiempo_infantes.features.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LinkDeviceUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LinkDeviceViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkDeviceUiState())
    val uiState: StateFlow<LinkDeviceUiState> = _uiState

    fun linkDevice(
        code: String,
        deviceId: String,
        model: String,
        androidVersion: String
    ) {
        viewModelScope.launch {
            if (code.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Ingresa un código válido."
                )
                return@launch
            }

            try {
                _uiState.value = LinkDeviceUiState(loading = true)

                val now = System.currentTimeMillis()

                val linkRef = db.collection("link_requests").document(code)
                val linkSnap = linkRef.get().await()

                if (!linkSnap.exists()) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        success = false,
                        error = "Código no encontrado."
                    )
                    return@launch
                }

                val used = linkSnap.getBoolean("used") ?: false
                val expiresAt = linkSnap.getLong("expiresAt") ?: 0L
                val childId = linkSnap.getString("childId") ?: ""

                if (childId.isBlank()) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        success = false,
                        error = "El código no tiene infante asociado."
                    )
                    return@launch
                }

                if (used) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        success = false,
                        error = "Este código ya fue utilizado."
                    )
                    return@launch
                }

                if (expiresAt < now) {
                    _uiState.value = LinkDeviceUiState(
                        loading = false,
                        success = false,
                        error = "El código ha expirado."
                    )
                    return@launch
                }

                val childSnap = db.collection("children")
                    .document(childId)
                    .get()
                    .await()

                val circleId = childSnap.getString("circleId") ?: ""

                val deviceDoc = db.collection("devices").document()
                val deviceData = hashMapOf(
                    "id" to deviceDoc.id,
                    "childId" to childId,
                    "circleId" to circleId,
                    "deviceId" to deviceId,
                    "model" to model,
                    "androidVersion" to androidVersion,
                    "createdAt" to now,
                    "lastSyncAt" to 0L
                )

                db.runBatch { batch ->
                    batch.set(deviceDoc, deviceData)
                    batch.update(
                        linkRef,
                        mapOf(
                            "used" to true,
                            "usedAt" to now
                        )
                    )
                }.await()

                _uiState.value = LinkDeviceUiState(
                    loading = false,
                    success = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = LinkDeviceUiState(
                    loading = false,
                    success = false,
                    error = e.message ?: "Error al vincular el dispositivo."
                )
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
