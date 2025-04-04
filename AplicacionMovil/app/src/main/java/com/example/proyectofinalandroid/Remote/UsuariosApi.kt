package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface UsuariosApi {

    // Endpoint de login: NO requiere token
    @POST("auth/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<LoginResponse>

    // Endpoint para obtener todos los usuarios (requiere token)
    @GET("usuarios/getAll")
    suspend fun getAllUsuarios(@Header("Authorization") auth: String): Response<List<Usuarios>>

    // Endpoint para actualizar usuario (requiere token)
    @PATCH("usuarios/update")
    suspend fun updateUsuario(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar usuario (requiere token)
    @HTTP(method = "DELETE", path = "user/delete", hasBody = true)
    suspend fun deleteUsuario(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un usuario por email (requiere token)
    @POST("usuarios/getOneEmail")
    suspend fun getOneByEmail(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Usuarios>

    // Endpoint para registrar usuario sin token
    @POST("usuarios/register")
    suspend fun registerWithoutToken(@Body usuario: Usuarios): Response<Usuarios>
}
