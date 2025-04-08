package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Entrenar
import com.example.proyectofinalandroid.Model.LoginResponse
import com.example.proyectofinalandroid.Model.Usuarios
import retrofit2.Response
import retrofit2.http.*

interface EntrenamientosApi {

    // Endpoint para obtener todos los entrenamientos (requiere token)
    @GET("entrenamientos/getAll")
    suspend fun getAllEntrenamientos(@Header("Authorization") auth: String): Response<List<Entrenamientos>>

    // Endpoint para actualizar un entrenamiento (requiere token)
    @PATCH("entrenamientos/update")
    suspend fun updateEntrenamiento(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para eliminar un entrenamiento (requiere token)
    @HTTP(method = "DELETE", path = "entrenamientos/delete", hasBody = true)
    suspend fun deleteEntrenamiento(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un entrenamiento (requiere token)
    @POST("entrenamientos/getOneEntrenamiento")
    suspend fun getOneEntrenamiento(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Entrenamientos>

    // Endpoint para hacer petición de entrenamiento nuevo
    @POST("entrenamiento/peticion")
    suspend fun peticionEntrenamiento(@Body entrenamientos: Entrenamientos): Response<Entrenamientos>

    @GET("entrenamientos/getFilterEntrenamientos")
    suspend fun getFilterEntrenamientos(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Entrenamientos>>
}
