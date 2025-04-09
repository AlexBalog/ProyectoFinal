package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Repository.EntrenamientoRealizadoRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntrenamientoRealizadoViewModel @Inject constructor(private val repository: EntrenamientoRealizadoRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _entrenamientoRealizado = MutableStateFlow<List<EntrenamientoRealizado>?>(emptyList())
    val entrenamientoRealizado: StateFlow<List<EntrenamientoRealizado>?> get() = _entrenamientoRealizado

    private fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _entrenamientoRealizado.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _entrenamientoRealizado.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _entrenamientoRealizado.value = emptyList()
            }
        }
    }

    private val _entrenamientoRealizadoSeleccionado = MutableStateFlow<EntrenamientoRealizado?>(null)
    val entrenamientoRealizadoSeleccionado: StateFlow<EntrenamientoRealizado?> get() = _entrenamientoRealizadoSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _entrenamientoRealizadoSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val lista = repository.getFilter(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _entrenamientoRealizado.value = lista
                    Log.d("Habitaciones", "Datos filtrados cargados: $lista")
                } else {
                    _entrenamientoRealizado.value = emptyList()
                    Log.d("Habitaciones", "No se encontraron habitaciones con esos filtros.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones filtradas: ${e.message}")
                _entrenamientoRealizado.value = emptyList()
            }
        }
    }
}