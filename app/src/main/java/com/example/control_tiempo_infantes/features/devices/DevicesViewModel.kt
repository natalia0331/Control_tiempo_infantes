package com.example.control_tiempo_infantes.features.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.Device
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DevicesUiState(
    val loading: Boolean = false,
    val devices: List<Device> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState

    fun loadDevices(childId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    loading = true,
                    error = null
                )

                val snap = db.collection("devices")
                    .whereEqualTo("childId", childId)
                    .get()
                    .await()

                val devices = snap.documents.mapNotNull { d ->
                    val id = d.getString("id") ?: d.id
                    val deviceId = d.getString("deviceId") ?: ""
                    val model = d.getString("model") ?: ""
                    val os = d.getString("osVersion") ?: ""   // campo en Firestore
                    val createdAt = d.getLong("createdAt") ?: 0L

                    Device(
                        id = id,
                        childId = childId,
                        circleId = "",               // si aún no manejas esto, puedes dejarlo vacío
                        deviceId = deviceId,
                        model = model,
                        createdAt = createdAt,
                        androidVersion = os,
                        lastSyncAt = 0L
                    )
                }


                _uiState.value = _uiState.value.copy(
                    loading = false,
                    devices = devices
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.message ?: "Error cargando dispositivos."
                )
            }
        }
    }

    fun refresh(childId: String) {
        loadDevices(childId)
    }
}
