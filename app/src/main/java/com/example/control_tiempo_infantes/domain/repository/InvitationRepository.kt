package com.example.control_tiempo_infantes.domain.repository

import com.example.control_tiempo_infantes.domain.model.Invitation

interface InvitationRepository {

    suspend fun createInvitation(
        circleId: String,
        email: String,
        type: String
    ): Boolean

    suspend fun getInvitations(circleId: String): List<Invitation>

    // Invitaciones pendientes para un email (infante)
    suspend fun getPendingInvitationsForEmail(email: String): List<Invitation>

    // Aceptar invitación y agregar al usuario al círculo
    suspend fun acceptInvitation(code: String, userUid: String): Boolean

    // Rechazar invitación (solo cambia estado)
    suspend fun rejectInvitation(code: String): Boolean
}
