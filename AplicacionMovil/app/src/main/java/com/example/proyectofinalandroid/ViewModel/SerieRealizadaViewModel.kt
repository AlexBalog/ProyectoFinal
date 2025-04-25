package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.Repository.SerieRealizadaRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SerieRealizadaViewModel @Inject constructor(private val repository: SerieRealizadaRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _seriesRealizadas = MutableStateFlow<List<SerieRealizada>?>(emptyList())
    val seriesRealizadas: StateFlow<List<SerieRealizada>?> get() = _seriesRealizadas

    fun anadirSerieALista(serie: SerieRealizada) {
        val listaActual = _seriesRealizadas.value?.toMutableList() ?: mutableListOf()
        listaActual.add(serie)
        _seriesRealizadas.value = listaActual
    }

    fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _seriesRealizadas.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _seriesRealizadas.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _seriesRealizadas.value = emptyList()
            }
        }
    }

    private val _serieRealizadaSeleccionado = MutableStateFlow<SerieRealizada?>(null)
    val serieRealizadaSeleccionada: StateFlow<SerieRealizada?> get() = _serieRealizadaSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _serieRealizadaSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val lista = repository.getFilter(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _seriesRealizadas.value = lista
                    Log.d("Habitaciones", "Datos filtrados cargados: $lista")
                } else {
                    _seriesRealizadas.value = emptyList()
                    Log.d("Habitaciones", "No se encontraron habitaciones con esos filtros.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones filtradas: ${e.message}")
                _seriesRealizadas.value = emptyList()
            }
        }
    }

    fun new(serieRealizada: SerieRealizada) {
        viewModelScope.launch {
            try {
                val creado = repository.new(serieRealizada)
                if (creado != null) {
                    _serieRealizadaSeleccionado.value = creado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun eliminarSerieDeLista(indice: Int) {
        viewModelScope.launch {
            try {
                val listaActual = _seriesRealizadas.value?.toMutableList() ?: mutableListOf()
                if (indice in listaActual.indices) {
                    listaActual.removeAt(indice)
                    _seriesRealizadas.value = listaActual
                } else {
                    Log.e("SerieRealizadaViewModel", "Índice fuera de rango: $indice")
                }
            } catch (e: Exception) {
                Log.e("SerieRealizadaViewModel", "Error al eliminar la serie: ${e.message}")
            }
        }
    }
}