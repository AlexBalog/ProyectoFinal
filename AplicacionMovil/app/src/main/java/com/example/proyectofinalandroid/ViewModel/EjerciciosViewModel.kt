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
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EjerciciosViewModel @Inject constructor(private val repository: EjerciciosRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _ejercicios = MutableStateFlow<List<Ejercicios>?>(emptyList())
    val ejercicios: StateFlow<List<Ejercicios>?> get() = _ejercicios

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
        getAll()
    }

    fun getAll() {
        viewModelScope.launch {
            try {
                val lista = repository.getAll(token = _usuario.value?.token.toString())
                if (lista != null) {
                    _ejercicios.value = lista
                    Log.d("Habitaciones", "Datos cargados: $lista")
                } else {
                    _ejercicios.value = emptyList()
                    Log.d("Habitaciones", "Respuesta nula o lista vacía.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones: ${e.message}")
                _ejercicios.value = emptyList()
            }
        }
    }

    private val _ejercicioSeleccionado = MutableStateFlow<Ejercicios?>(null)
    val ejercicioSeleccionado: StateFlow<Ejercicios?> get() = _ejercicioSeleccionado

    fun getOne(id: String) {
        Log.d("Mensaje", "${id} cargado")
        viewModelScope.launch {
            _ejercicioSeleccionado.value = repository.getOne(id, _usuario.value?.token.toString())
        }
    }

    fun getFilter(filtros: Map<String, String>) {
        viewModelScope.launch {
            try {
                val lista = repository.getFilter(_usuario.value?.token.toString(), filtros)
                if (lista != null) {
                    _ejercicios.value = lista
                    Log.d("Habitaciones", "Datos filtrados cargados: $lista")
                } else {
                    _ejercicios.value = emptyList()
                    Log.d("Habitaciones", "No se encontraron habitaciones con esos filtros.")
                }
            } catch (e: Exception) {
                Log.e("Habitaciones", "Error al obtener habitaciones filtradas: ${e.message}")
                _ejercicios.value = emptyList()
            }
        }
    }

    suspend fun fetchOne(id: String): Ejercicios? {
        return repository.getOne(id, _usuario.value?.token.orEmpty())
    }

}