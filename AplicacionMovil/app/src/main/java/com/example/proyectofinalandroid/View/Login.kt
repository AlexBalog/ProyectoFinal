package com.example.proyectofinalandroid.View

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lint.kotlin.metadata.Visibility
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    usuariosViewModel: UsuariosViewModel = viewModel(navController.getBackStackEntry("main"))
) {
    // Estados
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var isAnimatedIn by remember { mutableStateOf(false) }

    // Recolección de estados
    val usuario by usuariosViewModel.usuario.collectAsState()
    val errorMessage by usuariosViewModel.errorMessage.collectAsState()
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

    // Funciones de validación
    fun isEmailValid(): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(): Boolean {
        return password.length >= 6
    }

    fun validateAndAuthenticate() {
        isError = false
        if (!isEmailValid()) {
            Toast.makeText(context, "Introduce un email válido", Toast.LENGTH_SHORT).show()
            isError = true
            return
        }

        if (!isPasswordValid()) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            isError = true
            return
        }

        isLoading = true
        keyboardController?.hide()
        usuariosViewModel.login(email, password)
    }

    // Efectos de lanzamiento para manejo de usuario
    LaunchedEffect(usuario) {
        if (usuario != null) {
            isLoading = false
            Toast.makeText(context, "Bienvenido ${usuario?.nombre}", Toast.LENGTH_LONG).show()
            navController.currentBackStackEntry?.savedStateHandle?.set("usuario", usuario)
            navController.navigate("vistaBuscador") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Efecto para manejo de errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            isLoading = false
            isError = true
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Contenido animado
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
                    contentDescription = "Logo Login",
                    modifier = Modifier
                        .height(150.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        }
                )

                Text(
                    text = "FitSphere",
                    style = TextStyle(
                        color = Color(0xFFAB47BC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Default,
                        shadow = Shadow(
                            color = Color(0xFF7B1FA2),
                            blurRadius = 12f,
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f)
                        ),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tarjeta para formulario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF7B1FA2),
                            spotColor = Color(0xFF7B1FA2)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontSize = 24.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Email Field con icono
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; isError = false },
                            label = { Text("Email", color = if (isError) Color.Red else Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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
                            isError = isError && !isEmailValid()
                        )

                        // Password field con icono y toggle de visibilidad
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; isError = false },
                            label = { Text("Contraseña", color = if (isError) Color.Red else Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password Icon",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color(0xFFAB47BC)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    validateAndAuthenticate()
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
                            isError = isError && !isPasswordValid()
                        )

                        // Botón de login con indicador de progreso
                        Button(
                            onClick = { validateAndAuthenticate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2),
                                disabledContainerColor = Color(0xFF7B1FA2).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    "Iniciar sesión",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Opciones de registro y recuperación de contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { navController.navigate("RegisterUser") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFAB47BC)
                        )
                    ) {
                        Text(
                            "Registrarse",
                            fontSize = 16.sp
                        )
                    }

                    TextButton(
                        onClick = { /* Lógica para recuperar contraseña */ },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFAB47BC)
                        )
                    ) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            fontSize = 16.sp
                        )
                    }
                }

                // Texto de pie de página
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