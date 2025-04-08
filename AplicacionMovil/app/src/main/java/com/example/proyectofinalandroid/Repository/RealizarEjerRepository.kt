package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.RealizarEjer
import com.example.proyectofinalandroid.Remote.EntrenarApi
import com.example.proyectofinalandroid.Remote.RealizarEjerApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RealizarEjerRepository @Inject constructor(private val api: RealizarEjerApi) {

    suspend fun getAllRealizarEjer(token: String): List<RealizarEjer>? {
        val response = api.getAllRealizarEjer("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateRealizarEjer(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.updateRealizarEjer("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun deleteRealizarEjer(_id: String, token: String): Boolean {
        val request = mapOf("_id" to _id)
        val response = api.deleteRealizarEjer("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOneRealizarEjer(_id: String, token: String): RealizarEjer? {
        val request = mapOf("_id" to _id)
        val response = api.getOneRealizarEjer("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun newRealizarEjer(realizarEjer: RealizarEjer): RealizarEjer? {
        val response = api.newRealizarEjer(realizarEjer)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }
}
