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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.Model.Usuarios
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    usuariosViewModel: UsuariosViewModel = hiltViewModel()
) {
    // Estados
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
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
    fun isNombreValid(): Boolean {
        return nombre.isNotEmpty() && nombre.length >= 2
    }

    fun isApellidoValid(): Boolean {
        return apellido.isNotEmpty() && apellido.length >= 2
    }

    fun isEmailValid(): Boolean {
        return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPasswordValid(): Boolean {
        return password.length >= 6
    }

    fun validateAndRegister() {
        isError = false

        if (!isNombreValid()) {
            Toast.makeText(context, "Introduce un nombre válido", Toast.LENGTH_SHORT).show()
            isError = true
            return
        }

        if (!isApellidoValid()) {
            Toast.makeText(context, "Introduce un apellido válido", Toast.LENGTH_SHORT).show()
            isError = true
            return
        }

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

        // Crear el objeto Usuario con los datos del formulario
        val newUser = Usuarios(
            nombre = nombre,
            apellido = apellido,
            email = email,
            contrasena = password
        )

        usuariosViewModel.registrarUsuario(newUser)
    }

    // Efectos de lanzamiento para manejo de usuario
    LaunchedEffect(usuario) {
        if (usuario != null) {
            isLoading = false
            Toast.makeText(context, "¡Bienvenido ${usuario?.nombre}! Registro exitoso", Toast.LENGTH_LONG).show()
            navController.currentBackStackEntry?.savedStateHandle?.set("usuario", usuario)
            navController.navigate("vistaBuscador") {
                popUpTo("RegisterUser") { inclusive = true }
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
                    contentDescription = "Logo Register",
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
                                Color(0xFFAB47BC), // Morado más claro
                                Color(0xFF7B1FA2)  // Morado más oscuro
                            )
                        ),
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta para formulario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF7B1FA2),
                            spotColor = Color(0xFF7B1FA2)
                        ),
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
                            text = "Crear Cuenta",
                            fontSize = 24.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Nombre Field con icono
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it; isError = false },
                            label = { Text("Nombre", color = if (isError && !isNombreValid()) Color.Red else Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Nombre Icon",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
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
                            isError = isError && !isNombreValid()
                        )

                        // Apellido Field con icono
                        OutlinedTextField(
                            value = apellido,
                            onValueChange = { apellido = it; isError = false },
                            label = { Text("Apellido", color = if (isError && !isApellidoValid()) Color.Red else Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Apellido Icon",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
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
                            isError = isError && !isApellidoValid()
                        )

                        // Email Field con icono
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; isError = false },
                            label = { Text("Email", color = if (isError && !isEmailValid()) Color.Red else Color.Gray) },
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
                                .padding(bottom = 12.dp),
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
                            label = { Text("Contraseña", color = if (isError && !isPasswordValid()) Color.Red else Color.Gray) },
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
                                    validateAndRegister()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
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

                        // Botón de registro con indicador de progreso
                        Button(
                            onClick = { validateAndRegister() },
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
                                    "Registrarse",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opción para volver al login
                TextButton(
                    onClick = { navController.navigate("login") {
                        popUpTo("RegisterUser") { inclusive = true }
                    }},
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFAB47BC)
                    )
                ) {
                    Text(
                        "¿Ya tienes cuenta? Inicia sesión",
                        fontSize = 16.sp
                    )
                }

                // Texto de pie de página
                Spacer(modifier = Modifier.height(24.dp))
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