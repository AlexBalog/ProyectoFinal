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

    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    private val _entrenamiento = MutableStateFlow<Entrenamientos?>(null)
    val entrenamiento: StateFlow<Entrenamientos?> get() = _entrenamiento

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> get() = _likesCount

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> get() = _isLiked

    private val _likes = MutableStateFlow<List<Likes>?>(emptyList())
    val likes: StateFlow<List<Likes>?> get() = _likes

    // Variables para evitar múltiples llamadas
    private var currentUsuarioId: String? = null
    private var currentEntrenamientoId: String? = null

    fun setUsuarioYEntrenamiento(usuario: Usuarios, entrenamiento: Entrenamientos) {
        // Solo actualizar si realmente ha cambiado
        if (currentUsuarioId != usuario._id || currentEntrenamientoId != entrenamiento._id) {
            currentUsuarioId = usuario._id
            currentEntrenamientoId = entrenamiento._id

            _usuario.value = usuario
            _entrenamiento.value = entrenamiento

            // Verificar estado del like
            verificarLike(usuario._id, entrenamiento._id)
        }
    }

    /**
     * Carga el contador de likes para un entrenamiento específico
     */
    fun cargarContadorLikes(entrenamientoId: String) {
        if (currentEntrenamientoId == entrenamientoId) {
            viewModelScope.launch {
                try {
                    val token = _usuario.value?.token ?: return@launch
                    val likesDelEntrenamiento = repository.getFilter(
                        token,
                        mapOf("entrenamiento" to entrenamientoId)
                    )
                    _likesCount.value = likesDelEntrenamiento?.size ?: 0
                } catch (e: Exception) {
                    _errorMessage.value = "Error al cargar likes: ${e.message}"
                    Log.e("LikesViewModel", "Error al cargar contador: ${e.message}")
                }
            }
        }
    }

    /**
     * Método principal para hacer toggle del like
     */
    suspend fun toggleLike(entrenamientoId: String, entrenamientosViewModel: EntrenamientosViewModel) {
        val usuarioActual = _usuario.value ?: return
        val token = usuarioActual.token ?: return

        try {
            _isLoading.value = true

            if (_isLiked.value) {
                // Quitar like
                val success = repository.delete(
                    token = token,
                    datos = mapOf(
                        "usuario" to usuarioActual._id,
                        "entrenamiento" to entrenamientoId
                    )
                )

                if (success) {
                    _isLiked.value = false
                    _likesCount.value = maxOf(0, _likesCount.value - 1) // Evitar números negativos
                    Log.d("LikesViewModel", "Like eliminado. Nuevo contador: ${_likesCount.value}")
                }
            } else {
                // Dar like
                val nuevoLike = Likes(
                    usuario = usuarioActual._id,
                    entrenamiento = entrenamientoId
                )

                val likeCreado = repository.new(nuevoLike)

                if (likeCreado != null) {
                    _isLiked.value = true
                    _likesCount.value = _likesCount.value + 1
                    Log.d("LikesViewModel", "Like creado. Nuevo contador: ${_likesCount.value}")
                }
            }

            Log.d("FalloLikes", "_likesCount: ${_likesCount.value}")
            entrenamientosViewModel.update(entrenamientoId, mapOf("likes" to _likesCount.value.toString()))
            _errorMessage.value = null

        } catch (e: Exception) {
            _errorMessage.value = "Error al actualizar like: ${e.message}"
            Log.e("LikesViewModel", "Error en toggle like: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    private fun verificarLike(usuarioId: String, entrenamientoId: String) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token ?: return@launch
                val likes = repository.getAll(token)
                _likes.value = likes ?: emptyList()

                _isLiked.value = likes?.any {
                    it.usuario == usuarioId && it.entrenamiento == entrenamientoId
                } == true

                Log.d("LikesViewModel", "Verificación de like - Usuario: $usuarioId, Entrenamiento: $entrenamientoId, Es liked: ${_isLiked.value}")

            } catch (e: Exception) {
                _isLiked.value = false
                _errorMessage.value = "Error al verificar like: ${e.message}"
                Log.e("LikesViewModel", "Error al verificar like: ${e.message}")
            }
        }
    }

    // Métodos legacy mantenidos para compatibilidad
    fun new(like: Likes) {
        viewModelScope.launch {
            try {
                val creado = repository.new(like)
                if (creado != null) {
                    _isLiked.value = true
                    _errorMessage.value = null
                    verificarLike(like.usuario, like.entrenamiento)
                } else {
                    _errorMessage.value = "Error al crear el like"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun delete(datos: Map<String, String>) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token ?: return@launch
                val eliminar = repository.delete(token = token, datos = datos)
                if (eliminar) {
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

    /**
     * Método mejorado para obtener likes de un entrenamiento
     */
    fun devolverLikesEntrenamiento(entrenamiento: String, usuario: Usuarios?) {
        // Solo ejecutar si es diferente al entrenamiento actual
        if (currentEntrenamientoId != entrenamiento) {
            viewModelScope.launch {
                try {
                    val token = usuario?.token ?: return@launch
                    val respuesta = repository.getFilter(token, mapOf("entrenamiento" to entrenamiento))
                    val count = respuesta?.size ?: 0
                    _likesCount.value = count
                    Log.d("LikesViewModel", "Likes para entrenamiento $entrenamiento: $count")
                } catch (e: Exception) {
                    _errorMessage.value = e.message
                    Log.e("LikesViewModel", "Error al obtener likes: ${e.message}")
                }
            }
        }
    }

    fun limpiarError() {
        _errorMessage.value = null
    }
}