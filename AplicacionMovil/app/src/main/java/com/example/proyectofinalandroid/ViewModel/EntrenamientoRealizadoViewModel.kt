package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Model.Usuarios
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.Repository.EntrenamientoRealizadoRepository
import com.example.proyectofinalandroid.View.EntrenamientoItem
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.collections.forEachIndexed
import kotlinx.coroutines.async

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

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }

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

    suspend fun new(entrenamientoRealizado: EntrenamientoRealizado) {
        try {
            // Llamamos al repositorio para crear el entrenamiento en la base de datos
            val creado = repository.new(entrenamientoRealizado) // Esta llamada es suspensiva y debe esperar a completarse
            if (creado != null) {
                // Si se crea correctamente, actualizamos el valor en el ViewModel
                _entrenamientoRealizadoSeleccionado.value = creado
                Log.d("FalloNew1", "Se crea correctamente")
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Error al crear el entrenamiento"
            }
        } catch (e: Exception) {
            // En caso de error, guardamos el mensaje
            _errorMessage.value = e.message
        }
    }


    suspend fun guardarEntrenamiento(
        entrenamientoId: String,
        duracion: String,
        viewModelEjercicio: EjercicioRealizadoViewModel,
        viewModelSerie: SerieRealizadaViewModel
    ) {
        try {
            val entrenamientoRealizado = EntrenamientoRealizado(
                usuario = _usuario.value!!._id,
                entrenamiento = entrenamientoId,
                duracion = duracion,
                fecha = Date(),
                ejerciciosRealizados = emptyList()
            )

            // Llamamos a new y esperamos el resultado
            val resultado = repository.new(entrenamientoRealizado)
            Log.d("FalloERVM1", "Llega despues de crear en la bbdd el entrenamientoRealizado")

            // Si el resultado es null, lanzamos una excepción temprana
            if (resultado == null) {
                throw Exception("El servidor no devolvió el entrenamiento creado")
            }

            // Actualizamos el valor en el StateFlow y usamos directamente el resultado
            _entrenamientoRealizadoSeleccionado.value = resultado
            val entrenamientoRealizadoId = resultado._id


            // Verificamos que tengamos un ID válido
            if (entrenamientoRealizadoId.isNullOrBlank()) {
                throw Exception("ID de entrenamiento no válido")
            }

            viewModelEjercicio.actualizarIds(entrenamientoRealizadoId)


            val listaEjerciciosRealizados = viewModelEjercicio.ejerciciosRealizados.value!!
            viewModelEjercicio.vaciarLista()

            Log.d("FalloERVM3", "Llega despues de vaciar la lista ${viewModelEjercicio.ejerciciosRealizados.value}")

            for (ejercicioRealizado in listaEjerciciosRealizados) {
                Log.d("FalloERVM3.5", "${ejercicioRealizado}")
                val creado = viewModelEjercicio.new(ejercicioRealizado) // Asegúrate de que `new` devuelva el objeto creado
                if (creado != null) {
                    Log.d("FalloERVM3.7", "${creado}")
                    viewModelEjercicio.guardarAListaRealizados(creado)
                } else {
                    Log.e("Error", "No se pudo crear el ejercicio realizado")
                }
            }

            Log.d("FalloERVM4", "Llega despues de crear ejerciciosRealizados ${viewModelEjercicio.ejerciciosRealizados.value}")

            for (ejercicioRealizado in viewModelEjercicio.ejerciciosRealizados.value) {
                viewModelSerie.actualizarIds(idEjercicioRealizado = ejercicioRealizado._id, idEjercicio = ejercicioRealizado.ejercicio)
            }

            val listaSeriesRealizadas = viewModelSerie.seriesRealizadas.value!!
            viewModelSerie.vaciarLista()
            for (serieRealizada in listaSeriesRealizadas) {
                viewModelSerie.new(serieRealizada)
                viewModelSerie.anadirSerieALista(viewModelSerie.serieRealizadaSeleccionada.value as SerieRealizada)
            }

            Log.d("FalloERVM6", "Llega despues de crear las series ${viewModelSerie.seriesRealizadas.value}")

        } catch (e: Exception) {
            Log.e("FalloGuardarEntrenamiento", "Error al guardar: ${e.message}")
        }
    }
}