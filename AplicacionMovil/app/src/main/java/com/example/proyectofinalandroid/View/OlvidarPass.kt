package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import kotlinx.coroutines.delay

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)

    // Estados locales
    var email by remember { mutableStateOf("") }
    var isAnimatedIn by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val errorMessage by usuariosViewModel.errorMessage.collectAsState()
    val isLoading by usuariosViewModel.isLoading.collectAsState()

    // Contexto y utilidades
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Configurar animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        isAnimatedIn = true
    }

    // Animación para el logo
    val logoScale by animateFloatAsState(
        targetValue = if (isAnimatedIn) 1f else 0.8f,
        animationSpec = tween(durationMillis = 500)
    )

    // Efectos para manejar errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            usuariosViewModel._errorMessage.value = null // Limpiar el mensaje después de mostrarlo
        }
    }

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Botón de regreso
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color(0xFFAB47BC)
            )
        }

        AnimatedVisibility(
            visible = isAnimatedIn,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .systemBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo con animación
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo FitSphere",
                    modifier = Modifier
                        .height(120.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        }
                )

                Text(
                    text = "FitSphere",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Default,
                        shadow = Shadow(
                            color = Color(0xFF7B1FA2),
                            blurRadius = 12f,
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f)
                        ),
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        ),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tarjeta para formulario de recuperación
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF7B1FA2),
                            spotColor = Color(0xFF7B1FA2)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!emailSent) {
                            // Vista de solicitud de recuperación
                            Text(
                                text = "Recuperar Contraseña",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Ingresa tu email y te enviaremos las instrucciones para restablecer tu contraseña.",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Email Field
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email Icon",
                                        tint = Color(0xFFAB47BC)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        sendPasswordReset(email, context, keyboardController, usuariosViewModel) {
                                            emailSent = true
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFFAB47BC),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFFAB47BC),
                                    selectionColors = TextSelectionColors(
                                        handleColor = Color(0xFFAB47BC),
                                        backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                    )
                                ),
                                textStyle = TextStyle(color = Color.White),
                                isError = email.isNotEmpty() && !isEmailValid(email)
                            )

                            // Botón de enviar
                            Button(
                                onClick = {
                                    sendPasswordReset(email, context, keyboardController, usuariosViewModel) {
                                        emailSent = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7B1FA2),
                                    disabledContainerColor = Color(0xFF7B1FA2).copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading && email.isNotEmpty()
                            ) {
                                if (isLoading == true) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        "Enviar instrucciones",
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // Vista de confirmación
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email enviado",
                                tint = Color(0xFFAB47BC),
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Email Enviado",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Hemos enviado las instrucciones de recuperación a:",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = email,
                                fontSize = 16.sp,
                                color = Color(0xFFAB47BC),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Text(
                                text = "Revisa tu bandeja de entrada y sigue las instrucciones del email.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Botón para volver al login
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7B1FA2)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Volver al Login",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón para reenviar email
                            TextButton(
                                onClick = {
                                    emailSent = false
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
                            ) {
                                Text("¿No recibiste el email? Intentar de nuevo", fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "© 2025 FitSphere",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f)
                )
            }
        }
    }
}

// Funciones auxiliares
private fun sendPasswordReset(
    email: String,
    context: Context,
    keyboardController: SoftwareKeyboardController?,
    usuariosViewModel: UsuariosViewModel,
    onSuccess: () -> Unit
) {
    if (!isEmailValid(email)) {
        Toast.makeText(context, "Introduce un email válido", Toast.LENGTH_SHORT).show()
        return
    }

    keyboardController?.hide()

    // Aquí deberías llamar al método correspondiente en tu ViewModel
    // Por ejemplo: usuariosViewModel.forgotPassword(email)
    // Por ahora simulo el envío exitoso

    Toast.makeText(context, "Email de recuperación enviado", Toast.LENGTH_LONG).show()
    onSuccess()
}

private fun isEmailValid(email: String): Boolean {
    return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}