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

/**
 * HU-01. Login/Registro con verificación de email OPCIONAL.
 * (En este caso no bloqueamos el ingreso por verificación ni la usamos.)
 */
class AuthRepositoryImpl @Inject constructor(   // <-- ya no es abstract
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

        users.createIfMissing(
            user.uid,
            user.email ?: email,
            user.displayName
        )

        Unit   // <-- importante para que Result sea Result.success(Unit)
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?
    ): Result<Unit> = runCatching {
        val res = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = res.user ?: error("No user")

        if (!displayName.isNullOrBlank()) {
            val profile = userProfileChangeRequest {
                this.displayName = displayName.trim()
            }
            user.updateProfile(profile).await()
        }

        users.createIfMissing(
            user.uid,
            user.email ?: email,
            user.displayName ?: displayName
        )

        // no hacemos verificación de correo, solo dejamos el usuario creado
        Unit   // <-- para cerrar el runCatching correctamente
    }

    override suspend fun logout() {
        auth.signOut()
    }
}
