package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Likes
import com.example.proyectofinalandroid.Remote.LikesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LikesRepository @Inject constructor(private val api: LikesApi) {

    suspend fun getAll(token: String): List<Likes>? {
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

    suspend fun getOne(_id: String, token: String): Likes? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun new(likes: Likes): Likes? {
        val response = api.new(likes)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<Likes>? {
        return withContext(Dispatchers.IO) {
            Log.d("FalloRepos1", "$filtros y token $token")
            val response = api.getFilter(token, filtros)
            Log.d("FalloRepos2", "${response.isSuccessful}")
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }
}
