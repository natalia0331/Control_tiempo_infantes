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
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AppUsageUi(
    val appName: String,
    val totalMinutes: Int
)

data class DeviceUsageItem(
    val deviceId: String,
    val model: String,
    val totalMinutes: Int,
    val apps: List<AppUsageUi>
)

data class ScreenTimeUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val dateLabel: String = "",
    val devices: List<DeviceUsageItem> = emptyList()
)

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun loadToday() {
        val today = dateFormat.format(Date())
        loadForDate(today)
    }

    fun refresh() {
        loadToday()
    }

    private fun loadForDate(day: String) {
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

                val groupedByDevice = snapshot.documents.groupBy {
                    it.getString("deviceId") ?: "unknown-device"
                }

                val deviceItems = groupedByDevice.map { (deviceId, docs) ->

                    val model = docs.firstOrNull()?.getString("model") ?: "Dispositivo"

                    val appsGrouped = docs.groupBy {
                        it.getString("appName") ?: "App desconocida"
                    }.map { (appName, appDocs) ->
                        val totalMinutes = appDocs.sumOf {
                            it.getLong("totalMinutes")?.toInt() ?: 0
                        }
                        AppUsageUi(
                            appName = appName,
                            totalMinutes = totalMinutes
                        )
                    }

                    val totalMinutesDevice = appsGrouped.sumOf { it.totalMinutes }

                    DeviceUsageItem(
                        deviceId = deviceId,
                        model = model,
                        totalMinutes = totalMinutesDevice,
                        apps = appsGrouped.sortedByDescending { it.totalMinutes }
                    )
                }

                _uiState.value = ScreenTimeUiState(
                    loading = false,
                    error = null,
                    dateLabel = "Hoy ($day)",
                    devices = deviceItems.sortedByDescending { it.totalMinutes }
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Error cargando tiempos: ${e.message}"
                )
            }
        }
    }
}
