package com.example.control_tiempo_infantes.data.repository

import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : InvitationRepository {

    private val col get() = db.collection("invitations")

    override suspend fun createInvitation(circleId: String, email: String, type: String): Boolean {
        return try {
            val code = (100000..999999).random().toString()
            val now = System.currentTimeMillis()
            val expiresAt = now + 7L * 24L * 60L * 60L * 1000L // 7 días

            val inv = hashMapOf(
                "circleId" to circleId,
                "email" to email,
                "type" to type,            // "child" o "adult"
                "code" to code,
                "status" to "pending",
                "createdAt" to now,
                "expiresAt" to expiresAt
            )

            col.add(inv).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getInvitations(circleId: String): List<Invitation> {
        return try {
            val snap = col.whereEqualTo("circleId", circleId).get().await()
            snap.toObjects(Invitation::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun acceptInvitation(code: String): Boolean {
        return try {
            // 1. Buscar la invitación por código
            val snap = col
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()

            if (snap.isEmpty) return false

            val doc = snap.documents.first()
            val status = doc.getString("status") ?: "pending"
            val expiresAt = doc.getLong("expiresAt") ?: 0L
            val now = System.currentTimeMillis()

            // 2. Validar estado y expiración
            if (status != "pending") return false
            if (expiresAt < now) return false

            // 3. Marcar como aceptada
            doc.reference.update(
                mapOf(
                    "status" to "accepted",
                    "acceptedAt" to now
                )
            ).await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
