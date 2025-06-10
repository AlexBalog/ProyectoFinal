package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Remote.EjerciciosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EjerciciosRepository @Inject constructor(private val api: EjerciciosApi) {

    suspend fun getAll(token: String): List<Ejercicios>? {
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

    suspend fun getOne(_id: String, token: String): Ejercicios? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<Ejercicios>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilter("Bearer $token", filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
