package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.Guardados
import com.example.proyectofinalandroid.Repository.GuardadosRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuardadosViewModel @Inject constructor(private val repository: GuardadosRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _guardadosCount = MutableStateFlow(0)
    val guardadosCount: StateFlow<Int> get() = _guardadosCount

    private val _entrenamiento = MutableStateFlow<Entrenamientos?>(null)
    val entrenamiento: StateFlow<Entrenamientos?> get() = _entrenamiento

    private val _guardados = MutableStateFlow<List<Guardados>?>(emptyList())
    val guardados: StateFlow<List<Guardados>?> get() = _guardados

    fun setUsuarioYEntrenamiento(usuario: Usuarios, entrenamiento: Entrenamientos) {
        _usuario.value = usuario
        _entrenamiento.value = entrenamiento
        verificarGuardado(usuario._id, entrenamiento._id)
    }

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }


    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> get() = _isSaved

    private fun verificarGuardado(usuarioId: String, entrenamientoId: String) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.toString() ?: return@launch
                val guardados = repository.getAll(token)
                _guardados.value = guardados ?: emptyList()
                _isSaved.value = guardados?.any {
                    it.usuario == usuarioId && it.entrenamiento == entrenamientoId
                } == true
            } catch (e: Exception) {
                _isSaved.value = false
                _errorMessage.value = "Error al verificar guardado: ${e.message}"
            }
        }
    }


    fun getAll() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = _usuario.value?.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    _guardados.value = emptyList()
                    return@launch
                }

                val lista = repository.getAll(token)
                if (lista != null) {
                    _guardados.value = lista
                    Log.d("GuardadosViewModel", "Datos cargados: ${lista.size} guardados")
                } else {
                    _guardados.value = emptyList()
                    Log.d("GuardadosViewModel", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("GuardadosViewModel", "Error al obtener guardados: ${e.message}")
                _guardados.value = emptyList()
                _errorMessage.value = "Error al cargar guardados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getGuardadosByUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = _usuario.value?.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    _guardados.value = emptyList()
                    return@launch
                }

                val filtros = mapOf("usuario" to usuarioId)
                val lista = repository.getFilter(token, filtros)
                if (lista != null) {
                    _guardados.value = lista
                    Log.d("FalloGuardadosViewModel", "Guardados del usuario $usuarioId: ${lista.size}")
                } else {
                    _guardados.value = emptyList()
                    Log.d("FalloGuardadosViewModel", "No se encontraron guardados para el usuario $usuarioId")
                }
            } catch (e: Exception) {
                Log.e("GuardadosViewModel", "Error al obtener guardados del usuario: ${e.message}")
                _guardados.value = emptyList()
                _errorMessage.value = "Error al cargar guardados: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _guardadosSeleccionado = MutableStateFlow<Guardados?>(null)
    val guardadosSeleccionado: StateFlow<Guardados?> get() = _guardadosSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _guardadosSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun new(guardado: Guardados) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    _guardados.value = emptyList()
                    return@launch
                }
                val creado = repository.new(guardado, token)
                if (creado != null) {
                    _guardadosSeleccionado.value = creado
                    _isSaved.value = true
                    _errorMessage.value = null
                    verificarGuardado(guardado.usuario, guardado.entrenamiento)
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
                    _guardadosSeleccionado.value = null
                    _isSaved.value = false
                    _errorMessage.value = null
                    verificarGuardado(datos["usuario"].orEmpty(), datos["entrenamiento"].orEmpty())
                } else {
                    _errorMessage.value = "Error al eliminar el guardado"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun devolverGuardadosEntrenamiento(entrenamiento: String, usuario: Usuarios?) {
        viewModelScope.launch {
            try {
                val token = usuario!!.token.toString()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Usuario no autenticado"
                    return@launch
                }
                val respuesta = repository.getFilter(token, mapOf("entrenamiento" to entrenamiento))
                if (respuesta != null) {
                    _guardadosCount.value = respuesta.size
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}