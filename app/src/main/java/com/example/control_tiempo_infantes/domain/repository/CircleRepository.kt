package com.example.control_tiempo_infantes.domain.repository

import com.example.control_tiempo_infantes.domain.model.Circle
import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface CircleRepository {
    suspend fun createCircle(ownerUid: String, name: String, description: String): Result<String>
    fun listMembers(circleId: String): Flow<List<UserProfile>>
    fun listInvitations(circleId: String): Flow<List<Invitation>>

    // invitaciones HU-02.2
    suspend fun createInvitation(circleId: String, email: String, typeAccess: String): Result<String>
    suspend fun validateInvitation(code: String): Result<Invitation>
    suspend fun acceptInvitation(code: String, userId: String): Result<Unit>

    // HU-03: invite codes para vincular dispositivo
    suspend fun generateInviteCode(circleId: String): Result<String>
    suspend fun useInviteCode(code: String, childId: String): Result<Unit>
}
