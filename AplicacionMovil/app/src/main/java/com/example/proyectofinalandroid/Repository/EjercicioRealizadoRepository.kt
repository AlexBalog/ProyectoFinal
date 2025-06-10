package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Remote.EjercicioRealizadoApi
import javax.inject.Inject
import android.util.Log
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.SeriesRealizadasRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException


class EjercicioRealizadoRepository @Inject constructor(private val api: EjercicioRealizadoApi) {

    suspend fun getAll(token: String): List<EjercicioRealizado>? {
        val response = api.getAll("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun update(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.update("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun updateSeriesRealizadas(_id: String, updatedData: SeriesRealizadasRequest, token: String): Boolean {
        val response = api.updateSeriesRealizadas(_id = _id, auth = "Bearer $token", request = updatedData)
        return response.isSuccessful
    }

    suspend fun delete(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.delete("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOne(_id: String, token: String): EjercicioRealizado? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<EjercicioRealizado>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilter("Bearer $token", filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }

    suspend fun new(ejercicioRealizado: EjercicioRealizado, token: String): EjercicioRealizado? {
        return withContext(Dispatchers.IO) {
            try {

                val response = api.new(ejercicioRealizado = ejercicioRealizado, auth = "Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()

                    // IMPORTANTE: Validar explícitamente todos los campos requeridos
                    if (body != null && body._id.isNotEmpty()) {
                        // Copiar el ejercicio enviado si hay campos que faltan en la respuesta
                        val resultado = body.copy(
                            ejercicio = body.ejercicio ?: ejercicioRealizado.ejercicio,
                            nombre = body.nombre ?: ejercicioRealizado.nombre,
                            entrenamiento = body.entrenamiento ?: ejercicioRealizado.entrenamiento,
                            entrenamientoRealizado = body.entrenamientoRealizado ?: ejercicioRealizado.entrenamientoRealizado
                        )
                        return@withContext resultado
                    } else {
                        return@withContext null
                    }
                } else {
                    // Capturar el error para diagnóstico
                    val errorBody = response.errorBody()?.string()
                    return@withContext null
                }
            } catch (e: HttpException) {
                return@withContext null
            } catch (e: IOException) {
                return@withContext null
            } catch (e: Exception) {
                return@withContext null
            }
        }
    }
}
