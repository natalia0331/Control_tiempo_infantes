package com.example.control_tiempo_infantes.data.repository

import com.example.control_tiempo_infantes.domain.model.Circle
import com.example.control_tiempo_infantes.domain.model.Invitation
import com.example.control_tiempo_infantes.domain.model.UserProfile
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose

class CircleRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CircleRepository {

    private val circlesCol get() = db.collection("circles")
    private val usersCol get() = db.collection("users")
    private val invitesCol get() = db.collection("circle_invitations")
    private val codesCol get() = db.collection("invite_codes")

    override suspend fun createCircle(
        ownerUid: String,
        name: String,
        description: String
    ): Result<String> = runCatching {
        val doc = circlesCol.add(
            mapOf(
                "ownerUid" to ownerUid,
                "name" to name.trim(),
                "description" to description.trim(),
                "members" to listOf(ownerUid),
                "inviteCode" to null
            )
        ).await()
        doc.id
    }

    override fun listMembers(circleId: String): Flow<List<UserProfile>> = callbackFlow {
        val listener = usersCol
            .whereArrayContains("circleIds", circleId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { d ->
                    d.toObject(UserProfile::class.java)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun listInvitations(circleId: String): Flow<List<Invitation>> = callbackFlow {
        val listener = invitesCol
            .whereEqualTo("circleId", circleId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Invitation::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Crear invitación ligada a un círculo (colección circle_invitations).
     * OJO: aquí usamos el nuevo modelo Invitation:
     *  - typeAccess -> se mapea al campo 'type'
     *  - ya no usamos 'id' en el constructor.
     */
    override suspend fun createInvitation(
        circleId: String,
        email: String,
        typeAccess: String
    ): Result<String> {
        return try {
            val code = UUID.randomUUID().toString().substring(0, 8).uppercase()
            val now = System.currentTimeMillis()
            val expiresAt = now + (24 * 60 * 60 * 1000) // 24h

            val inv = Invitation(
                circleId = circleId,
                email = email.trim().lowercase(),
                type = typeAccess,
                status = "pending",
                code = code,
                createdAt = now,
                expiresAt = expiresAt
            )

            invitesCol.document(code).set(inv).await()
            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateInvitation(code: String): Result<Invitation> {
        return try {
            val snap = invitesCol.document(code).get().await()
            if (!snap.exists()) return Result.failure(Exception("Invitación no encontrada"))
            val inv = snap.toObject(Invitation::class.java)!!
            if (inv.expiresAt < System.currentTimeMillis()) {
                return Result.failure(Exception("Invitación expirada"))
            }
            Result.success(inv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptInvitation(code: String, userId: String): Result<Unit> {
        return try {
            val snap = invitesCol.document(code).get().await()
            if (!snap.exists()) return Result.failure(Exception("Invitación no encontrada"))

            val inv = snap.toObject(Invitation::class.java)!!
            if (inv.status != "pending") {
                return Result.failure(Exception("Invitación ya usada"))
            }

            circlesCol.document(inv.circleId)
                .update("members", FieldValue.arrayUnion(userId))
                .await()

            invitesCol.document(code)
                .update("status", "accepted")
                .await()

            usersCol.document(userId)
                .update("circleIds", FieldValue.arrayUnion(inv.circleId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateInviteCode(circleId: String): Result<String> {
        return try {
            val code = UUID.randomUUID().toString().substring(0, 6).uppercase()
            val now = System.currentTimeMillis()
            val exp = now + 10 * 60 * 1000 // 10 minutos

            codesCol.document(code)
                .set(
                    mapOf(
                        "circleId" to circleId,
                        "createdAt" to now,
                        "expiresAt" to exp
                    )
                )
                .await()

            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun useInviteCode(code: String, childId: String): Result<Unit> {
        return try {
            val doc = codesCol.document(code).get().await()
            if (!doc.exists()) return Result.failure(Exception("Código inválido"))

            val expiresAt = doc.getLong("expiresAt") ?: 0L
            if (System.currentTimeMillis() > expiresAt) {
                return Result.failure(Exception("Código expirado"))
            }

            val circleId = doc.getString("circleId")
                ?: return Result.failure(Exception("Código corrupto"))

            circlesCol.document(circleId)
                .update("members", FieldValue.arrayUnion(childId))
                .await()

            usersCol.document(childId)
                .update("circleIds", FieldValue.arrayUnion(circleId))
                .await()

            codesCol.document(code).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMember(circleId: String, userId: String): Boolean {
        return try {
            db.collection("circles")
                .document(circleId)
                .collection("members")
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "joinedAt" to System.currentTimeMillis(),
                        "role" to "child" // o "member" según tu lógica
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
