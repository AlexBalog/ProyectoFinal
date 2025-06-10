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

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }


    fun eliminarSerieDeLista(serie: SerieRealizada) {
        val listaActual = _seriesRealizadas.value?.toMutableList() ?: mutableListOf()
        listaActual.remove(serie)
        _seriesRealizadas.value = listaActual
    }

    // También necesitas una función para eliminar por índice si es más conveniente:
    fun eliminarSeriePorIndiceYEjercicio(ejercicioId: String, indiceSerie: Int) {
        val listaActual = _seriesRealizadas.value?.toMutableList() ?: mutableListOf()
        val seriesDelEjercicio = listaActual.filter { it.ejercicio == ejercicioId }

        if (indiceSerie >= 0 && indiceSerie < seriesDelEjercicio.size) {
            val serieAEliminar = seriesDelEjercicio[indiceSerie]
            listaActual.remove(serieAEliminar)

            // Reordenar números de serie
            val seriesRestantes = listaActual.filter { it.ejercicio == ejercicioId }
            seriesRestantes.forEachIndexed { index, serie ->
                serie.numeroSerie = index + 1
            }

            _seriesRealizadas.value = listaActual
        }
    }


    fun vaciarLista() {
        val listaVacia: List<SerieRealizada> = emptyList()
        _seriesRealizadas.value = listaVacia
    }

    fun actualizarIds(idEjercicioRealizado: String, idEjercicio: String): List<SerieRealizada> {
        // Devolver lista nueva en lugar de modificar state
        return _seriesRealizadas.value?.map { serie ->
            if (serie.ejercicio == idEjercicio) {
                serie.copy(ejercicioRealizado = idEjercicioRealizado)
            } else {
                serie
            }
        } ?: emptyList()
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

    // Añade esto al método getFilter en SerieRealizadaViewModel.kt
    fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                Log.d("SerieRealizadaViewModel", "Buscando series con filtros: $filtros")
                val lista = repository.getFilter(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _seriesRealizadas.value = lista
                    Log.d("SerieRealizadaViewModel", "Series encontradas: ${lista.size} para filtro: $filtros")
                } else {
                    _seriesRealizadas.value = emptyList()
                    Log.d("SerieRealizadaViewModel", "No se encontraron series con filtro: $filtros")
                }
            } catch (e: Exception) {
                Log.e("SerieRealizadaViewModel", "Error al obtener series: ${e.message}")
                _seriesRealizadas.value = emptyList()
            }
        }
    }

    suspend fun new(serieRealizada: SerieRealizada): SerieRealizada? {
        return try {
            val creado = repository.new(serieRealizada, _usuario.value?.token.toString())
            if (creado != null) {
                _serieRealizadaSeleccionado.value = creado
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Error al crear el usuario"
            }
            creado
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
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