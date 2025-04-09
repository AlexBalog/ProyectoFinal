package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.EventosUsuario
import retrofit2.Response
import retrofit2.http.*

interface EventosUsuarioApi {

    // Endpoint para obtener todos los eventos de usuario (requiere token)
    @GET("eventosUsuario/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<EventosUsuario>>

    // Endpoint para actualizar eventos de usuario(requiere token)
    @PATCH("eventosUsuario/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar un evento de usuario (requiere token)
    @HTTP(method = "DELETE", path = "eventosUsuario/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un evento de usuario(requiere token)
    @POST("eventosUsuario/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<EventosUsuario>

    // Endpoint para crear un evento de usuario sin token
    @POST("eventosUsuario/new")
    suspend fun new(@Body eventosUsuario: EventosUsuario): Response<EventosUsuario>

}
