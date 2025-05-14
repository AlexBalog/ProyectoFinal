package com.example.proyectofinalandroid.Navigator

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.proyectofinalandroid.View.ComenzarEntrenamientoScreen
import com.example.proyectofinalandroid.View.DetalleEntrenamientoScreen
import com.example.proyectofinalandroid.View.FormularioScreen
import com.example.proyectofinalandroid.View.HomeScreen
import com.example.proyectofinalandroid.View.LoginScreen
import com.example.proyectofinalandroid.View.RegisterScreen
import com.example.proyectofinalandroid.View.DetalleEjercicioScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.proyectofinalandroid.View.BuscadorScreen
import com.example.proyectofinalandroid.utils.UserPreferences
import com.example.proyectofinalandroid.View.SplashScreen
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.proyectofinalandroid.View.FitMindScreen
import com.example.proyectofinalandroid.View.ChatScreen
import com.example.proyectofinalandroid.View.ProfileScreen
import com.example.proyectofinalandroid.View.SettingsScreen


@Composable
fun Navegador() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash", route = "root") {
        composable("splash") {
            val context = LocalContext.current
            val userPrefs = remember { UserPreferences(context) }
            SplashScreen(navController, userPrefs)
        }
        // Este es el grafo de autenticación (login/registro)
        navigation(startDestination = "login", route = "auth") {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
        }

        // Este es el grafo principal después de login
        navigation(startDestination = "principal", route = "main") {
            composable("formulario") { FormularioScreen(navController) }
            composable("principal") { HomeScreen(navController) }
            composable("detalleEntrenamiento/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                DetalleEntrenamientoScreen(navController, id.toString())
            }
            composable("realizarEntrenamiento/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                ComenzarEntrenamientoScreen(navController, id.toString())
            }
            composable("detalleEjercicio/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                DetalleEjercicioScreen(navController, id.toString())
            }
            composable(
                "buscador?tipoBusqueda={tipoBusqueda}&categoria={categoria}&musculo={musculo}",
                arguments = listOf(
                    navArgument("tipoBusqueda") { nullable = true; defaultValue = null },
                    navArgument("categoria") { nullable = true; defaultValue = null },
                    navArgument("musculo") { nullable = true; defaultValue = null }
                )
            ) { backStackEntry ->
                val tipoBusqueda = backStackEntry.arguments?.getString("tipoBusqueda")
                val categoria = backStackEntry.arguments?.getString("categoria")
                val musculo = backStackEntry.arguments?.getString("musculo")

                BuscadorScreen(
                    navController = navController,
                    tipoBusqueda = tipoBusqueda,
                    categoria = categoria,
                    musculo = musculo
                )
            }
            composable("fitmind") {
                FitMindScreen(navController = navController)
            }

            composable("chat/{conversacionId}") { backStackEntry ->
                val conversacionId = backStackEntry.arguments?.getString("conversacionId") ?: ""
                ChatScreen(
                    navController = navController,
                    conversacionId = conversacionId
                )
            }

            composable("perfil") {
                ProfileScreen(navController = navController)
            }

            composable("configuracion") {
                SettingsScreen(navController = navController)
            }
        }
    }
}

