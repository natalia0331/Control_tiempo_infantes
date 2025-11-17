package com.example.control_tiempo_infantes.domain.repository

import com.example.control_tiempo_infantes.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createIfMissing(uid: String, email: String?, displayName: String?, role: String = "ADULT")
    suspend fun get(uid: String): UserProfile?
    fun observe(uid: String): Flow<UserProfile?>
    suspend fun addCircleToUser(uid: String, circleId: String)
}
