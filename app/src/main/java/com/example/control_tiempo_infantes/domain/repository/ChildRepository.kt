package com.example.control_tiempo_infantes.domain.repository

import com.example.control_tiempo_infantes.domain.model.Child
import kotlinx.coroutines.flow.Flow

interface ChildRepository {
    fun listByCircle(circleId: String): Flow<List<Child>>
    suspend fun create(circleId: String, name: String, age: Int): Result<String> // returns childId
    suspend fun generateLinkCode(circleId: String, childId: String): Result<String>
    suspend fun validateLinkCode(code: String): Result<Child>
    suspend fun linkDevice(childId: String): Result<Unit>
}
