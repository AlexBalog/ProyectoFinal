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
import java.text.SimpleDateFormat
import java.util.*
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

    fun setUsuario(usuario: Usuarios) {
        viewModelScope.launch {
            _usuario.value = usuario
            getAll()
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

        val usuario = _usuario.value
        if (usuario == null) {
            Log.e("EntrenamientosViewModel", "No hay usuario loggeado")
            return null
        }

        val token = usuario.token
        if (token.isNullOrBlank()) {
            Log.e("EntrenamientosViewModel", "Token del usuario es null o vacío")
            return null
        }

        // Si no lo encontramos en memoria, hacemos la petición al servidor
        return try {
            repository.getOne(id, token)
        } catch (e: Exception) {
            Log.e("EntrenamientosViewModel", "Error al obtener entrenamiento por ID: ${e.message}")
            null
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


    suspend fun actualizarEntrenamiento(entrenamiento: Entrenamientos): Entrenamientos? {
        return try {
            _usuario.value?.let { usuario ->
                val token = usuario.token ?: return@let null

                // Verificar que el entrenamiento tenga un ID válido
                if (entrenamiento._id.isBlank()) {
                    _errorMessage.value = "El entrenamiento no tiene un ID válido"
                    return null
                }

                // Llamar al repositorio para actualizar el entrenamiento
                val entrenamientoActualizado = repository.actualizarEntrenamiento(entrenamiento, token)

                if (entrenamientoActualizado != null) {
                    // Actualizar el estado local
                    _entrenamiento.value = entrenamientoActualizado

                    // Si el entrenamiento está en la lista de entrenamientos, actualizarlo también allí
                    _entrenamientos.value = _entrenamientos.value?.map {
                        if (it._id == entrenamiento._id) entrenamientoActualizado else it
                    }

                    // Si es el entrenamiento seleccionado actualmente, actualizarlo
                    if (_entrenamientoSeleccionado.value?._id == entrenamiento._id) {
                        _entrenamientoSeleccionado.value = entrenamientoActualizado
                    }

                    _errorMessage.value = null
                    entrenamientoActualizado
                } else {
                    _errorMessage.value = "Error al actualizar el entrenamiento"
                    null
                }
            } ?: run {
                _errorMessage.value = "Usuario no autenticado"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            Log.e("EntrenamientosViewModel", "Error al actualizar entrenamiento: ${e.message}")
            null
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
                val creado = repository.peticion(nuevoEntrenamiento, _usuario.value?.token.toString())
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

    suspend fun eliminarEntrenamiento(entrenamientoId: String): Boolean {
        return try {
            _usuario.value?.let { usuario ->
                val token = usuario.token ?: return false

                val success = repository.eliminarEntrenamiento(entrenamientoId, token)

                if (success) {
                    // Actualizar la lista local removiendo el entrenamiento eliminado
                    _entrenamientos.value = _entrenamientos.value?.filter {
                        it._id != entrenamientoId
                    }

                    // Si es el entrenamiento seleccionado actualmente, limpiarlo
                    if (_entrenamientoSeleccionado.value?._id == entrenamientoId) {
                        _entrenamientoSeleccionado.value = null
                    }

                    _errorMessage.value = null
                    true
                } else {
                    _errorMessage.value = "Error al eliminar el entrenamiento"
                    false
                }
            } ?: run {
                _errorMessage.value = "Usuario no autenticado"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            Log.e("EntrenamientosViewModel", "Error al eliminar entrenamiento: ${e.message}")
            false
        }
    }

    suspend fun darDeBajaEntrenamiento(entrenamientoId: String): Boolean {
        return try {
            Log.d("EntrenamientosVM", "darDeBajaEntrenamiento: $entrenamientoId")
            _usuario.value?.let { usuario ->
                val token = usuario.token ?: return false

                val updateData = mapOf(
                    "baja" to "true",
                    "fechaBaja" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
                )

                val success = repository.update(entrenamientoId, updateData, token)
                Log.d("EntrenamientosVM", "repository.update result: $success")

                // ✅ NO TOCAR LA LISTA LOCAL - dejar que la View se encargue de recargar
                if (success) {
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al dar de baja el entrenamiento"
                }

                success // ✅ Simplemente devolver el resultado del servidor
            } ?: false
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            Log.e("EntrenamientosVM", "Excepción: ${e.message}")
            false
        }
    }
}
