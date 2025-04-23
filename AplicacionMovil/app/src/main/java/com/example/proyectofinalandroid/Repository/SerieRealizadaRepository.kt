package com.example.proyectofinalandroid.Repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.Remote.SerieRealizadaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SerieRealizadaRepository @Inject constructor(private val api: SerieRealizadaApi) {

    suspend fun getAll(token: String): List<SerieRealizada>? {
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

    suspend fun getOne(_id: String, token: String): SerieRealizada? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getFilter(token: String, filtros: Map<String, String>): List<SerieRealizada>? {
        return withContext(Dispatchers.IO) {
            val response = api.getFilter(token, filtros)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        }
    }

    suspend fun new(serie: SerieRealizada): SerieRealizada? {
        val response = api.new(serie)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }
}
