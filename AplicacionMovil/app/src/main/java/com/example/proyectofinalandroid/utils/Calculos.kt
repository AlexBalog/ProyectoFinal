package com.example.proyectofinalandroid.utils

import android.content.Context
import android.widget.Toast
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import kotlin.math.pow

/**
 * Calcula la edad a partir de una fecha de nacimiento
 */
fun calcularEdad(fechaNacimiento: Date): Int {
    val fechaLocalDate = fechaNacimiento.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val hoy = LocalDate.now()
    return Period.between(fechaLocalDate, hoy).years
}

/**
 * Calcula y actualiza los objetivos del usuario basados en sus datos físicos
 * Esta función debe ser llamada desde una corrutina
 */
suspend fun calcularObjetivos(
    usuario: Usuarios,
    viewModel: UsuariosViewModel,
    context: Context
) {
    try {
        // Extraer datos del usuario (no usar remember ya que no estamos en un composable)
        val fechaNacimiento = usuario.fechaNacimiento
        val sexo = usuario.sexo
        val altura = usuario.altura
        val peso = usuario.peso
        val objetivoPeso = usuario.objetivoPeso
        val objetivoTiempo = usuario.objetivoTiempo
        val nivelActividad = usuario.nivelActividad

        // Verificar que tenemos todos los datos necesarios
        if (fechaNacimiento == null || sexo.isNullOrEmpty() ||
            altura <= 0f || peso <= 0f || objetivoPeso <= 0f ||
            objetivoTiempo <= 0f || nivelActividad.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Faltan datos para calcular objetivos", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Calcular la edad
        val edad = calcularEdad(fechaNacimiento)

        // Calcular TMB (Tasa Metabólica Basal)
        val TMB = when (sexo) {
            "Hombre" -> 10f * peso + 6.25f * altura - 5f * edad + 5f
            "Masculino" -> 10f * peso + 6.25f * altura - 5f * edad + 5f
            "Mujer" -> 10f * peso + 6.25f * altura - 5f * edad - 161f
            "Femenino" -> 10f * peso + 6.25f * altura - 5f * edad - 161f
            else -> 10f * peso + 6.25f * altura - 5f * edad - 78f // Valor neutro
        }

        // Calcular calorías de mantenimiento según nivel de actividad
        val caloriasMantenimiento = when (nivelActividad) {
            "Sedentario" -> TMB * 1.2f
            "Ligero" -> TMB * 1.375f
            "Moderado" -> TMB * 1.55f
            "Activo" -> TMB * 1.725f
            "Muy activo" -> TMB * 1.9f
            else -> TMB * 1.4f // Valor predeterminado moderado
        }

        // Calcular ajuste calórico para alcanzar el objetivo (+ para subir, - para bajar)
        // 7700 calorías = 1kg de grasa aproximadamente
        val ajusteCaloricoSemanal = (7700f * (objetivoPeso - peso)) / (objetivoTiempo * 7f)

        // Calcular objetivo de calorías diarias
        val objetivoCalorias = caloriasMantenimiento + ajusteCaloricoSemanal

        // Calcular IMC
        val imc = peso / ((altura / 100f).pow(2))

        // Crear mapa de datos para actualizar
        val datos = mapOf(
            "caloriasMantenimiento" to caloriasMantenimiento.toString(),
            "objetivoCalorias" to objetivoCalorias.toString(),
            "IMC" to imc.toString()
        )

        // Actualizar datos en el ViewModel
        val exito = viewModel.update(datos)

        // Mostrar toast con el resultado
        withContext(Dispatchers.Main) {
            if (exito) {
                Toast.makeText(context, "Objetivos calculados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al guardar los objetivos", Toast.LENGTH_SHORT).show()
            }
        }

    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Extensión para convertir Date a LocalDate
 */
fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

/**
 * Extensión para convertir String a Date
 */
fun String.toDate(pattern: String = "yyyy-MM-dd"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}