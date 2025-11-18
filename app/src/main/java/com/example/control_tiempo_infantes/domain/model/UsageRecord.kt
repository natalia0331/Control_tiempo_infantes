package com.example.control_tiempo_infantes.domain.model

data class UsageRecord(
    val id: String = "",
    val childId: String = "",
    val deviceId: String = "",
    val appPackage: String = "",
    val appName: String = "",
    val date: String = "",          // "yyyy-MM-dd"
    val totalMinutes: Int = 0,
    val lastUpdatedAt: Long = 0L
)


data class AppUsageUi(
    val appPackage: String,
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
