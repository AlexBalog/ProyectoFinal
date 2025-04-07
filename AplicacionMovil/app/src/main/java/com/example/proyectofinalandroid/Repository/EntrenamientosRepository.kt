package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Remote.EntrenamientosApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
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

    suspend fun getOneByEmail(_id: String, token: String): Entrenamientos? {
        val request = mapOf("_id" to _id)
        val response = api.getOneEntrenamiento("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }
}
