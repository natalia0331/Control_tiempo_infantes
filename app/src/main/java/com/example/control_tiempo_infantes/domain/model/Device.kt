package com.example.control_tiempo_infantes.domain.model

data class Device(
    val id: String = "",
    val childId: String = "",
    val platform: String = "android",
    val name: String = "",
    val createdAt: Long = 0L,
    val lastSeenAt: Long = 0L
)
