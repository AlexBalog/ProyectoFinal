package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Remote.EntrenamientosApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntrenamientosRepository @Inject constructor(private val api: EntrenamientosApi) {

    suspend fun getAllEntrenamientos(token: String): List<Entrenamientos>? {
        val response = api.getAllEntrenamientos("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateEntrenamiento(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.updateEntrenamiento("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun deleteEntrenamiento(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.deleteEntrenamiento("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOneEntrenamiento(_id: String, token: String): Entrenamientos? {
        val request = mapOf("_id" to _id)
        val response = api.getOneEntrenamiento("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun peticionEntrenamiento(entrenamientos: Entrenamientos): Entrenamientos? {
        val response = api.peticionEntrenamiento(entrenamientos)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilterEntrenamientos(token: String, filtros: Map<String, String>): List<Entrenamientos>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilterEntrenamientos(token, filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
