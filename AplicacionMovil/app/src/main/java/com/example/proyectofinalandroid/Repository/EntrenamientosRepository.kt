package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Remote.EntrenamientosApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntrenamientosRepository @Inject constructor(private val api: EntrenamientosApi) {

    suspend fun getAll(token: String): List<Entrenamientos>? {
        val response = api.getAll("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun update(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.update("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOne(_id: String, token: String): Entrenamientos? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun actualizarEntrenamiento(entrenamiento: Entrenamientos, token: String): Entrenamientos? {
        return try {
            val response = api.actualizarEntrenamiento(
                "Bearer $token",
                entrenamiento._id,
                entrenamiento
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("EntrenamientosRepo", "Error al actualizar: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("EntrenamientosRepo", "Excepción al actualizar entrenamiento: ${e.message}")
            null
        }
    }

    suspend fun peticion(entrenamientos: Entrenamientos, token: String): Entrenamientos? {
        val response = api.peticion(entrenamientos = entrenamientos, auth = "Bearer $token")
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun new(entrenamiento: Entrenamientos, token: String): Entrenamientos? {
        val token = "Bearer $token"
        val response = api.new(entrenamientos = entrenamiento, auth = token)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<Entrenamientos>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilter("Bearer $token", filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }

    suspend fun eliminarEntrenamiento(entrenamientoId: String, token: String): Boolean {
        return try {
            val response = api.eliminarEntrenamiento(
                entrenamientoId = entrenamientoId,
                authorization = "Bearer $token"
            )

            response.isSuccessful
        } catch (e: Exception) {
            Log.e("EntrenamientosRepository", "Error al eliminar entrenamiento: ${e.message}")
            false
        }
    }
}
