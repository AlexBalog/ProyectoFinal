package com.example.proyectofinalandroid.Repository

import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.Model.Mensaje
import com.example.proyectofinalandroid.Model.NuevaConversacion
import com.example.proyectofinalandroid.Model.NuevoMensaje
import com.example.proyectofinalandroid.Remote.IAService
import javax.inject.Inject
import android.util.Log

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


    suspend fun actualizarTituloConversacion(token: String, conversacionId: String, nuevoTitulo: String): Boolean {
        return try {
            // Creamos un mapa con el nuevo título para enviar en la solicitud
            val request = mapOf("titulo" to nuevoTitulo)

            // Hacemos la llamada a la API
            val respuesta = iaService.actualizarConversacion("Bearer $token", conversacionId, request)

            // Verificamos si la respuesta es exitosa
            respuesta != null
        } catch (e: Exception) {
            Log.e("IARepository", "Error al actualizar título: ${e.message}")
            false
        }
    }

    suspend fun eliminarConversacion(token: String, conversacionId: String): Boolean {
        return try {
            // Hacemos la llamada a la API para eliminar la conversación
            val respuesta = iaService.eliminarConversacion("Bearer $token", conversacionId)

            // Verificamos si la operación fue exitosa
            respuesta != null
        } catch (e: Exception) {
            Log.e("IARepository", "Error al eliminar conversación: ${e.message}")
            false
        }
    }
}