package com.example.control_tiempo_infantes.features.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUsageUi(
    val appName: String,
    val minutes: Long
)

data class DeviceUsageUi(
    val deviceLabel: String,
    val totalMinutes: Long,
    val perApp: List<AppUsageUi>
)

data class ScreenTimeUiState(
    val loading: Boolean = false,
    val devices: List<DeviceUsageUi> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(

) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = ScreenTimeUiState(
                loading = false,
                devices = mockDevices()
            )
        }
    }

    fun refresh() = load()

    private fun mockDevices(): List<DeviceUsageUi> {
        return listOf(
            DeviceUsageUi(
                deviceLabel = "Tablet Aurora (A123)",
                totalMinutes = 120,
                perApp = listOf(
                    AppUsageUi("YouTube Kids", 45),
                    AppUsageUi("Juego de puzzles", 30),
                    AppUsageUi("Netflix", 25),
                    AppUsageUi("Otros", 20)
                )
            ),
            DeviceUsageUi(
                deviceLabel = "Celular Aurora (B987)",
                totalMinutes = 75,
                perApp = listOf(
                    AppUsageUi("WhatsApp", 20),
                    AppUsageUi("YouTube", 35),
                    AppUsageUi("Otros", 20)
                )
            )
        )
    }
}
