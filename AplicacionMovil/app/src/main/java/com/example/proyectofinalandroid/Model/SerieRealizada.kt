package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SerieRealizada(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("ejercicioRealizado")
    var ejercicioRealizado: String = "",
    @SerializedName("ejercicio")
    val ejercicio: String,
    @SerializedName("numeroSerie")
    val numeroSerie: Number,
    @SerializedName("repeticiones")
    val repeticiones: Number,
    @SerializedName("peso")
    val peso: Number,
) : Serializable
