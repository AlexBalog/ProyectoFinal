package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
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
    @DELETE("entrenamientos/delete")
    suspend fun eliminarEntrenamiento(
        @Path("id") entrenamientoId: String,
        @Header("Authorization") authorization: String
    ): Response<Unit>

    @POST("entrenamientos/new") suspend fun new(
        @Header("Authorization") auth: String,
        @Body entrenamientos: Entrenamientos
    ): Response<Entrenamientos>

    @PATCH("entrenamientos/{id}")
    suspend fun actualizarEntrenamiento(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body entrenamiento: Entrenamientos
    ): Response<Entrenamientos>

    // Endpoint para obtener un entrenamiento (requiere token)
    @POST("entrenamientos/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Entrenamientos>

    // Endpoint para hacer petición de entrenamiento nuevo
    @POST("entrenamientos/peticion")
    suspend fun peticion(
        @Header("Authorization") auth: String,
        @Body entrenamientos: Entrenamientos
    ): Response<Entrenamientos>

    @POST("entrenamientos/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Entrenamientos>>
}
