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
    @SerializedName("fechaNacimiento")
    val fechaNacimiento: Date = Date(),
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
    @SerializedName("nivelActividad")
    val nivelActividad: String = "",
    @SerializedName("caloriasMantenimiento")
    val caloriasMantenimiento: Float = 0f,
    @SerializedName("altura")
    val altura: Float = 0f,
    @SerializedName("peso")
    val peso: Float = 0f,
    @SerializedName("objetivoPeso")
    val objetivoPeso: Float = 0f,
    @SerializedName("objetivoTiempo")
    val objetivoTiempo: Float = 0f,
    @SerializedName("objetivoCalorias")
    val objetivoCalorias: Float = 0f,
    @SerializedName("entrenamientosFavoritos")
    val entrenamientosFavoritos: List<String>,
    @SerializedName("plan")
    val plan: String = "",
    @SerializedName("formulario")
    val formulario: Boolean = false,
    @SerializedName("entrenamientosRealizados")
    val entrenamientosRealizados: List<String>,

    var token: String? = null
) : Serializable {
    // Constructor secundario que solo requiere nombre, apellido, email y contraseña
    constructor(nombre: String, apellido: String, email: String, contrasena: String) : this(
        _id = "",
        email = email,
        contrasena = contrasena,
        fechaNacimiento = Date(),
        nombre = nombre,
        apellido = apellido,
        foto = "",
        sexo = "",
        IMC = 0f,
        altura = 0f,
        peso = 0f,
        objetivoPeso = 0f,
        objetivoTiempo = 0f,
        objetivoCalorias = 0f,
        entrenamientosFavoritos = listOf(),
        plan = "",
        formulario = false,
        entrenamientosRealizados = listOf()
    )
}

data class LoginResponse(
    @SerializedName("token")
    val token: String
) : Serializable