package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface EntrenarApi {

    // Endpoint para obtener todos los entrenos (requiere token)
    @GET("entrenar/getAll")
    suspend fun getAllEntrenar(@Header("Authorization") auth: String): Response<List<Entrenar>>

    // Endpoint para actualizar entreno (requiere token)
    @PATCH("entrenar/update")
    suspend fun updateEntrenar(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar entreno (requiere token)
    @HTTP(method = "DELETE", path = "entrenar/delete", hasBody = true)
    suspend fun deleteEntrenar(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un entreno (requiere token)
    @POST("entrenar/getOneEntrenar")
    suspend fun getOneEntrenar(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Entrenar>

    // Endpoint para registrar entreno sin token
    @POST("entrenar/new")
    suspend fun newEntrenar(@Body entrenar: Entrenar): Response<Entrenar>

    @GET("entrenar/getFilterEntrenar")
    suspend fun getFilterEntrenar(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Entrenar>>
}
