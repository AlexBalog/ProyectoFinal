package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Remote.EjercicioRealizadoApi
import javax.inject.Inject
import android.util.Log
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

    suspend fun new(ejercicioRealizado: EjercicioRealizado): EjercicioRealizado? {
        return withContext(Dispatchers.IO) {
            try {
                // Log detallado de lo que estamos enviando
                Log.d("FalloRepository1", "Enviando: $ejercicioRealizado")

                // Realizar la llamada a la API
                val response = api.new(ejercicioRealizado)

                // Log de la respuesta bruta para diagnóstico
                Log.d("FalloRepository2", "Respuesta bruta: ${response.raw()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("FalloRepository3", "Respuesta código: ${response.code()} body: $body")

                    // IMPORTANTE: Validar explícitamente todos los campos requeridos
                    if (body != null && body._id.isNotEmpty()) {
                        // Copiar el ejercicio enviado si hay campos que faltan en la respuesta
                        val resultado = body.copy(
                            ejercicio = body.ejercicio ?: ejercicioRealizado.ejercicio,
                            nombre = body.nombre ?: ejercicioRealizado.nombre,
                            entrenamiento = body.entrenamiento ?: ejercicioRealizado.entrenamiento,
                            entrenamientoRealizado = body.entrenamientoRealizado ?: ejercicioRealizado.entrenamientoRealizado
                        )
                        Log.d("FalloRepository4", "Objeto final: $resultado")
                        return@withContext resultado
                    } else {
                        Log.e("FalloRepository5", "Respuesta body vacío o sin ID")
                        return@withContext null
                    }
                } else {
                    // Capturar el error para diagnóstico
                    val errorBody = response.errorBody()?.string()
                    Log.e("FalloRepository6", "Error HTTP ${response.code()}: $errorBody")
                    return@withContext null
                }
            } catch (e: HttpException) {
                Log.e("FalloRepository7", "Error HTTP: ${e.code()}", e)
                return@withContext null
            } catch (e: IOException) {
                Log.e("FalloRepository8", "Error de red: ${e.message}", e)
                return@withContext null
            } catch (e: Exception) {
                Log.e("FalloRepository9", "Error desconocido: ${e.message}", e)
                return@withContext null
            }
        }
    }
}
