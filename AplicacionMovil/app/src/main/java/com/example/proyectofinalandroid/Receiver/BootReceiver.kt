package com.example.proyectofinalandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.proyectofinalandroid.worker.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receptor de broadcast que se activa cuando el dispositivo se inicia.
 * Se encarga de programar los recordatorios después de un reinicio.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.d(TAG, "Dispositivo reiniciado o app actualizada")

            // Usar CoroutineScope para operaciones que podrían llevar tiempo
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    // Programar los workers después del reinicio
                    WorkManagerScheduler.schedule(context)
                    Log.d(TAG, "Workers programados después del reinicio")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al programar workers después del reinicio: ${e.message}")
                }
            }
        }
    }
}