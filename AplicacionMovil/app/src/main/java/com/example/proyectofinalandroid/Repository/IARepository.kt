package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.Model.Mensaje
import com.example.proyectofinalandroid.Model.NuevaConversacion
import com.example.proyectofinalandroid.Model.NuevoMensaje
import com.example.proyectofinalandroid.Remote.IAService
import javax.inject.Inject

class IARepository @Inject constructor(private val iaService: IAService) {

    suspend fun obtenerConversaciones(token: String, usuarioId: String): List<Conversacion>? {
        return try {
            iaService.obtenerConversaciones("Bearer $token", usuarioId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearConversacion(token: String, nuevaConversacion: NuevaConversacion): Conversacion? {
        return try {
            iaService.crearConversacion("Bearer $token", nuevaConversacion)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerMensajes(token: String, conversacionId: String): List<Mensaje>? {
        return try {
            iaService.obtenerMensajes("Bearer $token", conversacionId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun enviarMensaje(token: String, conversacionId: String, request: Map<String, String>): Map<String, Mensaje>? {
        return try {
            iaService.enviarMensaje("Bearer $token", conversacionId, request)
        } catch (e: Exception) {
            null
        }
    }
}