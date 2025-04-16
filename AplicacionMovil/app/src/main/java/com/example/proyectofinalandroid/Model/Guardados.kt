package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Guardados(
    @SerializedName("entrenamiento")
    val entrenamiento: String,
    @SerializedName("usuario")
    val usuario: String
) : Serializable
