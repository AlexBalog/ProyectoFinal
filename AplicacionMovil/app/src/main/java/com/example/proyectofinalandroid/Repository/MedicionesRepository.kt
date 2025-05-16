package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.Mediciones
import com.example.proyectofinalandroid.Model.TipoMedicion
import com.example.proyectofinalandroid.Remote.EstadisticasResponse
import com.example.proyectofinalandroid.Remote.MedicionesApi
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicionesRepository @Inject constructor(
    private val api: MedicionesApi
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun obtenerMedicionesPorUsuario(
        usuarioId: String,
        token: String,
        tipo: TipoMedicion? = null,
        fechaInicio: Date? = null,
        fechaFin: Date? = null
    ): List<Mediciones>? {
        try {
            val tipoString = tipo?.name
            val fechaInicioString = fechaInicio?.let { dateFormat.format(it) }
            val fechaFinString = fechaFin?.let { dateFormat.format(it) }

            val response = api.obtenerMedicionesPorUsuario(
                usuarioId = usuarioId,
                token = "Bearer $token",
                tipo = tipoString,
                fechaInicio = fechaInicioString,
                fechaFin = fechaFinString
            )

            if (response.isSuccessful) {
                return response.body()?.mediciones
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun crearMedicion(medicion: Mediciones, token: String): Mediciones? {
        try {
            val response = api.crearMedicion(medicion, "Bearer $token")
            if (response.isSuccessful) {
                return response.body()
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun actualizarMedicion(id: String, medicion: Mediciones, token: String): Mediciones? {
        try {
            val response = api.actualizarMedicion(id, medicion, "Bearer $token")
            if (response.isSuccessful) {
                return response.body()
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun eliminarMedicion(id: String, token: String): Boolean {
        try {
            val response = api.eliminarMedicion(id, "Bearer $token")
            return response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun obtenerEstadisticas(
        usuarioId: String,
        token: String,
        tipo: TipoMedicion = TipoMedicion.PESO
    ): EstadisticasResponse? {
        try {
            val response = api.obtenerEstadisticasPorUsuario(
                usuarioId = usuarioId,
                token = "Bearer $token",
                tipo = tipo.name
            )

            if (response.isSuccessful) {
                return response.body()
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}