package com.example.proyectofinalandroid.worker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyectofinalandroid.Repository.EntrenamientoRealizadoRepository
import com.example.proyectofinalandroid.Repository.EventosUsuarioRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.utils.UserPreferences


@HiltWorker
class EntrenamientoReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val entrenamientoRepo: EntrenamientoRealizadoRepository,
    private val eventosRepo: EventosUsuarioRepository
) : CoroutineWorker(context, workerParams) {

    @SuppressLint("MissingPermission") // Puede fallar ------------------------------------------- Puede fallar
    override suspend fun doWork(): Result {
        val prefs = UserPreferences(applicationContext)
        val usuarioId = prefs.getUserId() ?: return Result.failure()

        val ultimoEntrenamiento = entrenamientoRepo.getUltimoEntrenamiento(usuarioId) // tu lógica aquí
        val eventos = eventosRepo.getEventosProximos(usuarioId)

        val hoy = LocalDate.now()

        // Si hace más de 7 días sin entrenar
        if (ultimoEntrenamiento != null &&
            ChronoUnit.DAYS.between(ultimoEntrenamiento.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), hoy) >= 7) {
            enviarNotificacion("¡Es hora de volver a entrenar!", "Hace una semana que no entrenas 💪")
        }

        // Si hay un evento mañana
        eventos.forEach {
            val fechaEvento = it.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            if (fechaEvento == hoy.plusDays(1)) {
                enviarNotificacion("Tienes un entrenamiento mañana", "Prepárate para tu sesión 💥")
            }
        }

        return Result.success()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun enviarNotificacion(titulo: String, contenido: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No tienes permiso para notificar, salir sin hacer nada
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, "entrenamiento_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(applicationContext)
            .notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    }

}
