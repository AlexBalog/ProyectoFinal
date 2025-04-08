package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Entrenar(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("cod_usu")
    val cos_usu: String,
    @SerializedName("cod_ent")
    val cod_ent: String,
    @SerializedName("duracion")
    val duracion: Number,
    @SerializedName("fecha")
    val fecha: Date
) : Serializable
