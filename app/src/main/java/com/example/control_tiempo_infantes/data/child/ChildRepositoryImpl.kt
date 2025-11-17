package com.example.control_tiempo_infantes.data.child

import com.example.control_tiempo_infantes.domain.model.Child
import com.example.control_tiempo_infantes.domain.repository.ChildRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose


class ChildRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ChildRepository {

    private val col get() = db.collection("children")

    override fun listByCircle(circleId: String): Flow<List<Child>> = callbackFlow {
        val q = col.whereEqualTo("circleId", circleId)
        val reg = q.addSnapshotListener { snap, _ ->
            val list = snap?.toObjects(Child::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun create(circleId: String, name: String, age: Int): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val child = Child(id = id, circleId = circleId, name = name, age = age)
            col.document(id).set(child).await()
            Result.success(id)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun generateLinkCode(circleId: String, childId: String): Result<String> {
        return try {
            val code = (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("")
            val exp = System.currentTimeMillis() + 10 * 60 * 1000 // 10 min
            col.document(childId).update(mapOf("linkCode" to code, "linkCodeExpiration" to exp)).await()
            Result.success(code)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun validateLinkCode(code: String): Result<Child> {
        return try {
            val q = col.whereEqualTo("linkCode", code).get().await()
            if (q.isEmpty) return Result.failure(Exception("Código inválido"))
            val child = q.documents[0].toObject(Child::class.java)!!
            val expires = child.linkCodeExpiration ?: 0L
            if (expires < System.currentTimeMillis()) return Result.failure(Exception("Código expirado"))
            Result.success(child)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun linkDevice(childId: String): Result<Unit> {
        return try {
            col.document(childId).update(mapOf("deviceLinked" to true, "linkCode" to null, "linkCodeExpiration" to null)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
