package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.Model.Mensaje
import com.example.proyectofinalandroid.Model.NuevaConversacion
import com.example.proyectofinalandroid.Model.NuevoMensaje
import retrofit2.http.*

interface IAService {
    @GET("ia/conversaciones/{id}")
    suspend fun obtenerConversaciones(
        @Header("Authorization") token: String,
        @Path("id") usuarioId: String
    ): List<Conversacion>

    @POST("ia/conversaciones")
    suspend fun crearConversacion(
        @Header("Authorization") token: String,
        @Body nuevaConversacion: NuevaConversacion
    ): Conversacion

    @GET("ia/conversaciones/{id}/mensajes")
    suspend fun obtenerMensajes(
        @Header("Authorization") token: String,
        @Path("id") conversacionId: String
    ): List<Mensaje>

    @POST("ia/conversaciones/{id}/mensajes")
    suspend fun enviarMensaje(
        @Header("Authorization") token: String,
        @Path("id") conversacionId: String,
        @Body request: Map<String, String>
    ): Map<String, Mensaje>

    @PATCH("ia/conversaciones/{conversacionId}")
    suspend fun actualizarConversacion(
        @Header("Authorization") token: String,
        @Path("conversacionId") conversacionId: String,
        @Body request: Map<String, String>
    ): Conversacion?

    @DELETE("ia/conversaciones/{conversacionId}")
    suspend fun eliminarConversacion(
        @Header("Authorization") token: String,
        @Path("conversacionId") conversacionId: String
    ): Map<String, String>?
}