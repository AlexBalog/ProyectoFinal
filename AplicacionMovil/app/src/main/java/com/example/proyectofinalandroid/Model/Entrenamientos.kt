package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Entrenamientos(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("musculoPrincipal")
    var musculoPrincipal: String,
    @SerializedName("categoria")
    var categoria: String,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("duracion")
    var duracion: Number,
    @SerializedName("foto")
    var foto: String,
    @SerializedName("musculo")
    var musculo: List<String>,
    @SerializedName("likes")
    var likes: Number,
    @SerializedName("ejercicios")
    var ejercicios: List<String>,
    @SerializedName("creador")
    val creador: String,
    @SerializedName("aprobado")
    var aprobado: Boolean,
    @SerializedName("pedido")
    var pedido: Boolean,
    @SerializedName("motivoRechazo")
    var motivoRechazo: String,
    @SerializedName("baja")
    var baja: Boolean = false,
    @SerializedName("fechaBaja")
    var fechaBaja: Date? = null
) : Serializable
