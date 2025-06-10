package com.example.proyectofinalandroid.Remote


import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Eventos
import retrofit2.Response
import retrofit2.http.*

interface EventosApi {

    // Endpoint para obtener todos los eventos (requiere token)
    @GET("eventos/getAll")
    suspend fun getAll(
        @Header("Authorization") auth: String
    ): Response<List<Eventos>>

    // Endpoint para actualizar eventos (requiere token)
    @PATCH("eventos/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar un evento  (requiere token)
    @HTTP(method = "DELETE", path = "eventos/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un evento (requiere token)
    @POST("eventos/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Eventos>

    // Endpoint para crear un evento sin token
    @POST("eventos/new")
    suspend fun new(
        @Header("Authorization") auth: String,
        @Body eventos: Eventos
    ): Response<Eventos>

    @POST("eventos/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Eventos>>

}
