package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EjercicioRealizado(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("entrenamientoRealizado")
    var entrenamientoRealizado: String = "",
    @SerializedName("entrenamiento")
    val entrenamiento: String = "",
    @SerializedName("ejercicio")
    val ejercicio: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("series")
    val series: List<String>
    ) : Serializable
