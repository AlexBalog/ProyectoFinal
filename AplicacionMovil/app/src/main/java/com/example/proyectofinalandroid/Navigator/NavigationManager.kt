package com.example.proyectofinalandroid.Navigator

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectofinalandroid.View.BuscadorScreen
import com.example.proyectofinalandroid.utils.UserPreferences
import com.example.proyectofinalandroid.View.SplashScreen
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.View.FitMindScreen
import com.example.proyectofinalandroid.View.ChatScreen
import com.example.proyectofinalandroid.View.CreateTrainingScreen
import com.example.proyectofinalandroid.View.DetallesRealizarEntrenamientoScreen
import com.example.proyectofinalandroid.View.HistorialEntrenamientosRealizadosScreen
import com.example.proyectofinalandroid.View.MisEntrenamientosScreen
import com.example.proyectofinalandroid.View.ProfileScreen
import com.example.proyectofinalandroid.View.SettingsScreen
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import androidx.compose.runtime.produceState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.proyectofinalandroid.View.ForgotPasswordScreen
import com.example.proyectofinalandroid.View.HistorialMedicionesScreen
import com.example.proyectofinalandroid.View.EntrenamientosGuardadosScreen


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
            composable("olvidarPass") { ForgotPasswordScreen(navController) }
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
                "buscador?tipoBusqueda={tipoBusqueda}&categoria={categoria}&musculo={musculo}&orden={orden}&ordenAscDesc={ordenAscDesc}",
                arguments = listOf(
                    navArgument("tipoBusqueda") { nullable = true; defaultValue = null },
                    navArgument("categoria") { nullable = true; defaultValue = null },
                    navArgument("musculo") { nullable = true; defaultValue = null },
                    navArgument("orden") { nullable = true; defaultValue = null },
                    navArgument("ordenAscDesc") { nullable = true; defaultValue = "asc" }
                )
            ) { backStackEntry ->
                val tipoBusqueda = backStackEntry.arguments?.getString("tipoBusqueda")
                val categoria = backStackEntry.arguments?.getString("categoria")
                val musculo = backStackEntry.arguments?.getString("musculo")
                val orden = backStackEntry.arguments?.getString("orden")
                val ordenAscDesc = backStackEntry.arguments?.getString("ordenAscDesc")

                BuscadorScreen(
                    navController = navController,
                    tipoBusqueda = tipoBusqueda,
                    categoria = categoria,
                    musculo = musculo,
                    orden = orden,
                    ordenAscDesc = ordenAscDesc
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
            composable("detalleEntrenamientoRealizado/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                DetallesRealizarEntrenamientoScreen(navController = navController, entrenamientoRealizadoId = id)
            }

            composable(
                route = "crearEntrenamiento?id={id}&publicar={publicar}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("publicar") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val publicar = backStackEntry.arguments?.getBoolean("publicar") ?: false
                val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel()

                // Modificar produceState para manejar internamente los errores
                val entrenamientoState = produceState<EntrenamientoLoadState>(
                    initialValue = EntrenamientoLoadState.Loading
                ) {
                    if (id.isNullOrEmpty()) {
                        // Si no hay ID, no necesitamos cargar nada
                        value = EntrenamientoLoadState.Success(null)
                    } else {
                        try {
                            // Cargamos el entrenamiento
                            val entrenamiento = entrenamientosViewModel.getEntrenamientoById(id)
                            // Actualizamos el estado con el entrenamiento cargado
                            value = EntrenamientoLoadState.Success(entrenamiento)
                        } catch (e: Exception) {
                            Log.e("EntrenamientoNav", "Error al cargar entrenamiento: ${e.message}")
                            value = EntrenamientoLoadState.Error(e.message ?: "Error desconocido")
                        }
                    }
                }

                // Ahora usamos un when para determinar qué mostrar según el estado
                when (val state = entrenamientoState.value) {
                    is EntrenamientoLoadState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFAB47BC))
                        }
                    }
                    is EntrenamientoLoadState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                color = Color.Red
                            )
                        }
                    }
                    is EntrenamientoLoadState.Success -> {
                        CreateTrainingScreen(
                            navController = navController,
                            publicar = publicar,
                            entrenamiento = state.entrenamiento
                        )
                    }
                }
            }

            composable("misEntrenamientos") {
                MisEntrenamientosScreen(navController = navController)
            }

            composable("historialEntrenamientosRealizados") {
                HistorialEntrenamientosRealizadosScreen(navController = navController)
            }
            composable("verMediciones") {
                HistorialMedicionesScreen(navController = navController)
            }

            composable("verGuardados") {
                EntrenamientosGuardadosScreen(navController)
            }
        }
    }
}

sealed class EntrenamientoLoadState {
    object Loading : EntrenamientoLoadState()
    data class Error(val message: String) : EntrenamientoLoadState()
    data class Success(val entrenamiento: Entrenamientos?) : EntrenamientoLoadState()
}

