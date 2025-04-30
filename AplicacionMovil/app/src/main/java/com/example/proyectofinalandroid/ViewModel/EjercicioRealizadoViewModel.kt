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
import com.example.proyectofinalandroid.Model.Ejercicios

@HiltViewModel
class EjercicioRealizadoViewModel @Inject constructor(private val repository: EjercicioRealizadoRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _ejerciciosRealizados = MutableStateFlow<List<EjercicioRealizado>?>(emptyList())
    val ejerciciosRealizados: StateFlow<List<EjercicioRealizado>?> get() = _ejerciciosRealizados

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }

    fun actualizarIds(id: String) {
        _ejerciciosRealizados.value!!.forEach { ejercicio ->
            ejercicio.entrenamientoRealizado = id
        }
        _ejerciciosRealizados.value = _ejerciciosRealizados.value
    }


    fun vaciarLista() {
        _ejerciciosRealizados.value = emptyList()
    }

    private fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _ejerciciosRealizados.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _ejerciciosRealizados.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _ejerciciosRealizados.value = emptyList()
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

    suspend fun new(ejercicioRealizado: EjercicioRealizado): EjercicioRealizado? {
        return try {
            val creado = repository.new(ejercicioRealizado)
            if (creado != null) {
                _ejercicioRealizadoSeleccionado.value = creado
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Error al crear el ejercicio realizado"
            }
            creado
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
        }
    }

    fun guardarALista(ejercicio: Ejercicios, entrenamientoId: String) {
        val ejerReal = EjercicioRealizado(
            _id = "",
            entrenamiento = entrenamientoId,
            ejercicio = ejercicio._id,
            nombre = ejercicio.nombre,
            series = emptyList()
        )
        val listaActual = _ejerciciosRealizados.value?.toMutableList() ?: mutableListOf()
        listaActual.add(ejerReal)
        _ejerciciosRealizados.value = listaActual
    }

    fun guardarAListaRealizados(ejercicioRealizado: EjercicioRealizado) {
        val listaActual = _ejerciciosRealizados.value?.toMutableList() ?: mutableListOf()
        listaActual.add(ejercicioRealizado)
        _ejerciciosRealizados.value = listaActual
    }

}