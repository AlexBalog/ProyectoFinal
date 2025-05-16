package com.example.proyectofinalandroid.Remote

import com.example.proyectofinalandroid.Model.Mediciones
import retrofit2.Response
import retrofit2.http.*

interface MedicionesApi {

    @GET("mediciones/usuario/{usuarioId}")
    suspend fun obtenerMedicionesPorUsuario(
        @Path("usuarioId") usuarioId: String,
        @Header("Authorization") token: String,
        @Query("tipo") tipo: String? = null,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MedicionesResponse>

    @GET("mediciones/{id}")
    suspend fun obtenerMedicionPorId(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Mediciones>

    @POST("mediciones")
    suspend fun crearMedicion(
        @Body medicion: Mediciones,
        @Header("Authorization") token: String
    ): Response<Mediciones>

    @PUT("mediciones/{id}")
    suspend fun actualizarMedicion(
        @Path("id") id: String,
        @Body medicion: Mediciones,
        @Header("Authorization") token: String
    ): Response<Mediciones>

    @DELETE("mediciones/{id}")
    suspend fun eliminarMedicion(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<MensajeResponse>

    @GET("mediciones/estadisticas/{usuarioId}")
    suspend fun obtenerEstadisticasPorUsuario(
        @Path("usuarioId") usuarioId: String,
        @Header("Authorization") token: String,
        @Query("tipo") tipo: String = "PESO"
    ): Response<EstadisticasResponse>
}

// Clases de respuesta
data class MedicionesResponse(
    val total: Int,
    val totalPages: Int,
    val currentPage: Int,
    val mediciones: List<Mediciones>
)

data class MensajeResponse(
    val message: String
)

data class EstadisticasResponse(
    val tipo: String,
    val ultimo: Float?,
    val cambio: Float,
    val porcentajeCambio: Float,
    val promedio: Float,
    val maximo: Float,
    val minimo: Float,
    val totalMediciones: Int,
    val unidad: String
)