package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Conversacion(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("usuario")
    val usuario: String,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("createdAt")
    val createdAt: Date,
    @SerializedName("updatedAt")
    val updatedAt: Date
) : Serializable

data class NuevaConversacion(
    val usuario: String,
    val categoria: String,
    val titulo: String? = null
)