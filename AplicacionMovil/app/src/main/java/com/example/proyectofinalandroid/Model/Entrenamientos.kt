package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Entrenamientos(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("musculos")
    val musculos: Array<String> = arrayOf(),
    @SerializedName("likes")
    val likes: String,
    @SerializedName("ejercicios")
    val ejercicios: Array<String> = arrayOf(),
    @SerializedName("creador")
    val creador: String
) : Serializable
