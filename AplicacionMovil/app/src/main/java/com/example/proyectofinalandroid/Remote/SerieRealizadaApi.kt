package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.SerieRealizada
import retrofit2.Response
import retrofit2.http.*

interface SerieRealizadaApi {

    // Endpoint para obtener todos los ejercicios
    @GET("serieRealizada/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<SerieRealizada>>

    // Endpoint para actualizar ejercicio (requiere token)
    @PATCH("serieRealizada/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar ejercicio (requiere token)
    @HTTP(method = "DELETE", path = "serieRealizada/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio (requiere token)
    @POST("serieRealizada/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<SerieRealizada>

    @GET("serieRealizada/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<SerieRealizada>>
}
