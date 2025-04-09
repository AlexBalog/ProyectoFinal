package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class EntrenamientoRealizado(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("usuario")
    val cos_usu: String,
    @SerializedName("entrenamiento")
    val cod_ent: String,
    @SerializedName("duracion")
    val duracion: Number,
    @SerializedName("fecha")
    val fecha: Date,
    @SerializedName("ejerciciosRealizados")
    val ejerciciosRealizados: List<String>
) : Serializable
