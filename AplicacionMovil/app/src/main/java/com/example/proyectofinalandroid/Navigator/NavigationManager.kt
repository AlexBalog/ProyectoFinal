package com.example.proyectofinalandroid.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalandroid.View.LoginScreen

@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        // Pantalla de login
        composable("main") { LoginScreen(navController) }
    }
}
