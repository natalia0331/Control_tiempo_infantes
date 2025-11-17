package com.example.control_tiempo_infantes.domain.repository

import com.example.control_tiempo_infantes.domain.model.Invitation

interface InvitationRepository {
    suspend fun createInvitation(circleId: String, email: String, type: String): Boolean
    suspend fun getInvitations(circleId: String): List<Invitation>
    suspend fun acceptInvitation(code: String): Boolean
}
