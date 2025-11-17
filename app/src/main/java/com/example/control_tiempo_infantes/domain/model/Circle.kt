package com.example.control_tiempo_infantes.domain.model

data class Circle(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList(),
    val inviteCode: String? = null
)
