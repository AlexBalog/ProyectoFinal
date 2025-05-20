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
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
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

    private val _entrenamientos = MutableStateFlow<List<Entrenamientos>?>(emptyList())
    val entrenamientos: StateFlow<List<Entrenamientos>?> get() = _entrenamientos

    private val _entrenamiento = MutableStateFlow<Entrenamientos?>(null)
    val entrenamiento: StateFlow<Entrenamientos?> get() = _entrenamiento

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> get() = _likesCount

    fun setUsuario(usuario: Usuarios) {
        viewModelScope.launch {
            _usuario.value = usuario
            getAll()
        }
    }

    fun updateLikesCount(newCount: Int) {
        _likesCount.value = newCount
    }

    fun observarLikes(
        likesViewModel: LikesViewModel,
        usuario: Usuarios
    ) {
        viewModelScope.launch {
            entrenamientos.value?.forEach { entrenamiento ->
                likesViewModel.devolverLikesEntrenamiento(entrenamiento._id, usuario)
                _likesCount.value = likesViewModel.likesCount.value
            }
        }
    }


    suspend fun getEntrenamientoById(id: String): Entrenamientos? {
        // Primero buscar en los entrenamientos cargados en memoria
        val entrenamientosEnMemoria = _entrenamientos.value

        // Si tenemos entrenamientos cargados, buscamos por ID
        entrenamientosEnMemoria?.find { it._id == id }?.let {
            return it
        }

        // Si estamos aquí, no encontramos el entrenamiento en memoria
        // Verificamos si es el entrenamiento seleccionado actual
        _entrenamientoSeleccionado.value?.let {
            if (it._id == id) return it
        }

        // Si no lo encontramos en memoria, hacemos la petición al servidor
        return try {
            repository.getOne(id, _usuario.value?.token.orEmpty())
        } catch (e: Exception) {
            Log.e("EntrenamientosViewModel", "Error al obtener entrenamiento por ID: ${e.message}")
            null
        }
    }

    // Modifica esto en EntrenamientosViewModel
    fun observarLikesDeEntrenamiento(
        entrenamiento: String,
        likesViewModel: LikesViewModel,
        usuario: Usuarios
    ) {
        viewModelScope.launch {
            // Inicialización inicial
            likesViewModel.devolverLikesEntrenamiento(entrenamiento, usuario)

            // Observa continuamente los cambios en el contador
            likesViewModel.likesCount.collect { nuevoContador ->
                _likesCount.value = nuevoContador
            }
        }
    }

    suspend fun getAll() {
        viewModelScope.launch {
            try {
                _usuario.value?.let { currentUser ->
                    val token = currentUser.token ?: return@launch
                    val lista = repository.getAll(token)
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

    fun update(_id: String, updatedData: Map<String, String>) {
        viewModelScope.launch {
            _usuario.value?.let { currentUser ->
                val token = currentUser.token ?: return@launch // Si no hay token, no hacemos nada
                val success = repository.update(_id, updatedData, token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
            }
        }
    }


    private val _entrenamientoSeleccionado = MutableStateFlow<Entrenamientos?>(null)
    val entrenamientoSeleccionado: StateFlow<Entrenamientos?> get() = _entrenamientoSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado ${usuario.value}")
        viewModelScope.launch {
            _entrenamientoSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    suspend fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val lista = repository.getFilter(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _entrenamientos.value = lista
                    Log.d("Habitaciones", "Datos filtrados cargados: $lista")
                } else {
                    _entrenamientos.value = emptyList()
                    Log.d("Habitaciones", "No se encontraron habitaciones con esos filtros.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones filtradas: ${e.message}")
                _entrenamientos.value = emptyList()
            }
        }
    }

    fun peticion(nuevoEntrenamiento: Entrenamientos) {
        viewModelScope.launch {
            try {
                val creado = repository.peticion(nuevoEntrenamiento)
                if (creado != null) {
                    _entrenamiento.value = creado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    suspend fun new(entrenamiento: Entrenamientos) {
        viewModelScope.launch {
            try {
                _usuario.value?.let { usuario ->
                    val token = usuario.token ?: return@launch
                    val creado = repository.new(entrenamiento, token)
                    if (creado != null) {
                        _entrenamiento.value = creado
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Error al crear el usuario"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}
