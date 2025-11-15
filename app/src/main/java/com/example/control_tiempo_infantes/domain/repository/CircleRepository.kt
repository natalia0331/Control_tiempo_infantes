package com.example.control_tiempo_infantes.domain.repository

import kotlinx.coroutines.flow.Flow

data class Circle(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList()
)

interface CircleRepository {
    fun getMyCircles(ownerUid: String): Flow<List<Circle>>
    suspend fun create(ownerUid: String, name: String, description: String): Result<Unit>
}
