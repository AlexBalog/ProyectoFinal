package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.Likes
import com.example.proyectofinalandroid.Repository.LikesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikesViewModel @Inject constructor(private val repository: LikesRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> get() = _likesCount

    private val _entrenamiento = MutableStateFlow<Entrenamientos?>(null)
    val entrenamiento: StateFlow<Entrenamientos?> get() = _entrenamiento

    private val _likes = MutableStateFlow<List<Likes>?>(emptyList())
    val likes: StateFlow<List<Likes>?> get() = _likes

    fun setUsuarioYEntrenamiento(usuario: Usuarios, entrenamiento: Entrenamientos) {
        _usuario.value = usuario
        _entrenamiento.value = entrenamiento
        verificarLike(usuario._id, entrenamiento._id)
    }


    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> get() = _isLiked

    private fun verificarLike(usuarioId: String, entrenamientoId: String) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.toString() ?: return@launch
                val likes = repository.getAll(token)
                _likes.value = likes ?: emptyList()
                _isLiked.value = likes?.any {
                    it.usuario == usuarioId && it.entrenamiento == entrenamientoId
                } == true
            } catch (e: Exception) {
                _isLiked.value = false
                _errorMessage.value = "Error al verificar like: ${e.message}"
            }
        }
    }


    private fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _likes.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _likes.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _likes.value = emptyList()
            }
        }
    }

    private val _likesSeleccionado = MutableStateFlow<Likes?>(null)
    val likesSeleccionado: StateFlow<Likes?> get() = _likesSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _likesSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun new(like: Likes) {
        viewModelScope.launch {
            try {
                val creado = repository.new(like)
                if (creado != null) {
                    _likesSeleccionado.value = creado
                    _isLiked.value = true
                    _errorMessage.value = null
                    verificarLike(like.usuario, like.entrenamiento)
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun delete(datos: Map<String, String>) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    return@launch
                }

                val eliminar = repository.delete(token = token, datos = datos)
                if (eliminar) {
                    _likesSeleccionado.value = null
                    _isLiked.value = false
                    _errorMessage.value = null
                    verificarLike(datos["usuario"].orEmpty(), datos["entrenamiento"].orEmpty())
                } else {
                    _errorMessage.value = "Error al eliminar el like"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun devolverLikesEntrenamiento(entrenamiento: String, usuario: Usuarios?) {
        viewModelScope.launch {
            try {
                val token = usuario!!.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    return@launch
                }
                Log.d("FalloVM0", entrenamiento)
                val respuesta = repository.getFilter(token, mapOf("entrenamiento" to entrenamiento))
                Log.d("FalloVM1", "${respuesta}")
                if (respuesta != null) {
                    _likesCount.value = respuesta.size
                    Log.d("FalloVM2", "${_likesCount.value}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}