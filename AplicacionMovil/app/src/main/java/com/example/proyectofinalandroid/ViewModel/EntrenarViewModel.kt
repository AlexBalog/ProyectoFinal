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
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import com.example.proyectofinalandroid.Repository.EntrenarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EntrenarViewModel @Inject constructor(private val repository: EntrenarRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _entrenar = MutableStateFlow<List<Entrenar>?>(emptyList())
    val entrenar: StateFlow<List<Entrenar>?> get() = _entrenar

    private fun getAllEntrenar() {
        viewModelScope.launch {
            try {
                val lista = repository.getAllEntrenar(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _entrenar.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _entrenar.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _entrenar.value = emptyList()
            }
        }
    }

    private val _entrenarSeleccionado = MutableStateFlow<Entrenar?>(null)
    val entrenarSeleccionado: StateFlow<Entrenar?> get() = _entrenarSeleccionado

    fun getOneEntrenar(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _entrenarSeleccionado.value = repository.getOneEntrenar(id, _usuario.value?.token.toString())
        }
    }

    fun getFilterEntrenar(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val lista = repository.getFilterEntrenar(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _entrenar.value = lista
                    Log.d("Habitaciones", "Datos filtrados cargados: $lista")
                } else {
                    _entrenar.value = emptyList()
                    Log.d("Habitaciones", "No se encontraron habitaciones con esos filtros.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones filtradas: ${e.message}")
                _entrenar.value = emptyList()
            }
        }
    }
}