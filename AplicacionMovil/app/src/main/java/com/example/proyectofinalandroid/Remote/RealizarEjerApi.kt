package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.RealizarEjer
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface RealizarEjerApi {

    // Endpoint para obtener todos los ejercicios realizados (requiere token)
    @GET("realizarejer/getAll")
    suspend fun getAllRealizarEjer(@Header("Authorization") auth: String): Response<List<RealizarEjer>>

    // Endpoint para actualizar ejercicio realizado (requiere token)
    @PATCH("realizarejer/update")
    suspend fun updateRealizarEjer(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar ejercicio realizado (requiere token)
    @HTTP(method = "DELETE", path = "realizarejer/delete", hasBody = true)
    suspend fun deleteRealizarEjer(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio realizado (requiere token)
    @POST("realizarejer/getOneRealizarEjer")
    suspend fun getOneRealizarEjer(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<RealizarEjer>

    // Endpoint para registrar ejercicio realizado sin token
    @POST("realizarejer/new")
    suspend fun newRealizarEjer(@Body realizarEjer: RealizarEjer): Response<RealizarEjer>

}
