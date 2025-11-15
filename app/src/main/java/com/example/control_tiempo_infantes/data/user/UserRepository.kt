package com.example.control_tiempo_infantes.data.user

import com.example.control_tiempo_infantes.domain.repository.UserProfile
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    private fun users() = db.collection("users")

    override suspend fun createIfMissing(uid: String, email: String, displayName: String?) {
        val doc = users().document(uid).get().await()
        if (!doc.exists()) {
            val payload = mapOf(
                "uid" to uid,
                "email" to email,
                "displayName" to displayName,
                "createdAt" to Timestamp.now(),
                "settings" to mapOf("alertsEnabled" to true),
                "circles" to emptyList<String>()
            )
            users().document(uid).set(payload).await()
        }
    }

    override suspend fun getProfile(uid: String): UserProfile? {
        val snap = users().document(uid).get().await()
        return if (snap.exists()) {
            UserProfile(
                uid = snap.getString("uid") ?: uid,
                email = snap.getString("email") ?: "",
                displayName = snap.getString("displayName"),
                createdAt = snap.getTimestamp("createdAt") ?: Timestamp.now()
            )
        } else null
    }
}
