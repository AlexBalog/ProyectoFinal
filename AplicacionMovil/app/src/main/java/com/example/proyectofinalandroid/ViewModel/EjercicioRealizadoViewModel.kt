package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Repository.EjercicioRealizadoRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EjercicioRealizadoViewModel @Inject constructor(private val repository: EjercicioRealizadoRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _ejercicioRealizado = MutableStateFlow<List<EjercicioRealizado>?>(emptyList())
    val ejercicioRealizado: StateFlow<List<EjercicioRealizado>?> get() = _ejercicioRealizado

    private fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _ejercicioRealizado.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _ejercicioRealizado.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _ejercicioRealizado.value = emptyList()
            }
        }
    }

    private val _ejercicioRealizadoSeleccionado = MutableStateFlow<EjercicioRealizado?>(null)
    val ejercicioRealizadoSeleccionado: StateFlow<EjercicioRealizado?> get() = _ejercicioRealizadoSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _ejercicioRealizadoSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun new(ejercicioRealizado: EjercicioRealizado) {
        viewModelScope.launch {
            try {
                val creado = repository.new(ejercicioRealizado)
                if (creado != null) {
                    _ejercicioRealizadoSeleccionado.value = creado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}