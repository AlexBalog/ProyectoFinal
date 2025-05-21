package com.example.proyectofinalandroid.View

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.SolidColor
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTrainingScreen(
    navController: NavController,
    publicar: Boolean = false,
    entrenamiento: Entrenamientos? = null
) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    // ViewModels de la aplicación
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel(parentEntry)
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)

    // Estados para el formulario - Usamos mutableStateOf como propiedades para reducir recomposiciones
    val nombreState = remember { mutableStateOf(entrenamiento?.nombre ?: "") }
    val categoriaState = remember { mutableStateOf(entrenamiento?.categoria ?: "") }
    val musculoPrincipalState = remember { mutableStateOf(entrenamiento?.musculoPrincipal ?: "") }
    var duracionTemp = "${entrenamiento?.duracion}"
    if (duracionTemp.equals("null")) {
        duracionTemp = ""
    }
    val duracionState = remember { mutableStateOf(duracionTemp ?: "") }
    val fotoBase64State = remember { mutableStateOf<String?>(entrenamiento?.foto ?: null) }
    val selectedImageUriState = remember { mutableStateOf<Uri?>(
        if (entrenamiento?.foto != null && entrenamiento?.foto!!.isNotEmpty())
            Uri.parse("content://placeholder/image")
        else
            null
    ) }
    val musculosSeleccionadosState = remember { mutableStateOf<List<String>>(entrenamiento?.musculo ?: emptyList()) }
    val ejerciciosSeleccionadosState = remember { mutableStateOf<List<String>>(entrenamiento?.ejercicios ?: emptyList()) }

    // Accesos locales para reducir recomposiciones
    var nombre by nombreState
    var categoriaSelecionada by categoriaState
    var musculoPrincipalSeleccionado by musculoPrincipalState
    var duracion by duracionState
    var fotoBase64 by fotoBase64State
    var selectedImageUri by selectedImageUriState
    var musculosSeleccionados by musculosSeleccionadosState
    var ejerciciosSeleccionados by ejerciciosSeleccionadosState

    // Estado para diálogos - Usando remember para preservar estado
    var showCategoriaDialog by remember { mutableStateOf(false) }
    var showMusculoPrincipalDialog by remember { mutableStateOf(false) }
    var showMusculosDialog by remember { mutableStateOf(false) }
    var showEjerciciosDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Estado para animaciones y carga
    var isLoading by remember { mutableStateOf(false) }
    var isImageLoading by remember { mutableStateOf(false) }

    // Estados para controlar el foco de los TextField
    var isNombreFocused by remember { mutableStateOf(false) }
    var isDuracionFocused by remember { mutableStateOf(false) }
    // Obtener usuario actual
    val usuario by usuariosViewModel.usuario.collectAsState()
    val ejercicios by ejerciciosViewModel.ejercicios.collectAsState()

    // Listas predefinidas
    val categorias = remember { listOf("Fuerza", "Hipertrofia", "Resistencia", "Cardio", "HIIT", "Full Body") }
    val musculos = remember { listOf("Pecho", "Espalda", "Hombros", "Bíceps", "Tríceps", "Cuádriceps", "Isquiotibiales", "Glúteos", "Abdominales", "Antebrazos", "Gemelos", "Trapecio", "Lumbar") }

    // Validar formulario con derivedStateOf para evitar recomposiciones innecesarias
    val isFormValid by remember(
        nombre, categoriaSelecionada, musculoPrincipalSeleccionado, duracion,
        fotoBase64, musculosSeleccionados, ejerciciosSeleccionados
    ) {
        derivedStateOf {
            nombre.isNotBlank() &&
                    categoriaSelecionada.isNotBlank() &&
                    musculoPrincipalSeleccionado.isNotBlank() &&
                    duracion.isNotBlank() &&
                    fotoBase64 != null &&
                    musculosSeleccionados.isNotEmpty() &&
                    ejerciciosSeleccionados.isNotEmpty()
        }
    }

    // Contexto para convertir imagen
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Lanzador para seleccionar imagen
    // Lanzador para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            isImageLoading = true
            scope.launch(Dispatchers.IO) { // Explicitly use IO dispatcher
                try {
                    // First show a small preview quickly
                    val thumbnail = withContext(Dispatchers.IO) {
                        val bitmap = uriToBitmap(context, it)?.let { bmp ->
                            resizeBitmap(bmp, 300, 300) // Small thumbnail for immediate display
                        }
                        bitmap?.let { bitmapToBase64(it) }
                    }

                    // Update UI with thumbnail first for immediate feedback
                    if (thumbnail != null) {
                        withContext(Dispatchers.Main) {
                            fotoBase64 = thumbnail
                        }
                    }

                    // Then process the full quality image
                    val fullQualityBase64 = withContext(Dispatchers.IO) {
                        uriToBase64(context, it)
                    }

                    // Update UI with full quality image
                    withContext(Dispatchers.Main) {
                        fotoBase64 = fullQualityBase64
                        isImageLoading = false
                    }
                } catch (e: Exception) {
                    Log.e("CreateTraining", "Error converting image: ${e.message}")
                    withContext(Dispatchers.Main) {
                        isImageLoading = false
                        Toast.makeText(context, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Cargar ejercicios
    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(it)
            ejerciciosViewModel.setUsuario(it)
        }
    }


    // UI principal con fondo oscuro
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = if (publicar) "Editar Entrenamiento" else "Crear Entrenamiento",
                        fontSize = 24.sp,
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

            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Sección de información básica
                FormSectionTitle(title = "Información Básica")

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre del entrenamiento
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre del entrenamiento",
                    placeholder = "Ej: Entrenamiento de Pecho y Tríceps",
                    icon = Icons.Default.Title,
                    modifier = Modifier.fillMaxWidth(),
                    isFocused = isNombreFocused,
                    onFocusChanged = { isNombreFocused = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Categoría (Selector)
                SelectableField(
                    value = categoriaSelecionada,
                    label = "Categoría",
                    placeholder = "Selecciona una categoría",
                    icon = Icons.Default.Category,
                    onClick = { showCategoriaDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Músculo Principal (Selector)
                SelectableField(
                    value = musculoPrincipalSeleccionado,
                    label = "Músculo Principal",
                    placeholder = "Selecciona el músculo principal",
                    icon = Icons.Default.FitnessCenter,
                    onClick = { showMusculoPrincipalDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de duración y foto
                FormSectionTitle(title = "Duración y Foto")

                Spacer(modifier = Modifier.height(16.dp))

                // Duración con validación de máximo 180
                CustomTextField(
                    value = duracion,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            val numValue = it.toIntOrNull() ?: 0
                            if (numValue <= 180) {
                                duracion = it
                            } else {
                                duracion = "180"
                            }
                        }
                    },
                    label = "Duración (minutos)",
                    placeholder = "Ej: 45 (máximo 180)",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isFocused = isDuracionFocused,
                    onFocusChanged = { isDuracionFocused = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de foto
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color(0xFF7B1FA2),
                            ambientColor = Color(0xFF7B1FA2)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Foto del Entrenamiento",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vista previa de la imagen o botón para seleccionar
                        if (selectedImageUri != null && fotoBase64 != null) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2A2A2A))
                            ) {
                                val bitmap = getImageBitmapSafely(fotoBase64!!)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Foto seleccionada",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Botón para cambiar imagen
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7B1FA2))
                                            .clickable { imagePickerLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Cambiar imagen",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2A2A2A))
                                    .clickable(enabled = !isImageLoading) {
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isImageLoading) {
                                    CircularProgressIndicator(color = Color(0xFFAB47BC))
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Seleccionar imagen",
                                            tint = Color(0xFFAB47BC),
                                            modifier = Modifier.size(48.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Toca para seleccionar una imagen",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de músculos
                FormSectionTitle(title = "Músculos Trabajados")

                Spacer(modifier = Modifier.height(16.dp))

                // Músculos específicos (Selector múltiple)
                SelectableField(
                    value = if (musculosSeleccionados.isEmpty()) "Ninguno seleccionado"
                    else musculosSeleccionados.joinToString(", "),
                    label = "Músculos Específicos",
                    placeholder = "Selecciona los músculos trabajados",
                    icon = Icons.Default.FitnessCenter,
                    onClick = { showMusculosDialog = true }
                )

                // Chips de músculos seleccionados
                if (musculosSeleccionados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        mainAxisSpacing = 8,
                        crossAxisSpacing = 8
                    ) {
                        musculosSeleccionados.forEach { musculo ->
                            Chip(
                                label = musculo,
                                onRemove = {
                                    musculosSeleccionados = musculosSeleccionados.filter { it != musculo }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de ejercicios
                FormSectionTitle(title = "Ejercicios")

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de ejercicios
                SelectableField(
                    value = if (ejerciciosSeleccionados.isEmpty()) "Ninguno seleccionado"
                    else "${ejerciciosSeleccionados.size} ejercicios seleccionados",
                    label = "Ejercicios",
                    placeholder = "Selecciona los ejercicios",
                    icon = Icons.Default.FitnessCenter,
                    onClick = { showEjerciciosDialog = true }
                )

                // Lista de ejercicios seleccionados
                if (ejerciciosSeleccionados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    EjerciciosSeleccionadosList(
                        ejerciciosIds = ejerciciosSeleccionados,
                        ejercicios = ejercicios ?: emptyList(),
                        onRemove = { ejercicioId ->
                            ejerciciosSeleccionados = ejerciciosSeleccionados.filter { it != ejercicioId }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (publicar) {
                    // Botones de guardar y publicar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botón guardar con diseño mejorado
                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    try {
                                        Toast.makeText(context, "Editando entrenamiento...", Toast.LENGTH_LONG).show()

                                        entrenamiento!!.musculoPrincipal = musculoPrincipalSeleccionado
                                        entrenamiento!!.categoria = categoriaSelecionada
                                        entrenamiento!!.nombre = nombre.trim()
                                        entrenamiento!!.duracion = duracion.toInt()
                                        entrenamiento!!.foto = fotoBase64.toString() ?: ""
                                        entrenamiento!!.musculo = musculosSeleccionados
                                        entrenamiento!!.ejercicios = ejerciciosSeleccionados

                                        entrenamientosViewModel.actualizarEntrenamiento(entrenamiento)
                                        // Navegar solo si todo fue exitoso
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            showSuccessDialog = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Error", "Error al guardar: ${e.message}")
                                        Toast.makeText(context, "Error al crear entrenamiento", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF3A3A3A)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Guardar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { showPublishDialog = true },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A1A1A),
                                contentColor = Color(0xFFAB47BC),
                                disabledContainerColor = Color(0xFF222222)
                            ),
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (isFormValid && !isLoading) Color(0xFFAB47BC) else Color(0xFF444444)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Publish,
                                contentDescription = "Publicar",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Publicar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                isLoading = true
                                scope.launch {
                                    try {
                                        Toast.makeText(context, "Creando entrenamiento...", Toast.LENGTH_LONG).show()

                                        val nuevoEntrenamiento = Entrenamientos(
                                            musculoPrincipal = musculoPrincipalSeleccionado,
                                            categoria = categoriaSelecionada,
                                            nombre = nombre.trim(),
                                            duracion = duracion.toInt(),
                                            foto = fotoBase64.toString() ?: "",
                                            musculo = musculosSeleccionados,
                                            likes = 0,
                                            ejercicios = ejerciciosSeleccionados,
                                            creador = usuario?._id?.toString() ?: "",
                                            aprobado = false,
                                            pedido = false,
                                            motivoRechazo = ""
                                        )

                                        entrenamientosViewModel.new(nuevoEntrenamiento)
                                        // Navegar solo si todo fue exitoso
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            showSuccessDialog = true
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Error", "Error al guardar: ${e.message}")
                                        Toast.makeText(context, "Error al crear entrenamiento", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7B1FA2),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF3A3A3A)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Crear",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Espacio para el footer
            }
        }

        // Diálogos
        if (showCategoriaDialog) {
            SelectionDialog(
                title = "Selecciona una categoría",
                options = categorias,
                selectedOption = categoriaSelecionada,
                onOptionSelected = { categoria ->
                    categoriaSelecionada = categoria
                    showCategoriaDialog = false
                },
                onDismiss = { showCategoriaDialog = false }
            )
        }

        if (showMusculoPrincipalDialog) {
            SelectionDialog(
                title = "Selecciona el músculo principal",
                options = musculos,
                selectedOption = musculoPrincipalSeleccionado,
                onOptionSelected = { musculo ->
                    musculoPrincipalSeleccionado = musculo
                    showMusculoPrincipalDialog = false
                },
                onDismiss = { showMusculoPrincipalDialog = false }
            )
        }

        if (showMusculosDialog) {
            MultiSelectionDialog(
                title = "Selecciona los músculos trabajados",
                options = musculos,
                selectedOptions = musculosSeleccionados,
                onOptionsSelected = { musculos ->
                    musculosSeleccionados = musculos
                    showMusculosDialog = false
                },
                onDismiss = { showMusculosDialog = false }
            )
        }

        if (showEjerciciosDialog) {
            EjerciciosSelectionDialogOptimized(
                ejercicios = ejercicios ?: emptyList(),
                selectedEjercicios = ejerciciosSeleccionados,
                onEjerciciosSelected = { ejercicios ->
                    ejerciciosSeleccionados = ejercicios
                    showEjerciciosDialog = false
                },
                onDismiss = { showEjerciciosDialog = false }
            )
        }

        if (showPublishDialog) {
            ConfirmationDialog(
                title = "Publicar Entrenamiento",
                message = "¿Estás seguro de que quieres solicitar la publicación de este entrenamiento en la comunidad?",
                confirmButtonText = "Publicar",
                dismissButtonText = "Cancelar",
                onConfirm = {
                    scope.launch {
                        entrenamiento!!.pedido = true
                        entrenamientosViewModel.actualizarEntrenamiento(entrenamiento)
                        Toast.makeText(context, "¡Se ha procesado la petición!", Toast.LENGTH_SHORT).show()
                        showPublishDialog = false
                        navController.popBackStack()
                    }

                },
                onDismiss = { showPublishDialog = false }
            )
        }

        if (showSuccessDialog) {
            SuccessDialog(
                message = "Tu entrenamiento ha sido guardado correctamente",
                onDismiss = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isFocused: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    // Ahora resalta el campo si tiene foco O si tiene contenido
    val isActive = isFocused || value.isNotBlank()

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF7B1FA2),
                ambientColor = Color(0xFF7B1FA2)
            ),
        color = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Label con animación - ahora siempre destacado si tiene contenido
            Text(
                text = label,
                color = if (isActive) Color(0xFFAB47BC) else Color.Gray,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Campo de texto
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { onFocusChanged(it.isFocused) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(Color(0xFFAB47BC)),
                keyboardOptions = keyboardOptions,
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icono también conserva el color si hay contenido
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isActive) Color(0xFFAB47BC) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun FormSectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gradient divider left
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(24.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2).copy(alpha = 0.0f),
                            Color(0xFF7B1FA2)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title text
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAB47BC)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Gradient divider right
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7B1FA2),
                            Color(0xFF7B1FA2).copy(alpha = 0.0f)
                        )
                    )
                )
        )
    }
}

@Composable
fun SelectableField(
    value: String,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    // Ahora colorea el componente si tiene contenido
    val hasContent = value.isNotBlank() && value != "Ninguno seleccionado"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF7B1FA2),
                ambientColor = Color(0xFF7B1FA2)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (hasContent) Color(0xFFAB47BC) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = if (hasContent) Color(0xFFAB47BC) else Color.Gray
                )

                if (value == "Ninguno seleccionado" || value.isBlank()) {
                    Text(
                        text = placeholder,
                        fontSize = 16.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Seleccionar",
                tint = if (hasContent) Color(0xFFAB47BC) else Color.Gray
            )
        }
    }
}

@Composable
fun Chip(
    label: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF7B1FA2).copy(alpha = 0.2f),
                        Color(0xFFAB47BC).copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFFAB47BC)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFAB47BC).copy(alpha = 0.2f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
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
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = Color(0xFF3A3A3A))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(options) { option ->
                        val isSelected = option == selectedOption

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option) }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color(0xFFAB47BC),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = if (isSelected) Color(0xFFAB47BC) else Color.White
                            )
                        }

                        if (option != options.last()) {
                            Divider(
                                color = Color(0xFF2A2A2A),
                                modifier = Modifier.padding(start = 48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAB47BC),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Confirmar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MultiSelectionDialog(
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onOptionsSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Mantenemos estado en este componente para evitar recomposiciones en la pantalla principal
    val tempSelected = remember { mutableStateListOf<String>() }

    // Solo inicializamos el estado una vez cuando se abre el diálogo
    LaunchedEffect(Unit) {
        tempSelected.clear()
        tempSelected.addAll(selectedOptions)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
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
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = Color(0xFF3A3A3A))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(options) { option ->
                        val isSelected = tempSelected.contains(option)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        tempSelected.remove(option)
                                    } else {
                                        tempSelected.add(option)
                                    }
                                }
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckBox,
                                    contentDescription = "Seleccionado",
                                    tint = Color(0xFFAB47BC),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = "No seleccionado",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = if (isSelected) Color(0xFFAB47BC) else Color.White
                            )
                        }

                        if (option != options.last()) {
                            Divider(
                                color = Color(0xFF2A2A2A),
                                modifier = Modifier.padding(start = 48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onOptionsSelected(tempSelected.toList()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

// Nueva versión optimizada del diálogo de selección de ejercicios
@Composable
fun EjerciciosSelectionDialogOptimized(
    ejercicios: List<Ejercicios>,
    selectedEjercicios: List<String>,
    onEjerciciosSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    // Mantenemos estado localmente para evitar recomposiciones innecesarias
    val tempSelected = remember { mutableStateListOf<String>() }
    var filterMusculo by remember { mutableStateOf("") }

    // Inicializamos el estado solo una vez cuando se abre el diálogo
    LaunchedEffect(Unit) {
        tempSelected.clear()
        tempSelected.addAll(selectedEjercicios)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
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
            ) {
                // Encabezado con contador que se actualiza en tiempo real
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Selecciona Ejercicios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "${tempSelected.size} seleccionados",
                        fontSize = 14.sp,
                        color = Color(0xFFAB47BC)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFF3A3A3A))

                Spacer(modifier = Modifier.height(16.dp))

                // Filtro por grupo muscular
                val gruposMusculares = remember(ejercicios) {
                    ejercicios.map { it.musculo }.distinct().sorted()
                }

                if (gruposMusculares.isNotEmpty()) {
                    Text(
                        text = "Filtrar por grupo muscular",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                label = "Todos",
                                isSelected = filterMusculo.isEmpty(),
                                onClick = { filterMusculo = "" }
                            )
                        }

                        items(gruposMusculares) { musculo ->
                            FilterChip(
                                label = musculo,
                                isSelected = filterMusculo == musculo,
                                onClick = {
                                    filterMusculo = if (filterMusculo == musculo) "" else musculo
                                }
                            )
                        }
                    }
                }

                val filteredEjercicios = remember(ejercicios, filterMusculo) {
                    ejercicios.filter {
                        filterMusculo.isEmpty() || it.musculo == filterMusculo
                    }
                }

                // Lista de ejercicios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(filteredEjercicios) { ejercicio ->
                        val isSelected = tempSelected.contains(ejercicio._id)

                        EjercicioItem(
                            ejercicio = ejercicio,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    tempSelected.remove(ejercicio._id)
                                } else {
                                    tempSelected.add(ejercicio._id)
                                }
                            }
                        )

                        if (ejercicio != filteredEjercicios.last()) {
                            Divider(
                                color = Color(0xFF2A2A2A),
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }

                    // Mensaje si no hay ejercicios
                    if (filteredEjercicios.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay ejercicios para mostrar",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onEjerciciosSelected(tempSelected.toList()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color.Transparent else Color(0xFF2A2A2A),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7B1FA2),
                                    Color(0xFFAB47BC)
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun EjercicioItem(
    ejercicio: Ejercicios,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen o icono
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            if (ejercicio.foto.isNotEmpty()) {
                val bitmap = getImageBitmapSafely(ejercicio.foto)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Foto del ejercicio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Ejercicio",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Ejercicio",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Información
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ejercicio.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFFAB47BC) else Color.White
            )

            Text(
                text = ejercicio.musculo,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Checkbox
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckBox,
                contentDescription = "Seleccionado",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.CheckBoxOutlineBlank,
                contentDescription = "No seleccionado",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EjerciciosSeleccionadosList(
    ejerciciosIds: List<String>,
    ejercicios: List<Ejercicios>,
    onRemove: (String) -> Unit
) {
    // Optimización: calcular solo una vez los ejercicios seleccionados
    val ejerciciosSeleccionados = remember(ejerciciosIds, ejercicios) {
        ejercicios.filter { ejercicio ->
            ejerciciosIds.contains(ejercicio._id)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0xFF7B1FA2),
                ambientColor = Color(0xFF7B1FA2)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ejercicios seleccionados (${ejerciciosSeleccionados.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider(color = Color(0xFF2A2A2A))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(ejerciciosSeleccionados) { ejercicio ->
                    EjercicioSeleccionadoItem(
                        ejercicio = ejercicio,
                        onRemove = { onRemove(ejercicio._id) }
                    )

                    if (ejercicio != ejerciciosSeleccionados.last()) {
                        Divider(
                            color = Color(0xFF2A2A2A),
                            modifier = Modifier.padding(start = 64.dp)
                        )
                    }
                }

                // Mensaje si no hay ejercicios
                if (ejerciciosSeleccionados.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No has seleccionado ningún ejercicio",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EjercicioSeleccionadoItem(
    ejercicio: Ejercicios,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen o icono
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            if (ejercicio.foto.isNotEmpty()) {
                val bitmap = getImageBitmapSafely(ejercicio.foto)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Foto del ejercicio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Ejercicio",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Ejercicio",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Información
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ejercicio.nombre,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = ejercicio.musculo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Botón eliminar
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2A))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
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
                Icon(
                    imageVector = Icons.Default.Publish,
                    contentDescription = "Publicar",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(dismissButtonText)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    var isAnimationComplete by remember { mutableStateOf(false) }

    // Animación de checkmark
    LaunchedEffect(Unit) {
        delay(500)
        isAnimationComplete = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
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
                // Círculo con checkmark animado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo base
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF7B1FA2),
                                        Color(0xFFAB47BC)
                                    )
                                )
                            )
                    )

                    // Usamos alpha para animar el ícono en lugar de AnimatedVisibility
                    val alpha by animateFloatAsState(
                        targetValue = if (isAnimationComplete) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        ),
                        label = ""
                    )

                    // Checkmark con animación de alpha y escala
                    val scale by animateFloatAsState(
                        targetValue = if (isAnimationComplete) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = ""
                    )

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Éxito",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp * scale)
                            .alpha(alpha)
                    )
                }

                Text(
                    text = "¡Éxito!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAB47BC),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Aceptar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Utilidad para organizar elementos en filas fluidas
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Int = 0,
    crossAxisSpacing: Int = 0,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Pair<Int, Int>>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Pair<Int, Int>>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Medir cada hijo
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints)
            val size = Pair(placeable.width, placeable.height)

            // Si agregamos este elemento, ¿nos pasamos del ancho?
            if (currentSequence.isNotEmpty() && currentMainAxisSize + mainAxisSpacing + placeable.width > constraints.maxWidth) {
                // Si es así, empezamos una nueva fila
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                crossAxisPositions += crossAxisSpace

                crossAxisSpace += currentCrossAxisSize + crossAxisSpacing
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
                currentSequence.clear()
            }

            // Agregar placeable al batch actual
            currentSequence.add(size)
            currentMainAxisSize += placeable.width + if (currentSequence.size > 1) mainAxisSpacing else 0
            currentCrossAxisSize = maxOf(currentCrossAxisSize, placeable.height)

            placeable
        }

        // Agregar la última fila si no está vacía
        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace
            crossAxisSpace += currentCrossAxisSize
        }

        layout(
            width = constraints.maxWidth,
            height = crossAxisSpace.coerceAtMost(constraints.maxHeight)
        ) {
            // Coordenadas para colocar hijos
            var sequenceIndex = 0
            var elementIndex = 0

            placeables.forEach { placeable ->
                // Obtener la posición X usando la alineación
                val crossAxisPosition = crossAxisPositions[sequenceIndex]
                val sequence = sequences[sequenceIndex]

                val elementSize = sequence[elementIndex]
                val elementWidth = elementSize.first
                val elementHeight = elementSize.second

                var elementX = 0
                sequence.subList(0, elementIndex).forEach { item ->
                    elementX += item.first + mainAxisSpacing
                }

                placeable.place(
                    x = elementX,
                    y = crossAxisPosition
                )

                elementIndex++

                // Mover al siguiente batch si es necesario
                if (elementIndex >= sequence.size) {
                    sequenceIndex++
                    elementIndex = 0
                }
            }
        }
    }
}

// Extensión para usar Image con ImageBitmap
@Composable
fun Image(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = 1.0f
) {
    androidx.compose.foundation.Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha
    )
}