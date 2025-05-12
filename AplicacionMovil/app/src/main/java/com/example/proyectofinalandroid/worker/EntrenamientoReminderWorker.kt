package com.example.proyectofinalandroid.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectofinalandroid.Repository.EntrenamientoRealizadoRepository
import com.example.proyectofinalandroid.Repository.EventosUsuarioRepository
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.utils.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@HiltWorker
class EntrenamientoReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val entrenamientoRepo: EntrenamientoRealizadoRepository,
    private val eventosRepo: EventosUsuarioRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = UserPreferences(applicationContext)
                val usuarioId = prefs.getUserId() ?: return@withContext Result.failure()
                // Para pruebas, envía una notificación simple
                /*enviarNotificacion(
                    "Recordatorio de entrenamiento",
                    "¡Es hora de entrenar! 💪"
                )*/

                val hoy = LocalDate.now()
                try {
                    val ultimoEntrenamiento = entrenamientoRepo.getUltimoEntrenamiento(usuarioId)
                    val eventos = eventosRepo.getEventosProximos(usuarioId)
                    // 1 semana sin entrenar
                    if (ultimoEntrenamiento != null) {
                        val diasSinEntrenar = ChronoUnit.DAYS.between(
                            ultimoEntrenamiento.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                            hoy
                        )

                        if (diasSinEntrenar >= 7) {
                            enviarNotificacion(
                                "¡Es hora de entrenar!",
                                "Hace $diasSinEntrenar días que no entrenas 💪"
                            )
                        }
                    }

                    // Entrenamiento mañana
                    eventos.forEach {
                        val fechaEvento = it.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        if (fechaEvento == hoy.plusDays(1)) {
                            enviarNotificacion(
                                "¡Tienes un entrenamiento mañana!",
                                "Prepara tu ropa y motivación 🏋️‍♂️"
                            )
                        }
                    }

                    Result.success()
                } catch (e: Exception) {
                    Result.retry()
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun enviarNotificacion(titulo: String, contenido: String) {
        // Verificar permisos en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(applicationContext, "entrenamiento_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(
                (System.currentTimeMillis() % 10000).toInt(),
                notification
            )
        } catch (e: Exception) {
            Log.e("ERROR", "Error al enviar notificación: ${e.message}")
        }
    }
}