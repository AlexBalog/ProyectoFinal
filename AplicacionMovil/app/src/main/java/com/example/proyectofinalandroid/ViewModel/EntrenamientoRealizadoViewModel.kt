package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Model.EjerciciosRealizadosRequest
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
import kotlinx.coroutines.delay

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


    fun getEntrenamientosRealizadosByUsuario(usuario: Usuarios) {
        viewModelScope.launch {
            try {

                _isLoading.value = true
                if (_usuario.value == null || _usuario.value?._id != usuario._id) {
                    _usuario.value = usuario
                }

                val filtros = mapOf("usuario" to usuario._id)

                val lista = repository.getFilter(usuario.token ?: "", filtros)
                if (lista != null) {
                    _entrenamientoRealizado.value = lista
                    Log.d("EntrenamientoRealizado", "Entrenamientos del usuario cargados: ${lista.size}")
                } else {
                    _entrenamientoRealizado.value = emptyList()
                    Log.d("EntrenamientoRealizado", "No se encontraron entrenamientos para este usuario")
                }
            } catch (e: Exception) {
                Log.e("EntrenamientoRealizado", "Error al obtener entrenamientos del usuario: ${e.message}")
                _entrenamientoRealizado.value = emptyList()
                _errorMessage.value = "Error al cargar los entrenamientos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
            // 1. Crear el objeto EntrenamientoRealizado
            val entrenamientoRealizado = EntrenamientoRealizado(
                usuario = _usuario.value!!._id,
                entrenamiento = entrenamientoId,
                duracion = duracion,
                fecha = Date(),
                ejerciciosRealizados = emptyList()
            )

            // 2. Guardar el entrenamiento y obtener su ID (espera a que termine)
            val resultado = repository.new(entrenamientoRealizado)
                ?: throw Exception("El servidor no devolvió el entrenamiento creado")

            // Evitamos actualizar el StateFlow para no causar recomposiciones innecesarias
            val entrenamientoRealizadoId = resultado._id


            // 3. Establecer el ID del entrenamiento en los ejercicios (sin actualizar StateFlow)
            val listaEjerciciosParaGuardar = viewModelEjercicio.ejerciciosRealizados.value?.map { ejercicio ->
                ejercicio.copy(entrenamientoRealizado = entrenamientoRealizadoId)
            } ?: emptyList()


            // 4. Lista local para almacenar los ejercicios creados
            val ejerciciosCreados = mutableListOf<EjercicioRealizado>()

            // 5. Guardar los ejercicios uno por uno con mejor manejo de errores
            for (ejercicio in listaEjerciciosParaGuardar) {
                try {
                    // Agregamos un breve delay entre llamadas para evitar sobrecarga
                    delay(300)

                    // Intentamos guardar con el método principal
                    var ejercicioCreado = viewModelEjercicio.new2(ejercicio)

                    // Si falla, intentamos con el método alternativo
                    if (ejercicioCreado == null) {
                        ejercicioCreado = viewModelEjercicio.new(ejercicio)
                    }


                    if (ejercicioCreado != null) {
                        ejerciciosCreados.add(ejercicioCreado)
                    } else {
                        // Si ambos métodos fallan, creamos un objeto local con la información que tenemos
                        // para que al menos las series se puedan guardar
                        val ejercicioLocal = EjercicioRealizado(
                            _id = "LOCAL_" + System.currentTimeMillis(), // ID temporal
                            entrenamientoRealizado = entrenamientoRealizadoId,
                            entrenamiento = ejercicio.entrenamiento,
                            ejercicio = ejercicio.ejercicio,
                            nombre = ejercicio.nombre
                        )
                        ejerciciosCreados.add(ejercicioLocal)
                    }
                } catch (e: Exception) {
                    Log.e("FalloERVM2.6", "Error al guardar ejercicio: ${e.message}", e)
                    // Continuamos con el siguiente ejercicio
                }
            }


            // 6. Ahora procesamos las series relacionando con los ejercicios creados
            val seriesParaGuardar = mutableListOf<SerieRealizada>()
            val seriesOriginales = viewModelSerie.seriesRealizadas.value ?: emptyList()


            // Mapa para búsqueda rápida de ejercicios
            val mapaEjercicios = ejerciciosCreados.associateBy { it.ejercicio }

            // 7. Preparar todas las series con los IDs de ejercicios correctos

            seriesOriginales.forEach { serie ->
                // Buscar el ejercicio realizado correspondiente
                val ejercicioRealizado = mapaEjercicios[serie.ejercicio]

                if (ejercicioRealizado != null) {
                    // Crear una copia de la serie con el ID del ejercicio realizado correcto
                    seriesParaGuardar.add(
                        serie.copy(ejercicioRealizado = ejercicioRealizado._id)
                    )
                } else {
                    Log.w("Fallo", "No se encontró ejercicio realizado para la serie: $serie")
                }
            }

            val seriesCreadas = mutableListOf<SerieRealizada>()
            // 8. Guardar todas las series preparadas
            for (serie in seriesParaGuardar) {
                try {
                    // Pequeño delay entre llamadas
                    delay(200)

                    val serieCreada = viewModelSerie.new(serie)
                    if (serieCreada != null) {
                        seriesCreadas.add(serieCreada)
                    }
                } catch (e: Exception) {
                    Log.e("FalloERVM4.5", "Error al guardar serie: ${e.message}", e)
                }
            }


            var idsEjercicios = mutableListOf<String>()
            var idsSeries = mutableListOf<String>()

            for (ejercicio in ejerciciosCreados) {
                idsEjercicios.add(ejercicio._id)
                for (serie in seriesCreadas) {
                    if (serie.ejercicioRealizado == ejercicio._id)
                    idsSeries.add(serie._id)
                }
                viewModelEjercicio.updateSeriesRealizadas(ejercicio._id, idsSeries)
                idsSeries.clear()
                delay(300)
            }
            updateEjerciciosRealizados(entrenamientoRealizadoId, idsEjercicios)
            // 9. Solo al final limpiamos las listas para evitar problemas
            viewModelEjercicio.vaciarLista()
            viewModelSerie.vaciarLista()

        } catch (e: Exception) {
            Log.e("FalloEntrenamientoRealizado", "Error al guardar: ${e.message}", e)
            throw e
        }
    }


    fun updateEjerciciosRealizados(_id: String, lista: List<String>) {
        viewModelScope.launch {
            _usuario.value?.let { currentUser ->
                val token = currentUser.token ?: return@launch // Si no hay token, no hacemos nada
                val success = repository.updateEjerciciosRealizados(_id, EjerciciosRealizadosRequest(lista), token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
            }
        }
    }
}