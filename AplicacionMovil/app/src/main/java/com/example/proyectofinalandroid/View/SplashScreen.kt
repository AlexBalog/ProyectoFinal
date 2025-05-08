package com.example.proyectofinalandroid.View

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.UserPreferences


@Composable
fun SplashScreen(navController: NavHostController, userPrefs: UserPreferences) {
    val context = LocalContext.current
    val usuariosViewModel: UsuariosViewModel = hiltViewModel()


    LaunchedEffect(Unit) {
        val userId = userPrefs.getUserId()
        val user = usuariosViewModel.getOne(userId.toString())

        when {
            user == null -> {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            user.formulario -> {
                navController.navigate("formulario") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            else -> {
                navController.navigate("principal") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // UI opcional
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
