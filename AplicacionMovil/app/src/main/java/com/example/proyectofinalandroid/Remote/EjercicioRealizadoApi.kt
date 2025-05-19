package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.SeriesRealizadasRequest
import retrofit2.Response
import retrofit2.http.*

interface EjercicioRealizadoApi {

    // Endpoint para obtener todos los ejercicios realizados (requiere token)
    @GET("ejercicioRealizado/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<EjercicioRealizado>>

    // Endpoint para actualizar ejercicio realizado (requiere token)
    @PATCH("ejercicioRealizado/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    @PATCH("ejercicioRealizado/update/{id}")
    suspend fun updateSeriesRealizadas(
        @Path("id") _id: String,
        @Header("Authorization") auth: String,
        @Body request: SeriesRealizadasRequest
    ): Response<Map<String, String>>

    // Endpoint para eliminar ejercicio realizado (requiere token)
    @HTTP(method = "DELETE", path = "ejercicioRealizado/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio realizado (requiere token)
    @POST("ejercicioRealizado/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<EjercicioRealizado>

    // Endpoint para registrar ejercicio realizado sin token
    @POST("ejercicioRealizado/new")
    suspend fun new(@Body ejercicioRealizado: EjercicioRealizado): Response<EjercicioRealizado>

    @POST("ejercicioRealizado/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<EjercicioRealizado>>
}
