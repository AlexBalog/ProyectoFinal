package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Mensaje(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("conversacion")
    val conversacion: String,
    @SerializedName("contenido")
    val contenido: String,
    @SerializedName("esDeUsuario")
    val esDeUsuario: Boolean,
    @SerializedName("timestamp")
    val timestamp: Date
) : Serializable

data class NuevoMensaje(
    val contenido: String
)