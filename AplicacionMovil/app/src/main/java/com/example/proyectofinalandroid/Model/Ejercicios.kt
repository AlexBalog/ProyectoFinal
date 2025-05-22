package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Ejercicios(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("musculo")
    val musculo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("consejos")
    val consejos: List<String>,
    @SerializedName("foto")
    val foto: String,
    @SerializedName("tutorial")
    val tutorial: String
) : Serializable
