package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class EntrenamientoRealizado(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("usuario")
    val usuario: String,
    @SerializedName("entrenamiento")
    val entrenamiento: String,
    @SerializedName("duracion")
    val duracion: String,
    @SerializedName("fecha")
    val fecha: Date,
    @SerializedName("ejerciciosRealizados")
    val ejerciciosRealizados: List<String> = emptyList()
) : Serializable
