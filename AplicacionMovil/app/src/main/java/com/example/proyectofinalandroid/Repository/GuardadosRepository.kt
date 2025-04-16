package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Guardados
import com.example.proyectofinalandroid.Remote.GuardadosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GuardadosRepository @Inject constructor(private val api: GuardadosApi) {

    suspend fun getAll(token: String): List<Guardados>? {
        val response = api.getAll("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun update(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.update("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun delete(datos: Map<String, String>, token: String): Boolean {
        val response = api.delete("Bearer $token", datos)
        return response.isSuccessful
    }

    suspend fun getOne(_id: String, token: String): Guardados? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun new(guardados: Guardados): Guardados? {
        val response = api.new(guardados)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<Guardados>? {
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
