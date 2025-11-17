package com.example.control_tiempo_infantes.data.repository

import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : InvitationRepository {

    override suspend fun createInvitation(circleId: String, email: String, type: String): Boolean {
        return try {
            val code = (100000..999999).random().toString()

            val inv = hashMapOf(
                "circleId" to circleId,
                "email" to email,
                "type" to type,
                "code" to code,
                "status" to "pending"
            )

            db.collection("invitations")
                .add(inv)
                .await()

            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getInvitations(circleId: String): List<Invitation> {
        return try {
            db.collection("invitations")
                .whereEqualTo("circleId", circleId)
                .get()
                .await()
                .documents
                .map {
                    Invitation(
                        id = it.id,
                        circleId = it["circleId"].toString(),
                        email = it["email"].toString(),
                        code = it["code"].toString(),
                        typeAccess = it["type"].toString(),
                        status = it["status"].toString()
                    )
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun acceptInvitation(code: String): Boolean {
        return try {
            val query = db.collection("invitations")
                .whereEqualTo("code", code)
                .get()
                .await()

            if (query.isEmpty) return false

            val doc = query.documents.first()

            doc.reference.update("status", "accepted").await()

            true
        } catch (_: Exception) {
            false
        }
    }
}
