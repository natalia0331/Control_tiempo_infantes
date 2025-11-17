package com.example.control_tiempo_infantes.domain.model

data class Invitation(
    val circleId: String = "",
    val email: String = "",
    val type: String = "",
    val code: String = "",
    val status: String = "",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,

    // Info extra para mostrar al infante
    val inviterUid: String? = null,
    val inviterName: String? = null,
    val inviterEmail: String? = null,
    val circleName: String? = null,
    val circleMembersCount: Long? = null,

    // Info del que acepta la invitación
    val acceptedAt: Long? = null,
    val acceptedByUid: String? = null,
    val acceptedByName: String? = null,
    val acceptedByEmail: String? = null
)
