package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Remote.EjerciciosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EjerciciosRepository @Inject constructor(private val api: EjerciciosApi) {

    suspend fun getAllEjercicios(token: String): List<Ejercicios>? {
        val response = api.getAllEjercicios("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateEjercicios(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.updateEjercicios("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun deleteEjercicios(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.deleteEjercicios("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOneEjercicio(_id: String, token: String): Ejercicios? {
        val request = mapOf("_id" to _id)
        val response = api.getOneEjercicio("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getFilterEjercicios(token: String, filtros: Map<String, String>): List<Ejercicios>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilterEjercicios(token, filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
