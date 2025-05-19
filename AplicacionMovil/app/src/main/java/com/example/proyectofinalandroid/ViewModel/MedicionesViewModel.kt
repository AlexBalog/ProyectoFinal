package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Mediciones
import com.example.proyectofinalandroid.Model.TipoMedicion
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.MedicionesRepository
import com.example.proyectofinalandroid.Remote.EstadisticasResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MedicionesViewModel @Inject constructor(
    private val repository: MedicionesRepository
) : ViewModel() {

    // Estados
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // Mediciones
    private val _mediciones = MutableStateFlow<List<Mediciones>>(emptyList())
    val mediciones: StateFlow<List<Mediciones>> get() = _mediciones

    // Estadísticas
    private val _estadisticas = MutableStateFlow<EstadisticasResponse?>(null)
    val estadisticas: StateFlow<EstadisticasResponse?> get() = _estadisticas

    // Progreso calculado
    private val _progresoPeso = MutableStateFlow(0f)
    val progresoPeso: StateFlow<Float> get() = _progresoPeso

    fun setUsuario(usuario: Usuarios) {
        _usuario.value = usuario
    }

    suspend fun cargarMedicionesPorUsuario(
        tipo: TipoMedicion? = null,
        fechaInicio: Date? = null,
        fechaFin: Date? = null
    ): List<Mediciones>? {
        return try {
            _isLoading.value = true
            _usuario.value?.let { usuario ->
                val token = usuario.token ?: return@let null

                val mediciones = repository.obtenerMedicionesPorUsuario(
                    usuarioId = usuario._id,
                    token = token,
                    tipo = tipo,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

                if (mediciones != null) {
                    _mediciones.value = mediciones

                    // Calcular progreso si es necesario
                    if (tipo == TipoMedicion.PESO || tipo == null) {
                        calcularProgresoPeso(mediciones.filter { it.tipo == TipoMedicion.PESO.name })
                    }

                    mediciones
                } else {
                    _errorMessage.value = "No se pudieron obtener las mediciones"
                    null
                }
            } ?: null
        } catch (e: Exception) {
            _errorMessage.value = "Error: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    fun cargarEstadisticas(tipo: TipoMedicion = TipoMedicion.PESO) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _usuario.value?.let { usuario ->
                    val token = usuario.token ?: return@let

                    val estadisticas = repository.obtenerEstadisticas(
                        usuarioId = usuario._id,
                        token = token,
                        tipo = tipo
                    )

                    if (estadisticas != null) {
                        _estadisticas.value = estadisticas
                    } else {
                        _errorMessage.value = "No se pudieron obtener las estadísticas"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun crearMedicion(medicion: Mediciones) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _usuario.value?.let { usuario ->
                    val token = usuario.token ?: return@let

                    // Aseguramos que la medición está asociada al usuario actual
                    val medicionCompleta = medicion.copy(usuario = usuario._id)

                    val creada = repository.crearMedicion(medicionCompleta, token)
                    if (creada != null) {
                        // Añadir la nueva medición a la lista actual
                        _mediciones.value = _mediciones.value + creada

                        // Recargar estadísticas si es necesario
                        cargarEstadisticas(TipoMedicion.valueOf(creada.tipo))
                    } else {
                        _errorMessage.value = "No se pudo crear la medición"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarMedicion(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _usuario.value?.let { usuario ->
                    val token = usuario.token ?: return@let

                    val exito = repository.eliminarMedicion(id, token)
                    if (exito) {
                        // Eliminar la medición de la lista actual
                        _mediciones.value = _mediciones.value.filter { it._id != id }

                        // Recargar las estadísticas
                        val tipoEliminado = _mediciones.value.find { it._id == id }?.tipo
                        if (tipoEliminado != null) {
                            cargarEstadisticas(TipoMedicion.valueOf(tipoEliminado))
                        }
                    } else {
                        _errorMessage.value = "No se pudo eliminar la medición"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calcularProgresoPeso(medicionesPeso: List<Mediciones>) {
        _usuario.value?.let { usuario ->
            if (usuario.objetivoPeso <= 0f) {
                _progresoPeso.value = 0f
                return
            }

            // Si no hay mediciones, usar el peso inicial del usuario
            if (medicionesPeso.isEmpty()) {
                _progresoPeso.value = 0f
                return
            }

            // Ordenar mediciones por fecha
            val medicionesOrdenadas = medicionesPeso.sortedBy { it.fecha }

            // Usar el peso inicial del usuario como referencia
            val pesoInicial = usuario.peso

            // Última medición para el peso actual
            val ultimaMedicion = medicionesOrdenadas.lastOrNull()
            val pesoActual = ultimaMedicion?.valor ?: pesoInicial

            val objetivoPeso = usuario.objetivoPeso

            // Calcular progreso desde el peso inicial hacia el objetivo
            val progreso = if (objetivoPeso > pesoInicial) {
                // Objetivo: Aumentar peso
                (pesoActual - pesoInicial) / (objetivoPeso - pesoInicial)
            } else {
                // Objetivo: Perder peso
                (pesoInicial - pesoActual) / (pesoInicial - objetivoPeso)
            }

            // Registrar cálculo de progreso para depuración
            Log.d("MedicionesViewModel", "Progreso calculado: $pesoInicial → $pesoActual, objetivo: $objetivoPeso, progreso: $progreso")

            // Actualizar valor de progreso, asegurando que esté entre 0-100%
            _progresoPeso.value = (progreso * 100f).coerceIn(0f, 100f)
        }
    }

    // Función para crear rápidamente una medición de peso
    // In MedicionesViewModel.kt
    fun registrarPeso(valor: Float, notas: String = "") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _usuario.value?.let { usuario ->
                    val token = usuario.token ?: return@let

                    val nuevaMedicion = Mediciones(
                        usuario = usuario._id,
                        fecha = Date(),
                        tipo = TipoMedicion.PESO.name,
                        unidad = "kg",
                        valor = valor,
                        notas = notas
                    )

                    val creada = repository.crearMedicion(nuevaMedicion, token)
                    if (creada != null) {
                        // Actualizar la lista de mediciones inmediatamente con la nueva medición
                        val updatedMediciones = _mediciones.value + creada
                        _mediciones.value = updatedMediciones

                        // Calcular progreso inmediatamente con el nuevo valor
                        calcularProgresoPeso(updatedMediciones.filter { it.tipo == TipoMedicion.PESO.name })

                        // También actualizar las estadísticas
                        cargarEstadisticas(TipoMedicion.PESO)
                    } else {
                        _errorMessage.value = "No se pudo crear la medición"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función para crear rápidamente una medición de calorías
    fun registrarCalorias(valor: Float, notas: String = "") {
        val nuevaMedicion = Mediciones(
            usuario = _usuario.value?._id ?: "",
            fecha = Date(),
            tipo = TipoMedicion.CALORIAS.name,
            unidad = "kcal",
            valor = valor,
            notas = notas
        )
        crearMedicion(nuevaMedicion)
    }


    suspend fun verificarYCrearMedicionInicial() {
        try {
            _usuario.value?.let { usuario ->
                // Si el usuario tiene peso inicial configurado
                if (usuario.peso > 0f) {
                    // Primero asegúrate de que las mediciones estén cargadas completamente
                    // Espera a que se complete la carga antes de verificar
                    val medicionesActuales = _mediciones.value

                    // Verifica si existen mediciones de peso
                    val hayMedicionesDePeso = medicionesActuales.any { it.tipo == TipoMedicion.PESO.name }

                    Log.d("MedicionesViewModel", "Verificando mediciones: ${medicionesActuales.size} total, peso: $hayMedicionesDePeso")

                    // Solo si no hay mediciones de peso, creamos la inicial
                    if (!hayMedicionesDePeso) {
                        Log.d("MedicionesViewModel", "Creando medición inicial con peso: ${usuario.peso}")

                        // Crear la medición con la fecha actual pero restando unos días
                        val fechaInicial = Calendar.getInstance()
                        fechaInicial.add(Calendar.DAY_OF_MONTH, -7) // 7 días antes

                        val medicionInicial = Mediciones(
                            usuario = usuario._id,
                            fecha = fechaInicial.time, // Fecha una semana antes
                            tipo = TipoMedicion.PESO.name,
                            unidad = "kg",
                            valor = usuario.peso,
                            notas = "Medición inicial automática"
                        )

                        // Registrar la medición inicial y esperar a que se complete
                        registrarMedicionSilenciosa(medicionInicial)
                    } else {
                        Log.d("MedicionesViewModel", "Ya existen mediciones de peso, no se crea medición inicial")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MedicionesViewModel", "Error al verificar mediciones iniciales: ${e.message}")
        }
    }



    private suspend fun registrarMedicionSilenciosa(medicion: Mediciones) {
        try {
            _usuario.value?.let { usuario ->
                val token = usuario.token ?: return@let

                val creada = repository.crearMedicion(medicion, token)
                if (creada != null) {
                    // Actualizar la lista de mediciones y recalcular
                    val updatedMediciones = _mediciones.value + creada
                    _mediciones.value = updatedMediciones

                    // Recalcular progreso
                    if (medicion.tipo == TipoMedicion.PESO.name) {
                        calcularProgresoPeso(updatedMediciones.filter { it.tipo == TipoMedicion.PESO.name })
                    }

                    // Actualizar estadísticas
                    cargarEstadisticas(TipoMedicion.valueOf(medicion.tipo))

                    Log.d("MedicionesViewModel", "Medición inicial creada exitosamente")
                } else {
                    Log.e("MedicionesViewModel", "No se pudo crear la medición inicial")
                }
            }
        } catch (e: Exception) {
            Log.e("MedicionesViewModel", "Error al registrar medición silenciosa: ${e.message}")
        }
    }
}