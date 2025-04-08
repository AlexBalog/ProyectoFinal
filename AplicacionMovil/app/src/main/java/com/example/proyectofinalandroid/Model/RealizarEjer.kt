package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RealizarEjer(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("cod_ent")
    val cod_ent: String,
    @SerializedName("cod_eje")
    val cod_eje: String,
    @SerializedName("repeticiones")
    val repeticiones: Number,
    @SerializedName("series")
    val series: Number,
    @SerializedName("peso")
    val peso: Float
) : Serializable
