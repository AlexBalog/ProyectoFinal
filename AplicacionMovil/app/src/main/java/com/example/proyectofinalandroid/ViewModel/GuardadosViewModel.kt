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


    private fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _guardados.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _guardados.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _guardados.value = emptyList()
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
                val creado = repository.new(guardado)
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