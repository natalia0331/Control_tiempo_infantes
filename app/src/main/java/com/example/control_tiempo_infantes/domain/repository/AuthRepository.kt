package com.example.control_tiempo_infantes.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, displayName: String?, role: String = "supervisor"): Result<Unit>
    suspend fun logout()
}
