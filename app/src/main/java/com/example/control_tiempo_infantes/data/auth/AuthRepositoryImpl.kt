package com.example.control_tiempo_infantes.data.auth

import com.example.control_tiempo_infantes.domain.repository.AuthRepository
import com.example.control_tiempo_infantes.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val users: UserRepository
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { a -> trySend(a.currentUser != null) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = res.user ?: error("No user")
        // No exigimos verificación
        users.createIfMissing(user.uid, user.email, user.displayName, role = "supervisor") // default; if user has role set in Firestore, createIfMissing won't overwrite
        Unit
    }

    override suspend fun register(email: String, password: String, displayName: String?, role: String): Result<Unit> =
        runCatching {
            val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = res.user ?: error("No user")

            if (!displayName.isNullOrBlank()) {
                val profile = userProfileChangeRequest { this.displayName = displayName.trim() }
                user.updateProfile(profile).await()
            }
            users.createIfMissing(user.uid, user.email, user.displayName, role)
            Unit
        }

    override suspend fun logout() { auth.signOut() }
}
