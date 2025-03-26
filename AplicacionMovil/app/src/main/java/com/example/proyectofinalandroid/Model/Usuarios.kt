package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Usuarios(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasena")
    val contrasena: String,
    @SerializedName("fecha_nac")
    val fecha_nac: Date,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("foto")
    val foto: String,
    @SerializedName("sexo")
    val sexo: String,
    @SerializedName("IMC")
    val IMC: Float,
    @SerializedName("altura")
    val altura: Float,
    @SerializedName("peso")
    val peso: Float,
    @SerializedName("objetivo_peso")
    val objetivo_peso: Float,
    @SerializedName("objetivo_tiem")
    val objetivo_tiem: Float,
    @SerializedName("objetivo_cal")
    val objetivo_cal: Float,
    @SerializedName("ent_fav")
    val ent_fav: Array<String>,
    @SerializedName("plan")
    val plan: String,
    @SerializedName("formulario")
    val formulario: Boolean,
    var token: String? = null
) : Serializable

data class LoginResponse(
    @SerializedName("token")
    val token: String
) : Serializable