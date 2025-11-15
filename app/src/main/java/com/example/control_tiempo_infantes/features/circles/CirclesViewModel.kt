package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.repository.Circle
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CirclesViewModel @Inject constructor(
    private val repo: CircleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _items = MutableStateFlow<List<Circle>>(emptyList())
    val items: StateFlow<List<Circle>> = _items.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun clearMessage() { _message.value = null }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repo.getMyCircles(uid).collectLatest { _items.value = it }
        }
    }

    fun create(name: String, desc: String, onDone: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _loading.value = true
            val r = repo.create(uid, name, desc)
            _loading.value = false
            if (r.isSuccess) {
                _message.value = "Círculo creado"
                onDone()
            } else {
                _message.value = r.exceptionOrNull()?.localizedMessage ?: "Error al crear"
            }
        }
    }
}
