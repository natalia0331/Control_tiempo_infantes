package com.example.control_tiempo_infantes.data.repository

import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.repository.InvitationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InvitationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : InvitationRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val col get() = db.collection("invitations")
    private val circlesCol get() = db.collection("circles")
    private val usersCol get() = db.collection("users")

    override suspend fun createInvitation(circleId: String, email: String, type: String): Boolean {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val now = System.currentTimeMillis()
            val expiresAt = now + 7L * 24L * 60L * 60L * 1000L // 7 días
            val code = (100000..999999).random().toString()

            // Datos del círculo
            val circleSnap = circlesCol.document(circleId).get().await()
            val circleName = circleSnap.getString("name") ?: "Círculo familiar"
            val members = circleSnap.get("members") as? List<*>
            val membersCount = members?.size?.toLong() ?: 0L

            // Datos del usuario que invita (supervisor actual)
            val inviterUid = auth.currentUser?.uid
            val inviterEmail = auth.currentUser?.email
            var inviterName: String? = null

            if (inviterUid != null) {
                val userSnap = usersCol.document(inviterUid).get().await()
                inviterName = userSnap.getString("name") ?: inviterEmail
            }

            val inv = hashMapOf(
                "circleId" to circleId,
                "email" to normalizedEmail,
                "type" to type,            // "child" o "adult"
                "code" to code,
                "status" to "pending",
                "createdAt" to now,
                "expiresAt" to expiresAt,
                "inviterUid" to inviterUid,
                "inviterName" to inviterName,
                "inviterEmail" to inviterEmail,
                "circleName" to circleName,
                "circleMembersCount" to membersCount
            )

            col.add(inv).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getInvitations(circleId: String): List<Invitation> {
        return try {
            val snap = col.whereEqualTo("circleId", circleId).get().await()
            snap.toObjects(Invitation::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getPendingInvitationsForEmail(email: String): List<Invitation> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val snap = col
                .whereEqualTo("email", normalizedEmail)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            snap.toObjects(Invitation::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun acceptInvitation(code: String, userUid: String): Boolean {
        return try {
            // 1. Buscar invitación por código
            val snap = col
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()

            if (snap.isEmpty) return false

            val doc = snap.documents.first()
            val data = doc.data ?: return false

            val status = data["status"] as? String ?: "pending"
            val circleId = data["circleId"] as? String ?: return false
            val expiresAt = (data["expiresAt"] as? Number)?.toLong() ?: 0L
            val type = data["type"] as? String ?: "child"
            val now = System.currentTimeMillis()

            // Validaciones
            if (status != "pending") return false
            if (expiresAt > 0 && expiresAt < now) return false

            // 2. Datos del usuario que acepta (para guardar displayName en la invitación)
            val userSnap = usersCol.document(userUid).get().await()
            val acceptedName = userSnap.getString("name")
                ?: auth.currentUser?.displayName
                ?: auth.currentUser?.email
                ?: "Invitado"
            val acceptedEmail = userSnap.getString("email") ?: auth.currentUser?.email

            // 3. Agregar usuario al array "members" del círculo
            circlesCol.document(circleId)
                .update("members", FieldValue.arrayUnion(userUid))
                .await()

            // 4. Asegurar que el usuario tenga el círculo en su perfil (circleIds)
            usersCol.document(userUid)
                .set(
                    mapOf(
                        "circleIds" to FieldValue.arrayUnion(circleId)
                    ),
                    SetOptions.merge()
                )
                .await()

            // 5. Si la invitación es para INFANTE, creamos un Child en la colección "children"
            if (type == "child") {
                val childDoc = db.collection("children").document()
                val childData = hashMapOf(
                    "id" to childDoc.id,
                    "circleId" to circleId,
                    "name" to acceptedName,
                    "age" to 0, // no lo conocemos aún, pero tu modelo lo exige (Int)
                    "createdBy" to userUid,
                    "createdAt" to now
                )
                childDoc.set(childData).await()
            }

            // 6. Marcar invitación como aceptada y registrar quién la aceptó
            val updateMap = mutableMapOf<String, Any>(
                "status" to "accepted",
                "acceptedAt" to now,
                "acceptedByUid" to userUid
            )

            acceptedName?.let { updateMap["acceptedByName"] = it }
            acceptedEmail?.let { updateMap["acceptedByEmail"] = it }

            doc.reference.update(updateMap).await()

            true
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun rejectInvitation(code: String): Boolean {
        return try {
            val snap = col
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()

            if (snap.isEmpty) return false

            val doc = snap.documents.first()
            val status = doc.getString("status") ?: "pending"
            if (status != "pending") return false

            doc.reference.update(
                mapOf(
                    "status" to "rejected",
                    "rejectedAt" to System.currentTimeMillis()
                )
            ).await()

            true
        } catch (_: Exception) {
            false
        }
    }
}
