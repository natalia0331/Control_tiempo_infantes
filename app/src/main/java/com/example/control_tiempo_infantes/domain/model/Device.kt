package com.example.control_tiempo_infantes.domain.model

data class Device(
    val id: String = "",
    val childId: String = "",
    val circleId: String = "",
    val deviceId: String = "",
    val model: String = "",
    val createdAt: Long = 0L,
    val androidVersion: String = "",
    val lastSyncAt: Long = 0L
)
