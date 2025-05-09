package com.example.proyectofinalandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.proyectofinalandroid.worker.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "MyApplication"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Iniciando aplicación")
        crearCanalNotificaciones()

        // Programar el worker principal que gestionará las notificaciones
        programarWorkers()
    }

    private fun programarWorkers() {
        try {
            // Programar los workers al iniciar la aplicación
            WorkManagerScheduler.schedule(applicationContext)
            Log.d(TAG, "Workers programados correctamente al iniciar")
        } catch (e: Exception) {
            Log.e(TAG, "Error al programar workers: ${e.message}")
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "entrenamiento_channel"
            val channelName = "Recordatorios de entrenamiento"
            val description = "Notificaciones que te recuerdan entrenar y eventos programados"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
                enableVibration(true)
                enableLights(true)
            }

            // Obtener el NotificationManager con tipo específico
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado correctamente")
        }
    }
}