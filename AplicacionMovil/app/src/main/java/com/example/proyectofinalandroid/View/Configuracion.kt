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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.proyectofinalandroid.utils.calcularObjetivos
import com.example.proyectofinalandroid.utils.getImageBitmapSafely
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.proyectofinalandroid.utils.uriToBitmap
import com.example.proyectofinalandroid.utils.resizeBitmap
import com.example.proyectofinalandroid.utils.bitmapToBase64
import com.example.proyectofinalandroid.utils.createTempImageUri
import android.widget.Toast
import com.example.proyectofinalandroid.utils.PermissionHelper
import android.Manifest





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

    val tempPhotoUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri.value != null) {
            scope.launch {
                try {
                    val bitmap = uriToBitmap(context, tempPhotoUri.value!!)
                    bitmap?.let {
                        val resizedBitmap = resizeBitmap(it)
                        val base64Image = bitmapToBase64(resizedBitmap)
                        usuariosViewModel.update(mapOf("foto" to base64Image))
                        Toast.makeText(context, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Añade esto junto a los otros launchers
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir cámara
            tempPhotoUri.value = createTempImageUri(context)
            cameraLauncher.launch(tempPhotoUri.value)
        } else {
            // Permiso denegado
            Toast.makeText(context, "Se necesita permiso de cámara para usar esta función", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val bitmap = uriToBitmap(context, it)
                    bitmap?.let { bmp ->
                        val resizedBitmap = resizeBitmap(bmp)
                        val base64Image = bitmapToBase64(resizedBitmap)
                        usuariosViewModel.update(mapOf("foto" to base64Image))
                        Toast.makeText(context, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val requestMediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir galería
            galleryLauncher.launch("image/*")
        } else {
            // Permiso denegado
            Toast.makeText(context, "Se necesita permiso para acceder a las imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    // Diálogos
    var showChangePhotoDialog by remember { mutableStateOf(false) }
    var showChangeNameDialog by remember { mutableStateOf(false) }
    var showChangePersonalDataDialog by remember { mutableStateOf(false) }
    var showChangeFitnessDataDialog by remember { mutableStateOf(false) }
    var showChangeGoalsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showActivityLevelDialog by remember { mutableStateOf(false) }

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
                        onClick = { showActivityLevelDialog = true }
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
                        usuariosViewModel.update(
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
                    usuariosViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("root") { inclusive = true }
                    }
                }
            )
        }

        if (showChangePhotoDialog) {
            ChangePhotoDialog(
                currentPhoto = usuario?.foto,
                onDismiss = { showChangePhotoDialog = false },
                onTakePhoto = {
                    // Verificar y solicitar permiso de cámara
                    if (PermissionHelper.hasCameraPermission(context)) {
                        tempPhotoUri.value = createTempImageUri(context)
                        cameraLauncher.launch(tempPhotoUri.value)
                    } else {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showChangePhotoDialog = false
                },
                onSelectFromGallery = {
                    // Verificar y solicitar permiso de acceso a imágenes
                    if (PermissionHelper.hasReadMediaImagesPermission(context)) {
                        galleryLauncher.launch("image/*")
                    } else {
                        requestMediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    showChangePhotoDialog = false
                }
            )
        }

        if (showChangePersonalDataDialog) {
            ChangePersonalDataDialog(
                sexo = usuario?.sexo,
                fechaNacimiento = usuario?.fechaNacimiento,
                onDismiss = { showChangePersonalDataDialog = false },
                onConfirm = { sexo, fechaNacimiento ->
                    scope.launch {
                        usuariosViewModel.update(
                            mapOf(
                                "sexo" to sexo.toString(),
                                "fechaNacimiento" to fechaNacimiento.toString()
                            )
                        )

                        delay(500)

                        usuariosViewModel.usuario.value?.let { usuarioActualizado ->
                            calcularObjetivos(
                                usuario = usuarioActualizado,
                                viewModel = usuariosViewModel,
                                context = context
                            )
                        }
                    }
                    showChangePersonalDataDialog = false
                }
            )
        }

        if (showChangeFitnessDataDialog) {
            ChangeFitnessDataDialog(
                altura = usuario?.altura ?: 0f,
                peso = usuario?.peso ?: 0f,
                onDismiss = { showChangeFitnessDataDialog = false },
                onConfirm = { altura, peso ->
                    scope.launch {
                        // Primero actualiza los valores
                        usuariosViewModel.update(
                            mapOf(
                                "altura" to altura.toString(),
                                "peso" to peso.toString(),
                            )
                        )

                        // Espera a que el usuario se actualice desde la base de datos
                        delay(500)

                        // Ahora calcula los objetivos con el usuario actualizado
                        usuariosViewModel.usuario.value?.let { usuarioActualizado ->
                            calcularObjetivos(
                                usuario = usuarioActualizado,
                                viewModel = usuariosViewModel,
                                context = context
                            )
                        }
                    }
                    showChangeFitnessDataDialog = false
                }
            )
        }

        if (showChangeGoalsDialog) {
            ChangeGoalsDialog(
                pesoActual = usuario?.peso ?: 0f,
                objetivoPeso = usuario?.objetivoPeso ?: 0f,
                objetivoTiempo = usuario?.objetivoTiempo ?: 0f,
                onDismiss = { showChangeGoalsDialog = false },
                onConfirm = { objetivoPeso, objetivoTiempo ->
                    scope.launch {
                        usuariosViewModel.update(
                            mapOf(
                                "objetivoPeso" to objetivoPeso.toString(),
                                "objetivoTiempo" to objetivoTiempo.toString(),
                            )
                        )

                        delay(500)

                        usuariosViewModel.usuario.value?.let { usuarioActualizado ->
                            calcularObjetivos(
                                usuario = usuarioActualizado,
                                viewModel = usuariosViewModel,
                                context = context
                            )
                        }
                    }
                    showChangeGoalsDialog = false
                }
            )
        }

        if (showActivityLevelDialog) {
            ActivityLevelDialog(
                currentLevel = usuario?.nivelActividad,
                onDismiss = { showActivityLevelDialog = false },
                onConfirm = { nivelActividad ->
                    scope.launch {
                        usuariosViewModel.update(
                            mapOf(
                                "nivelActividad" to nivelActividad
                            )
                        )

                        delay(500)

                        usuariosViewModel.usuario.value?.let { usuarioActualizado ->
                            calcularObjetivos(
                                usuario = usuarioActualizado,
                                viewModel = usuariosViewModel,
                                context = context
                            )
                        }
                    }
                    showActivityLevelDialog = false
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

    val sexo = if (usuario.sexo.isNullOrEmpty()) "No definido" else usuario.sexo
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaNacimiento = if(usuario.fechaNacimiento != null) dateFormat.format(usuario.fechaNacimiento) else "No definido"

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


@Composable
fun ChangePhotoDialog(
    currentPhoto: String?,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onSelectFromGallery: () -> Unit
) {
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
                // Título
                Text(
                    text = "Cambiar foto de perfil",
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

                // Mostrar foto actual si existe
                if (currentPhoto != null && currentPhoto.isNotEmpty()) {
                    val bitmap = getImageBitmapSafely(currentPhoto)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap as ImageBitmap,
                            contentDescription = "Foto actual",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A2A2A))
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    // Mostrar icono de usuario si no hay foto
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sin foto",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Opciones para cambiar foto
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(2.dp, Color(0xFFAB47BC)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFAB47BC)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Tomar foto",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tomar foto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSelectFromGallery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(2.dp, Color(0xFFAB47BC)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFAB47BC)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Galería",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Seleccionar de la galería",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = "Cancelar",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePersonalDataDialog(
    sexo: String?,
    fechaNacimiento: Date?,
    onDismiss: () -> Unit,
    onConfirm: (sexo: String, fechaNacimiento: Date) -> Unit
) {
    var selectedSexo by remember { mutableStateOf(sexo ?: "") }
    var selectedDate by remember { mutableStateOf(fechaNacimiento ?: Date()) }
    var expandedSexoMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sexoOptions = listOf("Masculino", "Femenino")
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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
                // Título
                Text(
                    text = "Información personal",
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

                // Selección de sexo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sexo",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF252525),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { expandedSexoMenu = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedSexo.isEmpty()) "Selecciona una opción" else selectedSexo,
                                fontSize = 16.sp,
                                color = if (selectedSexo.isEmpty()) Color.Gray else Color.White
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedSexoMenu,
                        onDismissRequest = { expandedSexoMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0xFF252525))
                    ) {
                        sexoOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(text = option, color = Color.White) },
                                onClick = {
                                    selectedSexo = option
                                    expandedSexoMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selección de fecha de nacimiento
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Fecha de nacimiento",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF252525),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(selectedDate),
                                fontSize = 16.sp,
                                color = Color.White
                            )

                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    }
                }

                // Nota: Aquí deberías implementar un DatePicker personalizado o usar una biblioteca.
                // El DatePickerDialog estándar de Material3 no está completamente implementado en
                // este ejemplo por brevedad, pero puedes usar bibliotecas como material-dialogs.

                Spacer(modifier = Modifier.height(32.dp))

                // Botones
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
                        onClick = { onConfirm(selectedSexo, selectedDate) },
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeFitnessDataDialog(
    altura: Float,
    peso: Float,
    onDismiss: () -> Unit,
    onConfirm: (altura: Float, peso: Float) -> Unit
) {
    var alturaText by remember { mutableStateOf(if (altura > 0f) altura.toString() else "") }
    var pesoText by remember { mutableStateOf(if (peso > 0f) peso.toString() else "") }


    val alturaError = remember(alturaText) {
        when {
            alturaText.isEmpty() -> "La altura es requerida"
            alturaText.toFloatOrNull() == null -> "Debe ser un número válido"
            alturaText.toFloat() < 50 || alturaText.toFloat() > 250 -> "Debe estar entre 50 y 250 cm"
            else -> ""
        }
    }

    val pesoError = remember(pesoText) {
        when {
            pesoText.isEmpty() -> "El peso es requerido"
            pesoText.toFloatOrNull() == null -> "Debe ser un número válido"
            pesoText.toFloat() < 20 || pesoText.toFloat() > 300 -> "Debe estar entre 20 y 300 kg"
            else -> ""
        }
    }

    val isFormValid = alturaError.isEmpty() && pesoError.isEmpty()

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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Datos físicos",
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

                // Altura
                OutlinedTextField(
                    value = alturaText,
                    onValueChange = { alturaText = it },
                    label = { Text("Altura (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = alturaError.isNotEmpty(),
                    supportingText = {
                        if (alturaError.isNotEmpty()) {
                            Text(text = alturaError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    trailingIcon = {
                        Text(
                            text = "cm",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Peso
                OutlinedTextField(
                    value = pesoText,
                    onValueChange = { pesoText = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = pesoError.isNotEmpty(),
                    supportingText = {
                        if (pesoError.isNotEmpty()) {
                            Text(text = pesoError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    trailingIcon = {
                        Text(
                            text = "kg",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )


                Spacer(modifier = Modifier.height(24.dp))

                // Botones
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
                        onClick = {
                            onConfirm(
                                alturaText.toFloatOrNull() ?: 0f,
                                pesoText.toFloatOrNull() ?: 0f,
                            )
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFAB47BC).copy(alpha = 0.5f)
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeGoalsDialog(
    pesoActual: Float,
    objetivoPeso: Float,
    objetivoTiempo: Float,
    onDismiss: () -> Unit,
    onConfirm: (objetivoPeso: Float, objetivoTiempo: Float) -> Unit
) {
    var objetivoPesoText by remember { mutableStateOf(if (objetivoPeso > 0f) objetivoPeso.toString() else "") }
    var objetivoTiempoText by remember { mutableStateOf(if (objetivoTiempo > 0f) objetivoTiempo.toString() else "") }

    val objetivoPesoError = remember(objetivoPesoText, pesoActual) {
        when {
            objetivoPesoText.isEmpty() -> "El peso objetivo es requerido"
            objetivoPesoText.toFloatOrNull() == null -> "Debe ser un número válido"
            objetivoPesoText.toFloat() < 20 || objetivoPesoText.toFloat() > 300 -> "Debe estar entre 20 y 300 kg"
            pesoActual > 0 && objetivoPesoText.toFloat() == pesoActual -> "Debe ser diferente al peso actual"
            else -> ""
        }
    }

    val objetivoTiempoError = remember(objetivoTiempoText) {
        when {
            objetivoTiempoText.isEmpty() -> "El tiempo objetivo es requerido"
            objetivoTiempoText.toFloatOrNull() == null -> "Debe ser un número válido"
            objetivoTiempoText.toFloat() < 1 || objetivoTiempoText.toFloat() > 104 -> "Debe estar entre 1 y 104 semanas"
            else -> ""
        }
    }

    val isFormValid = objetivoPesoError.isEmpty() && objetivoTiempoError.isEmpty()

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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Mis objetivos",
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

                // Peso actual (informativo)
                if (pesoActual > 0) {
                    Text(
                        text = "Tu peso actual: $pesoActual kg",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                // Objetivo de peso
                OutlinedTextField(
                    value = objetivoPesoText,
                    onValueChange = { objetivoPesoText = it },
                    label = { Text("Peso objetivo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = objetivoPesoError.isNotEmpty(),
                    supportingText = {
                        if (objetivoPesoError.isNotEmpty()) {
                            Text(text = objetivoPesoError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    trailingIcon = {
                        Text(
                            text = "kg",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Objetivo de tiempo
                OutlinedTextField(
                    value = objetivoTiempoText,
                    onValueChange = { objetivoTiempoText = it },
                    label = { Text("Tiempo objetivo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = objetivoTiempoError.isNotEmpty(),
                    supportingText = {
                        if (objetivoTiempoError.isNotEmpty()) {
                            Text(text = objetivoTiempoError, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    trailingIcon = {
                        Text(
                            text = "semanas",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))


                // Botones
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
                        onClick = {
                            onConfirm(
                                objetivoPesoText.toFloatOrNull() ?: 0f,
                                objetivoTiempoText.toFloatOrNull() ?: 0f,
                            )
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFAB47BC).copy(alpha = 0.5f)
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
fun ActivityLevelDialog(
    currentLevel: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedLevel by remember { mutableStateOf(currentLevel ?: "") }

    // Niveles de actividad con descripciones para ayudar al usuario
    val activityLevels = listOf(
        Pair("Sedentario", "Poco o ningún ejercicio, trabajo de oficina"),
        Pair("Ligero", "Ejercicio ligero 1-3 días por semana"),
        Pair("Moderado", "Ejercicio moderado 3-5 días por semana"),
        Pair("Activo", "Ejercicio intenso 6-7 días por semana"),
        Pair("Muy activo", "Ejercicio muy intenso o trabajo físico diario")
    )

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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Nivel de actividad",
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Texto explicativo
                Text(
                    text = "Selecciona el nivel que mejor describe tu actividad física regular:",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Opciones de nivel de actividad
                activityLevels.forEach { (level, description) ->
                    ActivityLevelOption(
                        level = level,
                        description = description,
                        isSelected = selectedLevel == level,
                        onSelect = { selectedLevel = level }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
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
                        onClick = { onConfirm(selectedLevel) },
                        enabled = selectedLevel.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFAB47BC).copy(alpha = 0.5f)
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
private fun ActivityLevelOption(
    level: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3F2C50) else Color(0xFF252525)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFAB47BC),
                    unselectedColor = Color.Gray
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = level,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}