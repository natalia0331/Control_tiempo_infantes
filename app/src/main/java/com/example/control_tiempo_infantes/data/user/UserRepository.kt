package com.example.control_tiempo_infantes.data.user

import com.example.control_tiempo_infantes.domain.model.UserProfile
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    private val col get() = db.collection("users")

    override suspend fun createIfMissing(uid: String, email: String?, displayName: String?, role: String) {
        val ref = col.document(uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            val obj = mapOf(
                "uid" to uid,
                "email" to email,
                "displayName" to displayName,
                "role" to role,
                "circleIds" to listOf<String>()
            )
            ref.set(obj).await()
        }
    }

    override suspend fun get(uid: String): UserProfile? {
        val snap = col.document(uid).get().await()
        if (!snap.exists()) return null
        return UserProfile(
            uid = uid,
            email = snap.getString("email"),
            displayName = snap.getString("displayName"),
            role = snap.getString("role") ?: "ADULT",
            circleIds = (snap.get("circleIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )
    }

    override fun observe(uid: String): Flow<UserProfile?> = callbackFlow {
        val reg = col.document(uid).addSnapshotListener { snap, err ->
            if (err != null) { trySend(null); return@addSnapshotListener }
            if (snap == null || !snap.exists()) { trySend(null); return@addSnapshotListener }
            val up = UserProfile(
                uid = uid,
                email = snap.getString("email"),
                displayName = snap.getString("displayName"),
                role = snap.getString("role") ?: "ADULT",
                circleIds = (snap.get("circleIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
            trySend(up)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun addCircleToUser(uid: String, circleId: String) {
        col.document(uid).update("circleIds", com.google.firebase.firestore.FieldValue.arrayUnion(circleId)).await()
    }
}
