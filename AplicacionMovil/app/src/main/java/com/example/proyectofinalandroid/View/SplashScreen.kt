package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.UserPreferences
import com.example.proyectofinalandroid.utils.UserState
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.proyectofinalandroid.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.work.PeriodicWorkRequestBuilder
import com.example.proyectofinalandroid.worker.EntrenamientoReminderWorker
import java.util.concurrent.TimeUnit
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun SplashScreen(navController: NavHostController, userPrefs: UserPreferences) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val context = LocalContext.current
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val userState by usuariosViewModel.userState.collectAsState()

    LaunchedEffect(userState) { // Reaccionar a cambios en el estado del usuario
        when (userState) {
            is UserState.Loading -> {
            }
            is UserState.Success -> {
                val user = (userState as UserState.Success).data

                val workRequest = PeriodicWorkRequestBuilder<EntrenamientoReminderWorker>(
                    1, TimeUnit.DAYS
                ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "EntrenamientoReminderWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

                when {
                    user == null -> navController.navigate("login")
                    !(user.formulario) -> navController.navigate("formulario")
                    else -> navController.navigate("principal")
                }
            }
            is UserState.Error -> {
                Log.e("FalloSplashScreen", "Error: ${(userState as UserState.Error).message}")
                navController.navigate("login")
            }
        }
    }

    LaunchedEffect(Unit) {
        val userId = userPrefs.getUserId()
        val userToken = userPrefs.getToken()

        if (userId != null || userToken != null) {
            usuariosViewModel.loadUser(userId.toString(), userToken.toString())
        } else {
            navController.navigate("login")
        }
    }

    // UI opcional
    Box(modifier = Modifier.fillMaxSize().background(color = Color.Black), contentAlignment = Alignment.Center) {
        when (userState) {
            is UserState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(128.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Cargando...",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFAB47BC)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Indicador circular
                        CircularProgressIndicator(
                            strokeWidth = 4.dp,
                            color = Color(0xFFAB47BC)
                        )
                    }
                }
            }
            is UserState.Error -> Text("Error cargando usuario")
            else -> {}
        }
    }
}
