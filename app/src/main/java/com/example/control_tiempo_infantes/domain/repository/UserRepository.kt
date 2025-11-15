package com.example.control_tiempo_infantes.domain.repository

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

interface UserRepository {
    suspend fun createIfMissing(uid: String, email: String, displayName: String?)
    suspend fun getProfile(uid: String): UserProfile?
}
