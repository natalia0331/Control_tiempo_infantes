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
