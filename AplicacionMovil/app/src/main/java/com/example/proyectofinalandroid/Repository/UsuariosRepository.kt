package com.example.proyectofinalandroid.Repository

import android.util.Log
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Remote.UsuariosApi
import javax.inject.Inject

class UsuariosRepository @Inject constructor(private val api: UsuariosApi) {

    suspend fun login(email: String, contrasena: String): LoginResponse? {
        val credentials = mapOf("email" to email, "contrasena" to contrasena)
        val response = api.login(credentials)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getAllUsuarios(token: String): List<Usuarios>? {
        val response = api.getAllUsuarios("Bearer $token")
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun updateUsuario(_id: String, updatedData: Map<String, String>, token: String): Boolean {
        val request = mutableMapOf<String, String>("_id" to _id)
        request.putAll(updatedData)
        val response = api.updateUsuario("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun deleteUsuario(dni: String, token: String): Boolean {
        val request = mapOf("dni" to dni)
        val response = api.deleteUsuario("Bearer $token", request)
        return response.isSuccessful
    }

    suspend fun getOneByEmail(email: String, contrasena: String, token: String): Usuarios? {
        val request = mapOf("email" to email, "contrasena" to contrasena)
        val response = api.getOneByEmail("Bearer $token", request)
        return if (response.isSuccessful) response.body() else null
    }

    // Método para registrar usuario sin token
    suspend fun registerWithoutToken(usuario: Usuarios): Usuarios? {
        val response = api.registerWithoutToken(usuario)
        if (!response.isSuccessful) {
            val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception(errorMsg)
        }
        return response.body()
    }
}
