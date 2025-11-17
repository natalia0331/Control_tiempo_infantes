package com.example.control_tiempo_infantes.features.auth

import androidx.lifecycle.ViewModel
import com.example.control_tiempo_infantes.data.repository.CircleRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

@HiltViewModel
class JoinByCodeViewModel @Inject constructor(
    private val circleRepo: CircleRepositoryImpl
) : ViewModel() {

    suspend fun joinByCode(code: String, userUid: String): String {
        return try {
            val snap = FirebaseFirestore.getInstance()
                .collection("circles")
                .whereEqualTo("inviteCode", code.trim().uppercase())
                .get()
                .await()

            if (snap.isEmpty) return "Código inválido"

            val circleId = snap.documents.first().id

            circleRepo.addMember(circleId, userUid)

            FirebaseFirestore.getInstance()
                .collection("users").document(userUid)
                .update("circles", com.google.firebase.firestore.FieldValue.arrayUnion(circleId))
                .await()

            "OK"
        } catch (e: Exception) {
            e.localizedMessage ?: "Error inesperado"
        }
    }
}
