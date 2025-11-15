package com.example.control_tiempo_infantes.data.circles

import com.example.control_tiempo_infantes.domain.repository.Circle
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * HU-02 – Crear y listar círculos familiares.
 * Colección: "circles"
 * Doc: auto-id con campos (ownerUid, name, description, members)
 */
class CircleRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : CircleRepository {

    private val col get() = db.collection("circles")

    override fun getMyCircles(ownerUid: String): Flow<List<Circle>> = callbackFlow {
        val reg = col.whereEqualTo("ownerUid", ownerUid)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { d ->
                    Circle(
                        id = d.id,
                        ownerUid = d.getString("ownerUid") ?: "",
                        name = d.getString("name") ?: "",
                        description = d.getString("description") ?: "",
                        members = (d.get("members") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun create(ownerUid: String, name: String, description: String): Result<Unit> =
        runCatching {
            val data = mapOf(
                "ownerUid" to ownerUid,
                "name" to name.trim(),
                "description" to description.trim(),
                "members" to listOf(ownerUid)
            )
            col.add(data).await()
            Unit
        }
}
