package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class EventosUsuario(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("evento")
    val evento: String,
    @SerializedName("usuario")
    val usuario: String,
    @SerializedName("fecha")
    val fecha: Date,
    @SerializedName("hora")
    val hora: String,
    @SerializedName("notas")
    val notas: String
) : Serializable
