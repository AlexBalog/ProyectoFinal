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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    var showCodeDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados del ViewModel
    val errorMessage by usuariosViewModel.errorMessage.collectAsState()
    val isLoading by usuariosViewModel.isLoading.collectAsState()
    val passwordResetSuccess by usuariosViewModel.passwordResetSuccess.collectAsState()

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

    // Efectos para manejar errores y estados
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            usuariosViewModel._errorMessage.value = null
        }
    }

    LaunchedEffect(passwordResetSuccess) {
        if (passwordResetSuccess == true) {
            Toast.makeText(context, "Contraseña cambiada exitosamente", Toast.LENGTH_LONG).show()
            usuariosViewModel._passwordResetSuccess.value = false
            navController.popBackStack()
        }
    }

    // Diálogo para verificar código
    if (showCodeDialog) {
        CodeVerificationDialog(
            onDismiss = { showCodeDialog = false },
            onCodeVerified = {
                showCodeDialog = false
                showPasswordDialog = true
            },
            usuariosViewModel = usuariosViewModel,
            email = email
        )
    }

    // Diálogo para nueva contraseña
    if (showPasswordDialog) {
        NewPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordChanged = {
                showPasswordDialog = false
                navController.popBackStack()
            },
            usuariosViewModel = usuariosViewModel,
            email = email
        )
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
                                text = "Ingresa tu email y te enviaremos un código de verificación para restablecer tu contraseña.",
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
                                        sendVerificationCode(email, context, usuariosViewModel) {
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
                                    sendVerificationCode(email, context, usuariosViewModel) {
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
                                        "Enviar código",
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
                                text = "Código Enviado",
                                fontSize = 24.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = "Hemos enviado un código de verificación a:",
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

                            // Botón para verificar código
                            Button(
                                onClick = { showCodeDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7B1FA2)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Verificar código",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón para reenviar código
                            TextButton(
                                onClick = {
                                    sendVerificationCode(email, context, usuariosViewModel) {}
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
                            ) {
                                Text("¿No recibiste el código? Reenviar", fontSize = 14.sp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeVerificationDialog(
    onDismiss: () -> Unit,
    onCodeVerified: () -> Unit,
    usuariosViewModel: UsuariosViewModel,
    email: String
) {
    var code by remember { mutableStateOf("") }
    val isLoading by usuariosViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Verificar Código",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Ingresa el código de 6 dígitos que enviamos a tu email",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it },
                    label = { Text("Código", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFAB47BC)
                    ),
                    textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFAB47BC))
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            verifyCode(email, code, context, usuariosViewModel, onCodeVerified)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        enabled = !isLoading && code.length == 6
                    ) {
                        if (isLoading == true) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Verificar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: () -> Unit,
    usuariosViewModel: UsuariosViewModel,
    email: String
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by usuariosViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nueva Contraseña",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Ingresa tu nueva contraseña",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Nueva contraseña
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color(0xFFAB47BC)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(
                                imageVector = if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    },
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFAB47BC)
                    ),
                    textStyle = TextStyle(color = Color.White),
                    isError = newPassword.isNotEmpty() && !isPasswordValid(newPassword)
                )

                // Confirmar contraseña
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = Color(0xFFAB47BC)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    },
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFAB47BC)
                    ),
                    textStyle = TextStyle(color = Color.White),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFAB47BC))
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            changePassword(email, newPassword, confirmPassword, context, usuariosViewModel, onPasswordChanged)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        enabled = !isLoading && isPasswordValid(newPassword) && newPassword == confirmPassword
                    ) {
                        if (isLoading == true) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Cambiar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Funciones auxiliares
private fun sendVerificationCode(
    email: String,
    context: Context,
    usuariosViewModel: UsuariosViewModel,
    onSuccess: () -> Unit
) {
    if (!isEmailValid(email)) {
        Toast.makeText(context, "Introduce un email válido", Toast.LENGTH_SHORT).show()
        return
    }

    usuariosViewModel.sendVerificationCode(email) { success ->
        if (success) {
            onSuccess()
        }
    }
}

private fun verifyCode(
    email: String,
    code: String,
    context: Context,
    usuariosViewModel: UsuariosViewModel,
    onSuccess: () -> Unit
) {
    if (code.length != 6) {
        Toast.makeText(context, "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
        return
    }

    usuariosViewModel.verifyCode(email, code) { success ->
        if (success) {
            onSuccess()
        }
    }
}

private fun changePassword(
    email: String,
    newPassword: String,
    confirmPassword: String,
    context: Context,
    usuariosViewModel: UsuariosViewModel,
    onSuccess: () -> Unit
) {
    if (!isPasswordValid(newPassword)) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        return
    }

    if (newPassword != confirmPassword) {
        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        return
    }

    usuariosViewModel.changePassword(email, newPassword) { success ->
        if (success) {
            onSuccess()
        }
    }
}

private fun isEmailValid(email: String): Boolean {
    return email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isPasswordValid(password: String): Boolean {
    return password.length >= 6
}