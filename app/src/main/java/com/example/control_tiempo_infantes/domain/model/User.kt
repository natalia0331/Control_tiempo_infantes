package com.example.control_tiempo_infantes.domain.model

data class UserProfile(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val role: String = "ADULT", // "ADULT" | "CHILD"
    val circleIds: List<String> = emptyList()
)
