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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

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

    fun actualizarIds(id: String): List<EjercicioRealizado> {
        // En lugar de modificar el state, devolvemos una copia actualizada
        return _ejerciciosRealizados.value?.map { ejercicio ->
            ejercicio.copy(entrenamientoRealizado = id)
        } ?: emptyList()
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

    suspend fun new2(ejercicioRealizado: EjercicioRealizado): EjercicioRealizado? {
        return withContext(Dispatchers.IO) {
            try {
                // Registramos lo que estamos intentando crear
                Log.d("FalloViewModel1", "Intentando crear ejercicio: $ejercicioRealizado")

                // Llamada directa al repositorio
                val creado = repository.new(ejercicioRealizado)

                if (creado != null) {
                    Log.d("FalloViewModel2", "Ejercicio creado: $creado")

                    // Validamos que el ejercicio creado tenga todos los campos requeridos
                    if (creado._id.isNotEmpty() && creado.ejercicio != null && creado.nombre != null) {
                        Log.d("FalloViewModel3", "Ejercicio validado correctamente")
                        return@withContext creado
                    } else {
                        // Si falta algún campo, hacemos una copia con los datos originales
                        Log.w("FalloViewModel4", "Datos incompletos en la respuesta, completando con datos originales")
                        val completado = creado.copy(
                            ejercicio = creado.ejercicio ?: ejercicioRealizado.ejercicio,
                            nombre = creado.nombre ?: ejercicioRealizado.nombre,
                            entrenamiento = creado.entrenamiento ?: ejercicioRealizado.entrenamiento
                        )
                        Log.d("FalloViewModel5", "Ejercicio completado: $completado")
                        return@withContext completado
                    }
                } else {
                    // Si no se crea, registramos error y creamos un objeto alternativo
                    Log.e("FalloViewModel6", "El repositorio devolvió null")
                    _errorMessage.value = "Error al crear el ejercicio realizado - respuesta nula"

                    // Último recurso: verificar directamente con la API si el ejercicio se creó
                    // Este bloque debe implementarse según tus necesidades

                    null
                }
            } catch (e: Exception) {
                Log.e("FalloViewModel7", "Error al crear ejercicio: ${e.message}", e)
                _errorMessage.value = "Error: ${e.message}"
                null
            }
        }
    }

    fun guardarALista(ejercicio: Ejercicios, entrenamientoId: String) {
        // Verificar primero si el ejercicio ya existe para evitar duplicados
        val yaExiste = _ejerciciosRealizados.value?.any { it.ejercicio == ejercicio._id } == true

        if (!yaExiste) {
            val ejerReal = EjercicioRealizado(
                _id = "",
                entrenamiento = entrenamientoId,
                ejercicio = ejercicio._id,
                nombre = ejercicio.nombre
            )

            // Usamos update atómico para evitar problemas de concurrencia
            val listaActual = _ejerciciosRealizados.value?.toMutableList() ?: mutableListOf()
            listaActual.add(ejerReal)
            _ejerciciosRealizados.value = listaActual
        }
    }

    fun guardarAListaRealizados(ejercicioRealizado: EjercicioRealizado) {
        val listaActual = _ejerciciosRealizados.value?.toMutableList() ?: mutableListOf()
        listaActual.add(ejercicioRealizado)
        _ejerciciosRealizados.value = listaActual
    }

}