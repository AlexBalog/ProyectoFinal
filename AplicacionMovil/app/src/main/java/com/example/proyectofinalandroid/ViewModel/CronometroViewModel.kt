package com.example.proyectofinalandroid.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.View.formatearTiempo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CronometroViewModel : ViewModel() {
    // Estado del tiempo como StateFlow para evitar recomposiciones innecesarias
    private val _tiempoTranscurrido = MutableStateFlow(0L)
    val tiempoTranscurrido: StateFlow<Long> = _tiempoTranscurrido.asStateFlow()

    // Estado activo
    private val _activo = MutableStateFlow(true)
    val activo: StateFlow<Boolean> = _activo.asStateFlow()

    // Job para el cronómetro
    private var cronometroJob: Job? = null

    // Tiempo de inicio para cálculos precisos
    private var startTimeMillis = 0L

    init {
        startTimeMillis = System.currentTimeMillis()
    }

    fun iniciarCronometro() {
        if (_activo.value && cronometroJob == null) {
            startTimeMillis = System.currentTimeMillis() - (_tiempoTranscurrido.value * 1000)

            cronometroJob = viewModelScope.launch {
                while (isActive && _activo.value) {
                    _tiempoTranscurrido.value = (System.currentTimeMillis() - startTimeMillis) / 1000
                    delay(1000) // Actualizar cada segundo
                }
            }
        }
    }

    fun detenerCronometro() {
        _activo.value = false
        cronometroJob?.cancel()
        cronometroJob = null
    }

    fun getTiempoFormateado(): String {
        return formatearTiempo(_tiempoTranscurrido.value)
    }

    override fun onCleared() {
        super.onCleared()
        cronometroJob?.cancel()
    }
}