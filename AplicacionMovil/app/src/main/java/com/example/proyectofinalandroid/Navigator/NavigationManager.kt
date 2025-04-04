package com.example.proyectofinalandroid.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalandroid.View.FormularioScreen
import com.example.proyectofinalandroid.View.LoginScreen
import com.example.proyectofinalandroid.View.RegisterScreen

@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        // Pantalla de login
        composable("login") { LoginScreen(navController = navController) }
        // Pantalla de registro
        composable("register") { RegisterScreen(navController) }
        // Pantalla de formulario
        composable("formulario") { FormularioScreen(navController) }
    }
}
