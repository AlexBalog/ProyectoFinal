package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.window.Dialog


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // Obtener ViewModels
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)

    // Estados
    var isAnimatedIn by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val usuario by usuariosViewModel.usuario.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Diálogos
    var showChangePhotoDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showChangePersonalDataDialog by remember { mutableStateOf(false) }
    var showChangeFitnessDataDialog by remember { mutableStateOf(false) }
    var showChangeGoalsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Estados para los campos editables
    var newNombre by remember { mutableStateOf("") }
    var newApellido by remember { mutableStateOf("") }

    // Inicializar valores al cargar el usuario
    LaunchedEffect(usuario) {
        usuario?.let {
            newNombre = it.nombre
            newApellido = it.apellido
        }
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        isAnimatedIn = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )

            AnimatedVisibility(
                visible = isAnimatedIn,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Sección de CUENTA
                    SettingsSectionTitle(title = "CUENTA")

                    // Email (no editable, solo informativo)
                    EmailSettingItem(email = usuario?.email ?: "")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cambiar foto de perfil
                    SettingItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Cambiar foto de perfil",
                        onClick = { showChangePhotoDialog = true },
                        endContent = {
                            // Miniatura de la foto de perfil
                            if (usuario?.foto?.isNotEmpty() == true) {
                                Image(
                                    bitmap = base64ToImageBitmap(usuario!!.foto) as ImageBitmap,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2A2A2A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Perfil",
                                        tint = Color(0xFFAB47BC),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Cambiar nombre
                    SettingItem(
                        icon = Icons.Default.Edit,
                        title = "Cambiar nombre",
                        subtitle = "${usuario?.nombre ?: ""} ${usuario?.apellido ?: ""}",
                        onClick = { showChangeNameDialog = true }
                    )

                    // Notificaciones (envía al sistema)
                    SettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        subtitle = "Configurar notificaciones de la aplicación",
                        onClick = {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección de DATOS PERSONALES
                    SettingsSectionTitle(title = "DATOS PERSONALES")

                    // Datos personales (sexo, fecha nacimiento)
                    SettingItem(
                        icon = Icons.Default.PermIdentity,
                        title = "Información personal",
                        subtitle = formatPersonalInfo(usuario),
                        onClick = { showChangePersonalDataDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección de DATOS FITNESS
                    SettingsSectionTitle(title = "DATOS FITNESS")

                    // Datos físicos (altura, peso, IMC)
                    SettingItem(
                        icon = Icons.Default.Scale,
                        title = "Datos físicos",
                        subtitle = formatFitnessData(usuario),
                        onClick = { showChangeFitnessDataDialog = true }
                    )

                    // Nivel de actividad
                    SettingItem(
                        icon = Icons.Default.FitnessCenter,
                        title = "Nivel de actividad",
                        subtitle = usuario?.nivelActividad?.ifEmpty { "No definido" } ?: "No definido",
                        onClick = { /* Implementar cambio de nivel de actividad */ }
                    )

                    // Objetivos
                    SettingItem(
                        icon = Icons.Default.Flag,
                        title = "Objetivos",
                        subtitle = formatGoals(usuario),
                        onClick = { showChangeGoalsDialog = true }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sección de PREFERENCIAS
                    SettingsSectionTitle(title = "PREFERENCIAS")

                    // Tema
                    SettingItem(
                        icon = Icons.Default.DarkMode,
                        title = "Tema",
                        subtitle = "Oscuro",
                        onClick = { /* Implementar cambio de tema */ }
                    )

                    // Idioma
                    SettingItem(
                        icon = Icons.Default.Language,
                        title = "Idioma",
                        subtitle = "Español",
                        onClick = { /* Implementar cambio de idioma */ }
                    )

                    // Plan Premium
                    SettingItem(
                        icon = Icons.Default.Star,
                        title = "Plan Premium",
                        subtitle = if (usuario?.plan?.isNotEmpty() == true) "Activo" else "Desactivado",
                        onClick = { /* Navegar a pantalla de planes premium */ }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Botón de cerrar sesión
                    LogoutButton {
                        showLogoutConfirmDialog = true
                    }

                    // Espacio adicional al final
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // Diálogos
        if (showChangeNameDialog) {
            ChangeNameDialog(
                currentNombre = usuario?.nombre ?: "",
                currentApellido = usuario?.apellido ?: "",
                onDismiss = { showChangeNameDialog = false },
                onConfirm = { nombre, apellido ->
                    scope.launch {
                        // Implementar actualización de nombre en el ViewModel
                        usuariosViewModel.updateUsuario(
                            mapOf(
                                "nombre" to nombre,
                                "apellido" to apellido
                            )
                        )
                    }
                    showChangeNameDialog = false
                }
            )
        }

        if (showLogoutConfirmDialog) {
            LogoutConfirmDialog(
                onDismiss = { showLogoutConfirmDialog = false },
                onConfirm = {
                    // Implementar cierre de sesión y navegación a pantalla de login
                    usuariosViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("root") { inclusive = true }
                    }
                }
            )
        }

        // Aquí irían los demás diálogos para editar foto, datos personales, etc.
        // Los he omitido por brevedad, pero seguirían la misma estructura que ChangeNameDialog
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFAB47BC),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun EmailSettingItem(email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                Text(
                    text = email,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (endContent != null) {
                endContent()
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Ir",
                    tint = Color(0xFFAB47BC)
                )
            }
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFFE57373),
                spotColor = Color(0xFFE57373)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE57373),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar sesión",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Cerrar sesión",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNameDialog(
    currentNombre: String,
    currentApellido: String,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, apellido: String) -> Unit
) {
    var nombre by remember { mutableStateOf(currentNombre) }
    var apellido by remember { mutableStateOf(currentApellido) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color(0xFF7B1FA2),
                    spotColor = Color(0xFF7B1FA2)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cambiar nombre",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancelar", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(nombre, apellido) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "¿Estás seguro de que quieres cerrar la sesión?",
                color = Color.White
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sí, cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFAB47BC)
                )
            ) {
                Text("Cancelar")
            }
        },
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    )
}

// Funciones auxiliares para formatear información
fun formatPersonalInfo(usuario: Usuarios?): String {
    if (usuario == null) return "No disponible"

    val sexo = if (usuario.sexo.isNotEmpty()) usuario.sexo else "No definido"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaNacimiento = dateFormat.format(usuario.fechaNacimiento)

    return "$sexo · $fechaNacimiento"
}

fun formatFitnessData(usuario: Usuarios?): String {
    if (usuario == null) return "No disponible"

    val altura = if (usuario.altura > 0f) "${usuario.altura} cm" else "No definido"
    val peso = if (usuario.peso > 0f) "${usuario.peso} kg" else "No definido"
    val imc = if (usuario.IMC > 0f) String.format("%.1f", usuario.IMC) else "No calculado"

    return "$altura · $peso · IMC: $imc"
}

fun formatGoals(usuario: Usuarios?): String {
    if (usuario == null) return "No disponible"

    val objetivoPeso = if (usuario.objetivoPeso > 0f) "${usuario.objetivoPeso} kg" else "No definido"
    val objetivoTiempo = if (usuario.objetivoTiempo > 0f) "${usuario.objetivoTiempo} semanas" else "No definido"
    val objetivoCalorias = if (usuario.objetivoCalorias > 0f) "${usuario.objetivoCalorias} kcal" else "No definido"

    return "$objetivoPeso en $objetivoTiempo"
}

// Extension function para actualizar el usuario
// Deben implementarse en el ViewModel
fun UsuariosViewModel.updateUsuario(cambios: Map<String, Any>) {
    // Implementar la actualización en la API
    // Ejemplo: apiService.updateUsuario(usuario.value?._id ?: "", cambios)
}

fun UsuariosViewModel.logout() {
    // Implementar la lógica de cierre de sesión
    // Ejemplo:
    // 1. Limpiar token en SharedPreferences
    // 2. Limpiar estado del ViewModel
}