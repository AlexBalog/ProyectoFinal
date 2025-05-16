package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

/**
 * Enum que define los tipos de mediciones permitidos
 */
enum class TipoMedicion {
    PESO, CALORIAS, EJERCICIO_TIEMPO, EJERCICIO_CALORIAS, IMC
}

/**
 * Clase de datos que representa una medición del usuario
 */
data class Mediciones(
    @SerializedName("_id")
    val _id: String = "",

    @SerializedName("usuario")
    val usuario: String = "",

    @SerializedName("fecha")
    val fecha: Date = Date(),

    @SerializedName("tipo")
    val tipo: String = "",

    @SerializedName("unidad")
    val unidad: String = "",

    @SerializedName("valor")
    val valor: Float = 0f,

    @SerializedName("notas")
    val notas: String = ""
) : Serializable {

    fun obtenerUnidad(): String {
        if (unidad.isNotEmpty()) return unidad

        return when (tipo) {
            TipoMedicion.PESO.name -> "kg"
            TipoMedicion.CALORIAS.name -> "kcal"
            TipoMedicion.EJERCICIO_TIEMPO.name -> "min"
            TipoMedicion.EJERCICIO_CALORIAS.name -> "kcal"
            TipoMedicion.IMC.name -> ""
            else -> ""
        }
    }
}