package com.example.control_tiempo_infantes.domain.model

data class Invitation(
    val id: String = "",
    val circleId: String = "",
    val email: String = "",
    val typeAccess: String = "CHILD", // "CHILD" | "ADULT"
    val status: String = "pending", // pending | accepted | expired
    val code: String = "",
    val expiresAt: Long = 0L
)
