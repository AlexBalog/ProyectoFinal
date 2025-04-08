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
import com.example.proyectofinalandroid.Model.RealizarEjer
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import com.example.proyectofinalandroid.Repository.RealizarEjerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RealizarEjerViewModel @Inject constructor(private val repository: RealizarEjerRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _realizarejer = MutableStateFlow<List<RealizarEjer>?>(emptyList())
    val realizarejer: StateFlow<List<RealizarEjer>?> get() = _realizarejer

    private fun getAllRealizarEjer() {
        viewModelScope.launch {
            try {
                val lista = repository.getAllRealizarEjer(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _realizarejer.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _realizarejer.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _realizarejer.value = emptyList()
            }
        }
    }

    private val _realizarejerSeleccionado = MutableStateFlow<RealizarEjer?>(null)
    val realizarejerSeleccionado: StateFlow<RealizarEjer?> get() = _realizarejerSeleccionado

    fun getOneRealizarEjer(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _realizarejerSeleccionado.value = repository.getOneRealizarEjer(id, _usuario.value?.token.toString())
        }
    }
}