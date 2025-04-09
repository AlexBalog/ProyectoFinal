package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import retrofit2.Response
import retrofit2.http.*

interface EntrenamientoRealizadoApi {

    // Endpoint para obtener todos los entrenos (requiere token)
    @GET("entrenamientoRealizado/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<EntrenamientoRealizado>>

    // Endpoint para actualizar entreno (requiere token)
    @PATCH("entrenamientoRealizado/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar entreno (requiere token)
    @HTTP(method = "DELETE", path = "entrenamientoRealizado/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un entreno (requiere token)
    @POST("entrenamientoRealizado/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<EntrenamientoRealizado>

    // Endpoint para registrar entreno sin token
    @POST("entrenamientoRealizado/new")
    suspend fun new(@Body entrenar: EntrenamientoRealizado): Response<EntrenamientoRealizado>

    @GET("entrenamientoRealizado/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<EntrenamientoRealizado>>
}
