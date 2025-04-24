package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.base64ToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import kotlin.String
import com.example.proyectofinalandroid.ViewModel.EntrenamientoRealizadoViewModel
import com.example.proyectofinalandroid.ViewModel.SerieRealizadaViewModel
import com.example.proyectofinalandroid.ViewModel.EjercicioRealizadoViewModel

// Colores principales - extraídos para mejor mantenimiento
private val primaryPurple = Color(0xFFAB47BC)
private val darkPurple = Color(0xFF7B1FA2)
private val backgroundDark = Color(0xFF0D0D0D)
private val surfaceDark = Color(0xFF1A1A1A)
private val darkGray = Color(0xFF252525)
private val lightGray = Color.LightGray


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComenzarEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String
) {
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel()
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(parentEntry)
    val entrenamientoRealizadoViewModel: EntrenamientoRealizadoViewModel = hiltViewModel()
    val ejercicioRealizadoViewModel: EjercicioRealizadoViewModel = hiltViewModel()
    val serieRealizadaViewModel: SerieRealizadaViewModel = hiltViewModel()
    val ejerciciosRealizados by ejercicioRealizadoViewModel.ejerciciosRealizados.collectAsState()
    val series by serieRealizadaViewModel.seriesRealizadas.collectAsState()
    val usuario by usuariosViewModel.usuario.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(usuario!!)
            ejerciciosViewModel.setUsuario(usuario!!)
        }
    }

    var isAnimatedIn by remember { mutableStateOf(false) }
    val ejerciciosCargados = remember { mutableStateMapOf<String, Ejercicios>() }
    val entrenamientoSeleccionado by entrenamientosViewModel.entrenamientoSeleccionado.collectAsState()
    val isLoading by entrenamientosViewModel.isLoading.collectAsState()

    // Estado para controlar los diálogos
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // Estado para el cronómetro con animación
    var tiempoTranscurrido by remember { mutableStateOf(0L) }
    var tiempoActivo by remember { mutableStateOf(true) }
    val tiempoFormateado = formatearTiempo(tiempoTranscurrido)

    // Animación para el cronómetro
    val infiniteTransition = rememberInfiniteTransition(label = "cronometro")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Cargar el entrenamiento por ID cuando se inicia la pantalla
    LaunchedEffect(entrenamientoId) {
        entrenamientosViewModel.getOne(entrenamientoId)
        delay(100)
        isAnimatedIn = true
    }

    // Iniciar cronómetro cuando se carga la pantalla
    LaunchedEffect(tiempoActivo) {
        while (tiempoActivo) {
            delay(1000)
            tiempoTranscurrido += 1
        }
    }

    // Cargar los ejercicios cuando cambia el entrenamiento seleccionado
    LaunchedEffect(entrenamientoSeleccionado) {
        entrenamientoSeleccionado?.ejercicios?.forEach { ejercicioId ->
            coroutineScope.launch {
                if (!ejerciciosCargados.containsKey(ejercicioId)) {
                    ejerciciosViewModel.getOne(ejercicioId)
                    ejerciciosViewModel.ejercicioSeleccionado.collect { ejercicio ->
                        if (ejercicio != null && ejercicio._id == ejercicioId) {
                            ejerciciosCargados[ejercicioId] = ejercicio
                            return@collect
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (showCancelDialog) {
        AlertaConfirmacion(
            titulo = "Cancelar entrenamiento",
            mensaje = "¿Estás seguro de que deseas cancelar el entrenamiento? No se guardarán tus progresos.",
            confirmButtonText = "Abandonar",
            confirmButtonColor = Color(0xFFE57373),
            onConfirm = {
                tiempoActivo = false
                showCancelDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showCancelDialog = false
            }
        )
    }

    if (showFinishDialog) {
        AlertaConfirmacion(
            titulo = "Finalizar entrenamiento",
            mensaje = "¿Estás seguro de que deseas finalizar el entrenamiento? Se guardarán tus progresos.",
            confirmButtonText = "Finalizar",
            confirmButtonColor = darkPurple,
            onConfirm = {
                tiempoActivo = false
                entrenamientoRealizadoViewModel.guardarEntrenamiento(
                    entrenamientoId = entrenamientoId,
                    usuarioId = usuario!!._id,
                    duracion = tiempoFormateado,
                    viewModelEjercicio = ejercicioRealizadoViewModel,
                    viewModelSerie = serieRealizadaViewModel
                )
                showFinishDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showFinishDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundDark)
    ) {
        AnimatedVisibility(
            visible = isAnimatedIn,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600)
            )
        ) {
            if (isLoading) {
                LoadingScreen()
            } else {
                entrenamientoSeleccionado?.let { entrenamiento ->
                    // Fondo con imagen difuminada para mejor contraste
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Image(
                            bitmap = base64ToImageBitmap(entrenamiento.foto)!!,
                            contentDescription = "Imagen de entrenamiento",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().blur(15.dp).alpha(0.2f)
                        )

                        // Overlay para mejorar legibilidad
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0f to backgroundDark.copy(alpha = 0.85f),
                                        0.4f to backgroundDark.copy(alpha = 0.75f),
                                        1f to backgroundDark.copy(alpha = 0.95f)
                                    )
                                )
                        )
                    }

                    // Contenido principal con layout mejorado
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Header con cronómetro prominente
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Encabezado con efecto de vidrio y cronómetro central
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                // Imagen del entrenamiento en la cabecera
                                Image(
                                    bitmap = base64ToImageBitmap(entrenamiento.foto)!!,
                                    contentDescription = "Imagen de entrenamiento",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                )

                                // Degradado para legibilidad
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                0f to backgroundDark.copy(alpha = 0.9f),
                                                0.5f to backgroundDark.copy(alpha = 0.7f),
                                                1f to backgroundDark.copy(alpha = 0.9f)
                                            )
                                        )
                                )

                                // Contenido del header
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 35.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Logo con estilo mejorado
                                    Text(
                                        text = "FitSphere",
                                        style = TextStyle(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            shadow = Shadow(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                blurRadius = 4f,
                                                offset = Offset(2f, 2f)
                                            ),
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    primaryPurple,
                                                    darkPurple
                                                )
                                            )
                                        )
                                    )

                                    // Cronómetro con animación y mejor visibilidad
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        darkPurple.copy(alpha = 0.7f),
                                                        darkPurple.copy(alpha = 0.4f)
                                                    )
                                                )
                                            )
                                            .border(
                                                width = 2.dp,
                                                brush = Brush.linearGradient(
                                                    colors = listOf(primaryPurple, darkPurple)
                                                ),
                                                shape = CircleShape
                                            )
                                            .scale(scale = pulseSize),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tiempoFormateado,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }

                                // Botón de regreso en la esquina
                                IconButton(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier
                                        .padding(16.dp, 35.dp, 16.dp, 16.dp)
                                        .size(48.dp)
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = CircleShape
                                        )
                                        .background(
                                            color = surfaceDark.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                        .align(Alignment.TopStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Título del entrenamiento y categoría
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                // Nombre del entrenamiento
                                Text(
                                    text = entrenamiento.nombre,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = Color.White
                                    )
                                )

                                // Categoría con diseño integrado
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "Categoría",
                                        tint = primaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Text(
                                        text = entrenamiento.categoria,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = primaryPurple,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            // Título de sección ejercicios
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ejercicios",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                )

                                // Línea separadora dinámica
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .height(1.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    primaryPurple.copy(alpha = 0.7f),
                                                    primaryPurple.copy(alpha = 0.0f)
                                                )
                                            )
                                        )
                                )
                            }

                            // Lista de ejercicios con LazyColumn para mejor rendimiento
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(entrenamiento.ejercicios) { ejercicioId ->
                                    Log.d("FalloEjercicios1", "${ejercicioId}")
                                    val ejercicio = ejerciciosCargados[ejercicioId]
                                    if (ejercicio != null) {
                                        EjercicioConSeries(ejercicio, serieRealizadaViewModel)
                                    } else {
                                        TarjetaEjercicioCargando()
                                    }
                                }

                                // Espacio al final para el botón flotante
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(100.dp))
                        }

                        // Botón de finalizar flotante
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(bottom = 40.dp)
                        ) {
                            Button(
                                onClick = { showFinishDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = darkPurple
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Finalizar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FINALIZAR ENTRENAMIENTO",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = primaryPurple,
                modifier = Modifier.size(60.dp),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cargando entrenamiento...",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun EjercicioConSeries(ejercicio: Ejercicios, serieRealizadaViewModel: SerieRealizadaViewModel) {
    // Estado para controlar la lista de series
    val series = remember { mutableStateListOf<SerieRealizada>() }

    // Estado para el diálogo de añadir serie
    var showAddSerieDialog by remember { mutableStateOf(false) }

    // Estado de expansión para mostrar/ocultar series
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = darkPurple,
                spotColor = darkPurple
            ),
        colors = CardDefaults.cardColors(containerColor = surfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Card header con información del ejercicio y botón de expansión
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del ejercicio con borde redondeado
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                ) {

                    if (ejercicio.foto.isEmpty()) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Imagen de ejercicio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            bitmap = base64ToImageBitmap(ejercicio.foto)!!,
                            contentDescription = "Imagen de ejercicio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradiente sobre la imagen para mejorar legibilidad
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )
                }

                // Información del ejercicio
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = ejercicio.nombre,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grupo muscular con icono
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Músculo",
                            tint = primaryPurple,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = ejercicio.musculo,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = primaryPurple
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Contador de series
                    if (series.isNotEmpty()) {
                        Text(
                            text = "${series.size} ${if (series.size == 1) "serie" else "series"} registradas",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = lightGray,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Icono de expansión
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Contraer" else "Expandir",
                        tint = Color.White
                    )
                }
            }

            // Contenido expandido con las series
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Lista de series registradas
                    if (series.isNotEmpty()) {
                        Text(
                            text = "Series registradas",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        series.forEachIndexed { index, serie ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = darkGray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Numeración de serie con círculo
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = darkPurple.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryPurple
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Información de la serie
                                    Column {
                                        // Peso
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Peso:",
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = "${serie.peso} kg",
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }

                                        // Repeticiones
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Repeticiones:",
                                                style = TextStyle(
                                                    fontSize = 13.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Text(
                                                text = serie.repeticiones.toString(),
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Botón para eliminar serie
                                    IconButton(
                                        onClick = { series.removeAt(index) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE57373).copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFE57373),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Botón de añadir serie mejorado
                    Button(
                        onClick = { showAddSerieDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkPurple.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir serie",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Añadir serie",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Si no está expandido, añadir un indicador visual
            if (!expanded && series.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Puntos indicando series registradas
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(minOf(series.size, 5)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = primaryPurple,
                                        shape = CircleShape
                                    )
                            )
                        }

                        // Si hay más de 5 series, mostrar +X
                        if (series.size > 5) {
                            Text(
                                text = "+${series.size - 5}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = primaryPurple,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

// Diálogo para añadir serie con mejor diseño
    if (showAddSerieDialog) {
        DialogAñadirSerie(
            onAddSerie = { peso, repeticiones ->
                val numeroSerie = series.size + 1
                Log.d("FalloSerie1", "Añadiendo serie: $peso kg, $repeticiones repeticiones, serie $numeroSerie, ejercicio ${ejercicio._id}")
                val serieNueva = SerieRealizada(peso = peso.toFloat(),
                                    repeticiones = repeticiones.toInt(),
                                    numeroSerie = numeroSerie,
                                    ejercicioRealizado = ejercicio._id
                )
                series.add(serieNueva)
                serieRealizadaViewModel.anadirSerieALista(serieNueva)
                showAddSerieDialog = false
            },
            onDismiss = { showAddSerieDialog = false }
        )
    }
}

@Composable
fun TarjetaEjercicioCargando() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder para imagen
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(
                        color = darkGray
                    )
            )

            // Placeholders para texto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Placeholder para título
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp)
                        .background(
                            color = darkGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Placeholder para subtítulo
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .background(
                            color = darkGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 16.dp),
                color = primaryPurple,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun DialogAñadirSerie(
    onAddSerie: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var peso by remember { mutableStateOf("") }
    var repeticiones by remember { mutableStateOf("") }
    var pesoError by remember { mutableStateOf(false) }
    var repeticionesError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título del diálogo
                Text(
                    text = "Añadir serie",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo de peso
                Column {
                    Text(
                        text = "Peso (kg)",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (pesoError) Color(0xFFE57373) else Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = peso,
                        onValueChange = {
                            peso = it
                            pesoError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryPurple,
                            unfocusedBorderColor = darkGray,
                            errorBorderColor = Color(0xFFE57373),
                            focusedContainerColor = darkGray,
                            unfocusedContainerColor = darkGray,
                            cursorColor = primaryPurple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = pesoError,
                        trailingIcon = {
                            if (pesoError) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFE57373)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (pesoError) {
                        Text(
                            text = "Ingresa un peso válido",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFFE57373)
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de repeticiones
                Column {
                    Text(
                        text = "Repeticiones",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = if (repeticionesError) Color(0xFFE57373) else Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = repeticiones,
                        onValueChange = {
                            repeticiones = it
                            repeticionesError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryPurple,
                            unfocusedBorderColor = darkGray,
                            errorBorderColor = Color(0xFFE57373),
                            focusedContainerColor = darkGray,
                            unfocusedContainerColor = darkGray,
                            cursorColor = primaryPurple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        isError = repeticionesError,
                        trailingIcon = {
                            if (repeticionesError) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFE57373)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (repeticionesError) {
                        Text(
                            text = "Ingresa un número válido de repeticiones",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFFE57373)
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón cancelar
                    OutlinedButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, darkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Botón guardar
                    Button(
                        onClick = {
                            // Validación
                            pesoError = peso.isEmpty() || peso.toFloatOrNull() == null
                            repeticionesError = repeticiones.isEmpty() || repeticiones.toIntOrNull() == null

                            if (!pesoError && !repeticionesError) {
                                onAddSerie(peso, repeticiones)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkPurple
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaConfirmacion(
    titulo: String,
    mensaje: String,
    confirmButtonText: String,
    confirmButtonColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de advertencia
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Advertencia",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = titulo,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje
                Text(
                    text = mensaje,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón cancelar
                    OutlinedButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, darkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Botón confirmar
                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmButtonColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = confirmButtonText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Función para formatear el tiempo en HH:MM:SS
@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
fun formatearTiempo(segundos: Long): String {
    val horas = TimeUnit.SECONDS.toHours(segundos)
    val minutos = TimeUnit.SECONDS.toMinutes(segundos) % 60
    val segs = segundos % 60

    return String.format("%02d:%02d:%02d", horas, minutos, segs)
}