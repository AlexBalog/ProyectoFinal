package com.example.proyectofinalandroid.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.Model.Mensaje
import com.example.proyectofinalandroid.Model.NuevaConversacion
import com.example.proyectofinalandroid.Model.NuevoMensaje
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.IARepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log


@HiltViewModel
class IAViewModel @Inject constructor(private val repository: IARepository) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    private val _conversaciones = MutableStateFlow<List<Conversacion>>(emptyList())
    val conversaciones: StateFlow<List<Conversacion>> get() = _conversaciones

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> get() = _mensajes

    private val _conversacionActual = MutableStateFlow<Conversacion?>(null)
    val conversacionActual: StateFlow<Conversacion?> get() = _conversacionActual

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
        obtenerConversaciones()
    }

    fun obtenerConversaciones() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _usuario.value?.token?.let { token ->
                    val conversaciones = repository.obtenerConversaciones(token, _usuario.value!!._id)
                    if (conversaciones != null) {
                        _conversaciones.value = conversaciones
                    } else {
                        _errorMessage.value = "Error al cargar conversaciones"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearConversacion(categoria: String, titulo: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _usuario.value?.token?.let { token ->
                    val nuevaConversacion = NuevaConversacion(usuario.value!!._id, categoria, titulo)
                    val conversacion = repository.crearConversacion(token, nuevaConversacion)
                    if (conversacion != null) {
                        _conversacionActual.value = conversacion
                        obtenerConversaciones() // Actualizar lista
                        obtenerMensajes(conversacion._id)
                    } else {
                        _errorMessage.value = "Error al crear conversación"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarConversacion(conversacionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val conv = _conversaciones.value.find { it._id == conversacionId }
                _conversacionActual.value = conv
                obtenerMensajes(conversacionId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun obtenerMensajes(conversacionId: String) {
        viewModelScope.launch {
            try {
                _usuario.value?.token?.let { token ->
                    val mensajes = repository.obtenerMensajes(token, conversacionId)
                    if (mensajes != null) {
                        _mensajes.value = mensajes
                    } else {
                        _errorMessage.value = "Error al cargar mensajes"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun enviarMensaje(contenido: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("FalloEnviar-1", "entra")
                _usuario.value?.token?.let { token ->
                    Log.d("FalloEnviar0", "$token")
                    _conversacionActual.value?._id?.let { conversacionId ->
                        Log.d("FalloEnviar1", "$conversacionId")
                        val request = mapOf("contenido" to contenido, "usuario" to usuario.value!!._id)
                        val respuesta = repository.enviarMensaje(token, conversacionId, request)

                        if (respuesta != null) {
                            obtenerMensajes(conversacionId)
                        } else {
                            _errorMessage.value = "Error al enviar mensaje"
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}