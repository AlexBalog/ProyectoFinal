package com.example.proyectofinalandroid.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Worker para programar el recordatorio de entrenamiento periódicamente.
 * Este worker se encarga de registrar el EntrenamientoReminderWorker para
 * que se ejecute periódicamente.
 */
@HiltWorker
class WorkManagerScheduler @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "WorkManagerScheduler"
        private const val WORK_NAME_SCHEDULER = "entrenamiento_scheduler_worker"
        private const val WORK_NAME_REMINDER = "entrenamiento_reminder_worker"

        /**
         * Programa el WorkManagerScheduler para que se ejecute al inicio del dispositivo.
         * Este método debe llamarse en Application.onCreate() y después de un reinicio.
         */
        fun schedule(context: Context) {
            Log.d(TAG, "Programando WorkManagerScheduler...")

            // Configuración para que se ejecute una vez al día
            val schedulerRequest = PeriodicWorkRequestBuilder<WorkManagerScheduler>(
                1, TimeUnit.DAYS
            ).addTag(WORK_NAME_SCHEDULER)
                .setInitialDelay(10, TimeUnit.SECONDS) // Retraso inicial para debug
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            // Asegurarse de que siempre exista una instancia programada
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_SCHEDULER,
                ExistingPeriodicWorkPolicy.KEEP,
                schedulerRequest
            )

            // También programamos inmediatamente el reminder worker (ejecución única para pruebas)
            scheduleReminder(context)
        }

        /**
         * Programa el EntrenamientoReminderWorker para que se ejecute periódicamente.
         */
        private fun scheduleReminder(context: Context) {
            Log.d(TAG, "Programando EntrenamientoReminderWorker...")

            // Configuración para que se ejecute cada 15 minutos (para pruebas)
            // En producción, deberías usar un intervalo más apropiado como 12 horas
            val reminderRequest = PeriodicWorkRequestBuilder<EntrenamientoReminderWorker>(
                12, TimeUnit.HOURS
            ).addTag(WORK_NAME_REMINDER)
                .setInitialDelay(1, TimeUnit.MINUTES) // Para pruebas
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            Log.d(TAG, "EntrenamientoReminderWorker programado")

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_REMINDER,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )

            Log.d(TAG, "EntrenamientoReminderWorker iniciado")

        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "WorkManagerScheduler ejecutándose...")
        try {
            // Programar el worker de recordatorios
            scheduleReminder(applicationContext)
            Log.d(TAG, "WorkManagerScheduler ejecutado correctamente")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error al programar recordatorio: ${e.message}")
            return Result.retry()
        }
    }
}