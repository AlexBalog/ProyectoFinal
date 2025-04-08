package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface EjerciciosApi {

    // Endpoint para obtener todos los ejercicios
    @GET("ejercicios/getAll")
    suspend fun getAllEjercicios(@Header("Authorization") auth: String): Response<List<Ejercicios>>

    // Endpoint para actualizar ejercicio (requiere token)
    @PATCH("ejercicios/update")
    suspend fun updateEjercicios(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar ejercicio (requiere token)
    @HTTP(method = "DELETE", path = "ejercicios/delete", hasBody = true)
    suspend fun deleteEjercicios(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio (requiere token)
    @POST("ejercicios/getOneEjercicio")
    suspend fun getOneEjercicio(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Ejercicios>

    @GET("ejercicios/getFilterEjercicios")
    suspend fun getFilterEjercicios(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Ejercicios>>
}
