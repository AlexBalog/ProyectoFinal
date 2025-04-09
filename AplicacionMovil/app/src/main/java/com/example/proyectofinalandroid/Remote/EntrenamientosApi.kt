package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Entrenamientos
import retrofit2.Response
import retrofit2.http.*

interface EntrenamientosApi {

    // Endpoint para obtener todos los entrenamientos (requiere token)
    @GET("entrenamientos/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<Entrenamientos>>

    // Endpoint para actualizar un entrenamiento (requiere token)
    @PATCH("entrenamientos/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar un entrenamiento (requiere token)
    @HTTP(method = "DELETE", path = "entrenamientos/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un entrenamiento (requiere token)
    @POST("entrenamientos/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Entrenamientos>

    // Endpoint para hacer petición de entrenamiento nuevo
    @POST("entrenamientos/peticion")
    suspend fun peticion(@Body entrenamientos: Entrenamientos): Response<Entrenamientos>

    @GET("entrenamientos/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Entrenamientos>>
}
