package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.EventosUsuario
import com.example.proyectofinalandroid.Remote.EventosUsuarioApi
import javax.inject.Inject

class EventosUsuarioRepository @Inject constructor(private val api: EventosUsuarioApi) {

    suspend fun getAll(token: String): List<EventosUsuario>? {
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

    suspend fun getOne(_id: String, token: String): EventosUsuario? {
        val request = mapOf("_id" to _id)
        val response = api.getOne("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun new(eventoUsuario: EventosUsuario): EventosUsuario? {
        val response = api.new(eventoUsuario)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }
}
