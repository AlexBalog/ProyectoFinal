package com.example.proyectofinalandroid.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

data class Usuarios(
    @SerializedName("_id")
    val _id: String = "",
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasena")
    val contrasena: String,
    @SerializedName("fecha_nac")
    val fecha_nac: Date = Date(),
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellido")
    val apellido: String,
    @SerializedName("foto")
    val foto: String = "",
    @SerializedName("sexo")
    val sexo: String = "",
    @SerializedName("IMC")
    val IMC: Float = 0f,
    @SerializedName("altura")
    val altura: Float = 0f,
    @SerializedName("peso")
    val peso: Float = 0f,
    @SerializedName("objetivo_peso")
    val objetivo_peso: Float = 0f,
    @SerializedName("objetivo_tiem")
    val objetivo_tiem: Float = 0f,
    @SerializedName("objetivo_cal")
    val objetivo_cal: Float = 0f,
    @SerializedName("ent_fav")
    val ent_fav: Array<String> = arrayOf(),
    @SerializedName("plan")
    val plan: String = "",
    @SerializedName("formulario")
    val formulario: Boolean = false,
    var token: String? = null
) : Serializable {
    // Constructor secundario que solo requiere nombre, apellido, email y contraseña
    constructor(nombre: String, apellido: String, email: String, contrasena: String) : this(
        _id = "",
        email = email,
        contrasena = contrasena,
        fecha_nac = Date(),
        nombre = nombre,
        apellido = apellido,
        foto = "",
        sexo = "",
        IMC = 0f,
        altura = 0f,
        peso = 0f,
        objetivo_peso = 0f,
        objetivo_tiem = 0f,
        objetivo_cal = 0f,
        ent_fav = arrayOf(),
        plan = "",
        formulario = false
    )
}

data class LoginResponse(
    @SerializedName("token")
    val token: String
) : Serializable