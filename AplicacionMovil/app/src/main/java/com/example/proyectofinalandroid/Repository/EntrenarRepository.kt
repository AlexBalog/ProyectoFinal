package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Remote.EntrenarApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntrenarRepository @Inject constructor(private val api: EntrenarApi) {

    suspend fun getAllEntrenar(token: String): List<Entrenar>? {
        val response = api.getAllEntrenar("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateEntrenar(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.updateEntrenar("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun deleteEntrenar(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.deleteEntrenar("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOneEntrenar(_id: String, token: String): Entrenar? {
        val request = mapOf("_id" to _id)
        val response = api.getOneEntrenar("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun newEntrenar(entrenar: Entrenar): Entrenar? {
        val response = api.newEntrenar(entrenar)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilterEntrenar(token: String, filtros: Map<String, String>): List<Entrenar>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilterEntrenar(token, filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
