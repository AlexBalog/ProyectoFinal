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
import android.util.Log

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
// MEJORA: Reemplaza la función calcularObjetivos en tu utils

/**
 * Calcula objetivos y combina con datos adicionales para hacer un solo update
 * Esta función ahora acepta datos adicionales para evitar múltiples updates
 */
suspend fun calcularObjetivos(
    usuario: Usuarios,
    viewModel: UsuariosViewModel,
    context: Context,
    datosAdicionales: Map<String, String> = emptyMap() // ✅ NUEVO: datos adicionales para combinar
) {
    try {
        // Extraer datos del usuario
        val fechaNacimiento = usuario.fechaNacimiento
        val sexo = usuario.sexo
        val altura = usuario.altura
        val peso = usuario.peso
        val objetivoPeso = usuario.objetivoPeso
        val objetivoTiempo = usuario.objetivoTiempo
        val nivelActividad = usuario.nivelActividad

        Log.d("calcularObjetivos", "=== INICIO CÁLCULO DE OBJETIVOS ===")
        Log.d("calcularObjetivos", "fechaNacimiento: $fechaNacimiento")
        Log.d("calcularObjetivos", "sexo: $sexo")
        Log.d("calcularObjetivos", "altura: $altura")
        Log.d("calcularObjetivos", "peso: $peso")
        Log.d("calcularObjetivos", "objetivoPeso: $objetivoPeso")
        Log.d("calcularObjetivos", "objetivoTiempo: $objetivoTiempo")
        Log.d("calcularObjetivos", "nivelActividad: $nivelActividad")
        Log.d("calcularObjetivos", "datosAdicionales: $datosAdicionales") // ✅ NUEVO

        // Verificar que tenemos todos los datos necesarios
        when {
            fechaNacimiento == null -> {
                Log.e("calcularObjetivos", "❌ fechaNacimiento es null")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta la fecha de nacimiento", Toast.LENGTH_SHORT).show()
                }
                return
            }
            sexo.isNullOrEmpty() -> {
                Log.e("calcularObjetivos", "❌ sexo está vacío")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta el sexo", Toast.LENGTH_SHORT).show()
                }
                return
            }
            altura <= 0f -> {
                Log.e("calcularObjetivos", "❌ altura inválida: $altura")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta la altura", Toast.LENGTH_SHORT).show()
                }
                return
            }
            peso <= 0f -> {
                Log.e("calcularObjetivos", "❌ peso inválido: $peso")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta el peso", Toast.LENGTH_SHORT).show()
                }
                return
            }
            objetivoPeso <= 0f -> {
                Log.e("calcularObjetivos", "❌ objetivoPeso inválido: $objetivoPeso")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta el peso objetivo", Toast.LENGTH_SHORT).show()
                }
                return
            }
            objetivoTiempo <= 0f -> {
                Log.e("calcularObjetivos", "❌ objetivoTiempo inválido: $objetivoTiempo")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta el tiempo objetivo", Toast.LENGTH_SHORT).show()
                }
                return
            }
            nivelActividad.isNullOrEmpty() -> {
                Log.e("calcularObjetivos", "❌ nivelActividad está vacío")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Falta el nivel de actividad", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

        // Calcular la edad
        val edad = calcularEdad(fechaNacimiento)
        Log.d("calcularObjetivos", "✅ Edad calculada: $edad años")

        // Calcular TMB (Tasa Metabólica Basal)
        val TMB = when (sexo) {
            "Hombre", "Masculino" -> 10f * peso + 6.25f * altura - 5f * edad + 5f
            "Mujer", "Femenino" -> 10f * peso + 6.25f * altura - 5f * edad - 161f
            else -> 10f * peso + 6.25f * altura - 5f * edad - 78f // Valor neutro
        }
        Log.d("calcularObjetivos", "✅ TMB calculada: $TMB")

        // Calcular calorías de mantenimiento según nivel de actividad
        val caloriasMantenimiento = when (nivelActividad) {
            "Sedentario" -> TMB * 1.2f
            "Ligero" -> TMB * 1.375f
            "Moderado" -> TMB * 1.55f
            "Activo" -> TMB * 1.725f
            "Muy activo" -> TMB * 1.9f
            else -> TMB * 1.4f // Valor predeterminado moderado
        }
        Log.d("calcularObjetivos", "✅ Calorías mantenimiento: $caloriasMantenimiento")

        // Calcular ajuste calórico para alcanzar el objetivo
        val ajusteCaloricoSemanal = (7700f * (objetivoPeso - peso)) / (objetivoTiempo * 7f)
        val objetivoCalorias = caloriasMantenimiento + ajusteCaloricoSemanal
        Log.d("calcularObjetivos", "✅ Objetivo calorías: $objetivoCalorias")

        // Calcular IMC
        val imc = peso / ((altura / 100f).pow(2))
        Log.d("calcularObjetivos", "✅ IMC calculado: $imc")

        // ✅ MEJORADO: Combinar datos calculados con datos adicionales
        val datosCalculados = mapOf(
            "caloriasMantenimiento" to caloriasMantenimiento.toString(),
            "objetivoCalorias" to objetivoCalorias.toString(),
            "IMC" to imc.toString()
        )

        // Combinar todos los datos en un solo update
        val datosCombinados = datosAdicionales + datosCalculados

        Log.d("calcularObjetivos", "📤 Enviando datos combinados al ViewModel: $datosCombinados")

        // ✅ MEJORADO: Un solo update con todos los datos
        val exito = viewModel.update(datosCombinados)

        Log.d("calcularObjetivos", "📥 Resultado del update combinado: $exito")

        // Mostrar toast con el resultado
        withContext(Dispatchers.Main) {
            if (exito) {
                Toast.makeText(context, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                Log.d("calcularObjetivos", "✅ ÉXITO: Todos los datos guardados en un solo update")
            } else {
                Toast.makeText(context, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                Log.e("calcularObjetivos", "❌ ERROR: No se pudieron guardar los datos")
            }
        }

    } catch (e: Exception) {
        Log.e("calcularObjetivos", "❌ EXCEPCIÓN: ${e.message}", e)
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