package com.example.proyectofinalandroid.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Repository.EntrenamientosRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntrenamientosViewModel @Inject constructor(private val repository: EntrenamientosRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _entrenamientos = MutableStateFlow<List<Entrenamientos>?>(null)
    val entrenamientos: StateFlow<List<Entrenamientos>?> get() = _entrenamientos

    private fun getAllEntrenamientos() {
        viewModelScope.launch {
            try {
                _usuario.value?.let { currentUser ->
                    val token = currentUser.token ?: return@launch
                    val lista = repository.getAllEntrenamientos(token)
                    if (lista != null) {
                        _entrenamientos.value = lista
                    } else {
                        _entrenamientos.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _entrenamientos.value = emptyList()
            }
        }
    }

    fun updateEntrenamiento(_id: String, updatedData: Map<String, String>) {
        viewModelScope.launch {
            _usuario.value?.let { currentUser ->
                val token = currentUser.token ?: return@launch // Si no hay token, no hacemos nada
                val success = repository.updateEntrenamiento(_id, updatedData, token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
            }
        }
    }
}