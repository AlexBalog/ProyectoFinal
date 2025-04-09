package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Entrenamientos(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("duracion")
    val duracion: Number,
    @SerializedName("foto")
    val foto: String,
    @SerializedName("musculos")
    val musculos: List<String>,
    @SerializedName("likes")
    val likes: Number,
    @SerializedName("ejercicios")
    val ejercicios: List<String>,
    @SerializedName("creador")
    val creador: String,
    @SerializedName("aprobado")
    val aprobado: Boolean,
    @SerializedName("pedido")
    val pedido: Boolean,
    @SerializedName("motivoRechazo")
    val motivoRechazo: String
) : Serializable
