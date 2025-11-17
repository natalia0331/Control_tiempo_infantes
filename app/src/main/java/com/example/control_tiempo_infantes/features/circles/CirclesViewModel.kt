package com.example.control_tiempo_infantes.features.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.control_tiempo_infantes.domain.model.Circle
import com.example.control_tiempo_infantes.domain.repository.CircleRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CirclesViewModel @Inject constructor(
    private val repo: CircleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _items = MutableStateFlow<List<Circle>>(emptyList())
    val items = _items.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

        }
    }
}
