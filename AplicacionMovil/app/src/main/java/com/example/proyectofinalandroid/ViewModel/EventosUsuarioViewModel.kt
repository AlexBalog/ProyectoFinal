package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State
import com.example.proyectofinalandroid.Model.Eventos
import com.example.proyectofinalandroid.Model.EventosUsuario
import com.example.proyectofinalandroid.Repository.EventosUsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EventosUsuarioViewModel @Inject constructor(private val repository: EventosUsuarioRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _eventosUsuarioLista = MutableStateFlow<List<EventosUsuario>?>(emptyList())
    val eventosUsuarioLista: StateFlow<List<EventosUsuario>?> get() = _eventosUsuarioLista

    private val _eventosUsuario = MutableStateFlow<EventosUsuario?>(null)
    val eventosUsuario: StateFlow<EventosUsuario?> get() = _eventosUsuario


    suspend fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }

    suspend fun update(updatedData: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            _usuario.value?.let { currentUser ->
                val _id = currentUser._id
                val token = currentUser.token ?: return@withContext false
                val success = repository.update(_id, updatedData, token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
                return@withContext success
            } ?: false
        }
    }

    fun new(eventosUsuario: EventosUsuario) {
        viewModelScope.launch {
            try {
                val creado = repository.new(eventosUsuario)
                if (creado != null) {
                    _eventosUsuario.value = creado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    private val _eventosUsuarioSeleccionado = MutableStateFlow<EventosUsuario?>(null)
    val eventosUsuarioSeleccionado: StateFlow<EventosUsuario?> get() = _eventosUsuarioSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _eventosUsuarioSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }


    suspend fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _eventosUsuarioLista.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _eventosUsuarioLista.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _eventosUsuarioLista.value = emptyList()
            }
        }
    }
}