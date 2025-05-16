package com.example.proyectofinalandroid.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    private const val NOTIFICATION_PERMISSION_CODE = 101
    private const val CAMERA_PERMISSION_CODE = 102
    private const val READ_MEDIA_IMAGES_PERMISSION_CODE = 103

    /**
     * Verifica si se tiene permiso para enviar notificaciones
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores a Android 13, no se necesita un permiso específico
        }
    }

    /**
     * Solicita permiso para enviar notificaciones (para Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
                Log.d("Permisos", "Solicitando permiso de notificaciones")
            } else {
                Log.d("Permisos", "Ya tiene permiso de notificaciones")
            }
        }
    }

    /**
     * Procesa el resultado de la solicitud de permisos
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita permiso para usar la cámara
     */
    fun requestCameraPermission(activity: Activity) {
        if (!hasCameraPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
            Log.d("Permisos", "Solicitando permiso de cámara")
        } else {
            Log.d("Permisos", "Ya tiene permiso de cámara")
        }
    }

    /**
     * Verifica si se tiene permiso para acceder a las imágenes
     */
    fun hasReadMediaImagesPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita permiso para acceder a las imágenes
     */
    fun requestReadMediaImagesPermission(activity: Activity) {
        if (!hasReadMediaImagesPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                READ_MEDIA_IMAGES_PERMISSION_CODE
            )
            Log.d("Permisos", "Solicitando permiso de acceso a imágenes")
        } else {
            Log.d("Permisos", "Ya tiene permiso de acceso a imágenes")
        }
    }

    /**
     * Procesa el resultado de la solicitud de permisos
     */
    fun onRequestPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onNotificationGranted: () -> Unit = {},
        onNotificationDenied: () -> Unit = {},
        onCameraGranted: () -> Unit = {},
        onCameraDenied: () -> Unit = {},
        onReadMediaImagesGranted: () -> Unit = {},
        onReadMediaImagesDenied: () -> Unit = {}
    ) {
        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permisos", "Permiso de notificaciones concedido")
                    onNotificationGranted()
                } else {
                    Log.d("Permisos", "Permiso de notificaciones denegado")
                    onNotificationDenied()
                }
            }
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permisos", "Permiso de cámara concedido")
                    onCameraGranted()
                } else {
                    Log.d("Permisos", "Permiso de cámara denegado")
                    onCameraDenied()
                }
            }
            READ_MEDIA_IMAGES_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permisos", "Permiso de acceso a imágenes concedido")
                    onReadMediaImagesGranted()
                } else {
                    Log.d("Permisos", "Permiso de acceso a imágenes denegado")
                    onReadMediaImagesDenied()
                }
            }
        }
    }
}