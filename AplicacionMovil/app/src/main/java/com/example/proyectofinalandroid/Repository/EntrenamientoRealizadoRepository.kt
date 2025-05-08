package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.EjerciciosRealizadosRequest
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Remote.EntrenamientoRealizadoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class EntrenamientoRealizadoRepository @Inject constructor(private val api: EntrenamientoRealizadoApi) {

    suspend fun getAll(token: String): List<EntrenamientoRealizado>? {
        val response = api.getAll("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun update(_id: String, updatedData: Map<String, Any>, token: String): Boolean {
        val request = mutableMapOf<String, Any>("_id" to _id)
        request.putAll(updatedData)
        val response = api.update("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun updateEjerciciosRealizados(_id: String, updatedData: EjerciciosRealizadosRequest, token: String): Boolean {
        val response = api.updateEjerciciosRealizados(_id = _id, auth = "Bearer $token", request = updatedData)
        return response.isSuccessful
    }

    suspend fun getUltimoEntrenamiento(usuarioId: String): EntrenamientoRealizado {
        return api.getLastEntrenamiento(usuarioId) // O filtrado manual desde todos
    }

    suspend fun delete(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.delete("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOne(_id: String, token: String): EntrenamientoRealizado? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun new(entrenar: EntrenamientoRealizado): EntrenamientoRealizado? {
        val response = api.new(entrenar)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<EntrenamientoRealizado>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilter(token, filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
