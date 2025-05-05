package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Eventos(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("tipo")
    val tipo: String
) : Serializable
