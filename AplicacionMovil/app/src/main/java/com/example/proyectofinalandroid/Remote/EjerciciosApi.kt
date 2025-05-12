package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface EjerciciosApi {

    // Endpoint para obtener todos los ejercicios
    @GET("ejercicios/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<Ejercicios>>

    // Endpoint para actualizar ejercicio (requiere token)
    @PATCH("ejercicios/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar ejercicio (requiere token)
    @HTTP(method = "DELETE", path = "ejercicios/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio (requiere token)
    @POST("ejercicios/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Ejercicios>

    @POST("ejercicios/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Ejercicios>>
}
