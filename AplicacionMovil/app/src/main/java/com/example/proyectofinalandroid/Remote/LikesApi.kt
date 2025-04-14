package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Likes
import retrofit2.Response
import retrofit2.http.*

interface LikesApi {

    // Endpoint para obtener todos los likes
    @GET("likes/getAll")
    suspend fun getAll(@Header("Authorization") auth: String): Response<List<Likes>>

    // Endpoint para actualizar ejercicio (requiere token)
    @PATCH("likes/update")
    suspend fun update(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    @POST("likes/new")
    suspend fun new(@Body likes: Likes): Response<Likes>

    // Endpoint para eliminar ejercicio (requiere token)
    @HTTP(method = "DELETE", path = "likes/delete", hasBody = true)
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Map<String, String>>

    // Endpoint para obtener un ejercicio (requiere token)
    @POST("likes/getOne")
    suspend fun getOne(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<Likes>

    @POST("likes/getFilter")
    suspend fun getFilter(
        @Header("Authorization") auth: String,
        @Body request: Map<String, String>
    ): Response<List<Likes>>
}
