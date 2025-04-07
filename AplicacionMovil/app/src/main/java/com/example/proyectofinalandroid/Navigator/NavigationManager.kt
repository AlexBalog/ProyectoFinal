package com.example.proyectofinalandroid.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalandroid.View.FormularioScreen
import com.example.proyectofinalandroid.View.HomeScreen
import com.example.proyectofinalandroid.View.LoginScreen
import com.example.proyectofinalandroid.View.RegisterScreen

@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth", route = "root") {

        // Este es el grafo de autenticación (login/registro)
        navigation(startDestination = "login", route = "auth") {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
        }

        // Este es el grafo principal después de login
        navigation(startDestination = "formulario", route = "main") {
            composable("formulario") { FormularioScreen(navController) }
            composable("principal") { HomeScreen(navController) }
        }
    }
}

