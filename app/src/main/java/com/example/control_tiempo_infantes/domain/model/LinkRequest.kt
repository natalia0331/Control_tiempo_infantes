package com.example.control_tiempo_infantes.domain.model

data class LinkRequest(
    val code: String = "",
    val circleId: String = "",
    val childId: String = "",
    val expiresAt: Long = 0L,
    val used: Boolean = false
)
