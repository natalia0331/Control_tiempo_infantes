package com.example.control_tiempo_infantes.features.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// =======================
// MODELOS QUE LA UI ESPERA
// =======================

data class AppUsageItem(
    val appName: String,
    val totalMinutes: Long
)

data class DeviceUsageItem(
    val deviceId: String,
    val model: String,
    val totalMinutes: Long,
    val apps: List<AppUsageItem>
)

data class ScreenTimeUiState(
    val loading: Boolean = false,
    val dateLabel: String = "",
    val devices: List<DeviceUsageItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // =======================
    // CARGAR USO DE HOY
    // =======================
    fun loadToday() {
        load(dateFormat.format(Date()))
    }

    fun refresh() {
        loadToday()
    }

    // =======================
    // CARGAR POR FECHA
    // =======================
    private fun load(day: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    loading = true,
                    error = null
                )

                val snapshot = db.collection("device_usage")
                    .whereEqualTo("date", day)
                    .get()
                    .await()

                if (snapshot.isEmpty) {
                    _uiState.value = ScreenTimeUiState(
                        loading = false,
                        dateLabel = "Hoy ($day)",
                        devices = emptyList()
                    )
                    return@launch
                }

                // Agrupar por dispositivo
                val groupedByDevice = snapshot.documents.groupBy {
                    it.getString("deviceId") ?: "unknown"
                }

                val deviceItems = groupedByDevice.map { (deviceId, docs) ->

                    val model = docs.first().getString("model") ?: "Dispositivo"

                    val appsGrouped = docs.groupBy {
                        it.getString("appName") ?: "App desconocida"
                    }.map { (appName, appDocs) ->
                        AppUsageItem(
                            appName = appName,
                            totalMinutes = appDocs.sumOf {
                                it.getLong("totalMinutes") ?: 0L
                            }
                        )
                    }

                    val totalMinutes = appsGrouped.sumOf { it.totalMinutes }

                    DeviceUsageItem(
                        deviceId = deviceId,
                        model = model,
                        totalMinutes = totalMinutes,
                        apps = appsGrouped.sortedByDescending { it.totalMinutes }
                    )
                }

                _uiState.value = ScreenTimeUiState(
                    loading = false,
                    dateLabel = "Hoy ($day)",
                    devices = deviceItems.sortedByDescending { it.totalMinutes }
                )

            } catch (e: Exception) {
                _uiState.value = ScreenTimeUiState(
                    loading = false,
                    error = "Error cargando tiempos: ${e.message}"
                )
            }
        }
    }
}
