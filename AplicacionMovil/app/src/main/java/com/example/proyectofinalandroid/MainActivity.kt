package com.example.proyectofinalandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.proyectofinalandroid.utils.PermissionHelper
import com.example.proyectofinalandroid.Navigator.Navegador

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionHelper.requestNotificationPermission(this)
        setContent {
            Navegador()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionResult(
            requestCode,
            permissions,
            grantResults,
            onGranted = { /* Permiso concedido */ },
            onDenied = { /* Permiso denegado */ }
        )
    }
}