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
import com.example.proyectofinalandroid.Model.Eventos
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import com.example.proyectofinalandroid.Repository.EventosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EventosViewModel @Inject constructor(
    private val repository: EventosRepository
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _eventos = MutableStateFlow<List<Eventos>>(emptyList())
    val eventos: StateFlow<List<Eventos>> get() = _eventos

    private val _tipoEventos = MutableStateFlow<List<String>>(emptyList())
    val tipoEventos: StateFlow<List<String>> get() = _tipoEventos

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }

    fun cargarEventosYTipos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = _usuario.value?.token.orEmpty()
                val lista = repository.getAll(token)
                _eventos.value = lista ?: emptyList()

                // Extraer tipos únicos de los eventos
                val tipos = _eventos.value
                    .mapNotNull { it.tipo }
                    .toSet()
                    .toList()

                _tipoEventos.value = tipos
                _errorMessage.value = null
            } catch (e: Exception) {
                _eventos.value = emptyList()
                _tipoEventos.value = emptyList()
                _errorMessage.value = "Error al cargar eventos: ${e.message}"
                Log.e("EventosViewModel", "Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _eventoSeleccionado = MutableStateFlow<Eventos?>(null)
    val eventoSeleccionado: StateFlow<Eventos?> get() = _eventoSeleccionado

    suspend fun getOne(id: String) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.orEmpty()
                _eventoSeleccionado.value = repository.getOne(id, token)
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar evento: ${e.message}"
            }
        }
    }

    suspend fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val token = _usuario.value?.token.orEmpty()
                val lista = repository.getFilter(token, filtros)
                _eventos.value = lista ?: emptyList()
            } catch (e: Exception) {
                _eventos.value = emptyList()
                _errorMessage.value = "Error al filtrar eventos: ${e.message}"
            }
        }
    }
}
