package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Guardados
import retrofit2.Response
import retrofit2.http.*

interface GuardadosApi {

    // Endpoint para obtener todos los guardados
    @GET("guardados/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<Guardados>>

    // Endpoint para actualizar ejercicio (requiere token)
    @PATCH("guardados/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    @POST("guardados/new")
    suspend fun new(@Body guardados: Guardados): Response<Guardados>

    // Endpoint para eliminar ejercicio (requiere token)
    @HTTP(method = "DELETE", path = "guardados/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio (requiere token)
    @POST("guardados/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Guardados>

    @POST("guardados/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Guardados>>
}
