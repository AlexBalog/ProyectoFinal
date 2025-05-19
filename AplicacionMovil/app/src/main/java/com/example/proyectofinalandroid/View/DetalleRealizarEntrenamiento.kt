package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.EjercicioRealizado
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.SerieRealizada
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EjercicioRealizadoViewModel
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientoRealizadoViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.SerieRealizadaViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.text.set

private fun printFull(series: List<SerieRealizada>) {
    Log.d("SerieRealizadaDebug", "=== TODAS LAS SERIES (${series.size}) ===")
    series.forEach { serie ->
        Log.d("SerieRealizadaDebug", "Serie ID: ${serie._id}, EjercicioID: ${serie.ejercicioRealizado}, Ejercicio Orig.: ${serie.ejercicio}, Peso: ${serie.peso}, Reps: ${serie.repeticiones}")
    }
}
// Colores principales - extraídos para mejor mantenimiento
private val primaryPurple = Color(0xFFAB47BC)
private val darkPurple = Color(0xFF7B1FA2)
private val backgroundDark = Color(0xFF0D0D0D)
private val surfaceDark = Color(0xFF1A1A1A)
private val darkGray = Color(0xFF252525)
private val lightGray = Color.LightGray

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun DetallesRealizarEntrenamientoScreen(
    navController: NavController,
    entrenamientoRealizadoId: String
) {
    // Obtener ViewModels
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientoRealizadoViewModel: EntrenamientoRealizadoViewModel = hiltViewModel(parentEntry)
    val ejercicioRealizadoViewModel: EjercicioRealizadoViewModel = hiltViewModel(parentEntry)
    val serieRealizadaViewModel: SerieRealizadaViewModel = hiltViewModel(parentEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel(parentEntry)

    // Estados
    val usuario by usuariosViewModel.usuario.collectAsState()
    var isAnimatedIn by remember { mutableStateOf(false) }
    val entrenamientoRealizado by entrenamientoRealizadoViewModel.entrenamientoRealizadoSeleccionado.collectAsState()
    val ejerciciosRealizados by ejercicioRealizadoViewModel.ejerciciosRealizados.collectAsState()
    val seriesRealizadas by serieRealizadaViewModel.seriesRealizadas.collectAsState()
    val isLoading by entrenamientoRealizadoViewModel.isLoading.collectAsState()
    val entrenamiento by entrenamientosViewModel.entrenamientoSeleccionado.collectAsState()
    val ejercicios by ejerciciosViewModel.ejercicios.collectAsState()

    // Mapa para organizar las series por ejercicio
    //val seriesPorEjercicio = remember { mutableStateMapOf<String, List<SerieRealizada>>() }

    // Estado para controlar la carga de datos
    var datosEjerciciosCargados by remember { mutableStateOf(false) }
    var datosCargando by remember { mutableStateOf(true) }
    // NUEVO: Control de carga de series para evitar ciclos
    var seriesCargadas by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Inicializar datos
    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientoRealizadoViewModel.setUsuario(it)
            ejercicioRealizadoViewModel.setUsuario(it)
            serieRealizadaViewModel.setUsuario(it)
            entrenamientosViewModel.setUsuario(it)
            ejerciciosViewModel.setUsuario(it)
        }
    }

    // Cargar entrenamiento realizado
    LaunchedEffect(entrenamientoRealizadoId) {
        entrenamientoRealizadoViewModel.getOne(entrenamientoRealizadoId)
        delay(300) // Pequeña espera para asegurar que el entrenamiento realizado se cargue
        isAnimatedIn = true
    }

    // Cargar entrenamiento original y ejercicios cuando tengamos el entrenamiento realizado
    LaunchedEffect(entrenamientoRealizado) {
        entrenamientoRealizado?.let { realizado ->
            // Cargar el entrenamiento original
            entrenamientosViewModel.getOne(realizado.entrenamiento)

            // Cargar los ejercicios realizados
            ejercicioRealizadoViewModel.getFilter(mapOf("entrenamientoRealizado" to realizado._id))

            delay(500)
            datosEjerciciosCargados = true
        }
    }

    // MODIFICADO: Ahora este LaunchedEffect sólo se encarga de cargar las series una vez
    LaunchedEffect(ejerciciosRealizados, datosEjerciciosCargados) {
        if (datosEjerciciosCargados && ejerciciosRealizados != null) {
            val ejerciciosRealizadosLocal = ejerciciosRealizados ?: emptyList()

            if (ejerciciosRealizadosLocal.isNotEmpty()) {
                // Cargar TODAS las series para todos los ejercicios
                // Este es un enfoque alternativo en caso de que el filtrado no funcione bien
                serieRealizadaViewModel.getAll()

                // Esperamos un poco para que se carguen todas las series
                delay(1000)

                // Ahora sí marcamos que las series están cargadas
                datosCargando = false
            } else {
                datosCargando = false
            }
        }
    }

    val seriesPorEjercicioState = remember { mutableStateOf<Map<String, List<SerieRealizada>>>(emptyMap()) }
    val seriesPorEjercicio by seriesPorEjercicioState

    // NUEVO: Efecto separado para organizar las series por ejercicio
    // Modifica este efecto
    LaunchedEffect(seriesRealizadas, ejerciciosRealizados, entrenamientoRealizado) {
        if (ejerciciosRealizados != null && seriesRealizadas != null && entrenamientoRealizado != null) {
            val ejerciciosRealizadosLocal = ejerciciosRealizados ?: emptyList()
            val seriesRealizadasLocal = seriesRealizadas ?: emptyList()

            // Mapa para resultados finales
            val nuevoMapa = mutableMapOf<String, List<SerieRealizada>>()

            // Obtener la lista de IDs de ejercicios realizados para este entrenamiento
            val idsEjerciciosRealizados = ejerciciosRealizadosLocal.map { it._id }

            // Solo asignar series que correspondan exactamente a los ejercicios de este entrenamiento
            ejerciciosRealizadosLocal.forEach { ejercicioRealizado ->
                // Filtrar solo series que coinciden EXACTAMENTE con este ejercicio realizado
                val seriesPorEjercicioRealizado = seriesRealizadasLocal.filter {
                    it.ejercicioRealizado == ejercicioRealizado._id
                }

                if (seriesPorEjercicioRealizado.isNotEmpty()) {
                    nuevoMapa[ejercicioRealizado._id] = seriesPorEjercicioRealizado
                } else {
                    // Si no hay coincidencia exacta, ver si hay series para este ejercicio original
                    val seriesPorEjercicioOriginal = seriesRealizadasLocal.filter {
                        it.ejercicio == ejercicioRealizado.ejercicio &&
                                idsEjerciciosRealizados.contains(it.ejercicioRealizado)
                    }

                    if (seriesPorEjercicioOriginal.isNotEmpty()) {
                        nuevoMapa[ejercicioRealizado._id] = seriesPorEjercicioOriginal
                    }
                }
            }

            // Verificación final
            nuevoMapa.forEach { (id, series) ->
                val ejercicio = ejerciciosRealizadosLocal.find { it._id == id }?.nombre ?: "Desconocido"
            }

            // Actualizar estado
            seriesPorEjercicioState.value = nuevoMapa
        }
    }

    // Contenido principal
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
            if (isLoading || entrenamientoRealizado == null || datosCargando) {
                // Pantalla de carga
                LoadingScreen()
            } else {
                val ejerciciosRealizadosLocal = ejerciciosRealizados ?: emptyList()
                val ejerciciosLocal = ejercicios ?: emptyList()

                // Contenido cuando está cargado
                ContenidoDetallesRealizarEntrenamiento(
                    navController = navController,
                    entrenamientoRealizado = entrenamientoRealizado!!,
                    entrenamiento = entrenamiento,
                    ejerciciosRealizados = ejerciciosRealizadosLocal,
                    seriesPorEjercicio = seriesPorEjercicio,
                    ejercicios = ejerciciosLocal,
                    snackbarHostState = snackbarHostState
                )
            }
        }

        // SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { snackbarData ->
            Snackbar(
                containerColor = darkPurple,
                contentColor = Color.White,
                snackbarData = snackbarData
            )
        }
    }
}

// El resto de funciones Composable quedan igual que en tu código original

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
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cargando entrenamiento...",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun ContenidoDetallesRealizarEntrenamiento(
    navController: NavController,
    entrenamientoRealizado: EntrenamientoRealizado,
    entrenamiento: Entrenamientos?,
    ejerciciosRealizados: List<EjercicioRealizado>,
    seriesPorEjercicio: Map<String, List<SerieRealizada>>,
    ejercicios: List<Ejercicios>,
    snackbarHostState: SnackbarHostState
) {
    val scrollState = rememberScrollState()
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val coroutineScope = rememberCoroutineScope()

    // Variable para controlar si se puede mostrar la imagen de fondo
    val mostrarImagenFondo = remember { mutableStateOf(false) }
    // Variable para almacenar el bitmap de la imagen de fondo
    val imagenFondoBitmap = remember { mutableStateOf<Any?>(null) }

    // Lógica para cargar la imagen de fondo de manera segura
    LaunchedEffect(entrenamiento) {
        entrenamiento?.foto?.let { foto ->
            if (foto.isNotEmpty()) {
                try {
                    val bitmap = base64ToImageBitmap(foto)
                    if (bitmap != null) {
                        imagenFondoBitmap.value = bitmap
                        mostrarImagenFondo.value = true
                    }
                } catch (e: Exception) {
                    Log.e("DetallesEntrenamiento", "Error al cargar imagen: ${e.message}")
                    mostrarImagenFondo.value = false
                }
            }
        }
    }

    // Calcular el total de series en todos los ejercicios
    val totalSeries = seriesPorEjercicio.values.sumOf { it.size }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con efecto blur si hay imagen de entrenamiento
        if (mostrarImagenFondo.value && imagenFondoBitmap.value != null) {
            Image(
                bitmap = imagenFondoBitmap.value as androidx.compose.ui.graphics.ImageBitmap,
                contentDescription = "Imagen de fondo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(15.dp)
                    .alpha(0.15f) // Imagen de fondo más sutil
            )

            // Overlay para mejorar legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to backgroundDark.copy(alpha = 0.9f),
                            0.4f to backgroundDark.copy(alpha = 0.85f),
                            1f to backgroundDark.copy(alpha = 0.95f)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp) // Espacio para el botón flotante
        ) {
            // Encabezado con imagen
            CabeceraEntrenamiento(
                entrenamiento = entrenamiento,
                entrenamientoRealizado = entrenamientoRealizado,
                formatoFecha = formatoFecha,
                navController = navController,
                snackbarHostState = snackbarHostState
            )

            // Sección de resumen
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(600, delayMillis = 300)
                )
            ) {
                TarjetaResumen(
                    ejerciciosRealizados = ejerciciosRealizados,
                    totalSeries = totalSeries,
                    entrenamientoRealizado = entrenamientoRealizado
                )
            }

            // Título de sección ejercicios con animación
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(600, delayMillis = 600)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ejercicios realizados",
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
            }

            // Lista de ejercicios realizados con animación
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, delayMillis = 900)
                )
            ) {
                if (ejerciciosRealizados.isEmpty()) {
                    EmptyExerciseMessage()
                } else {
                    ListaEjercicios(
                        ejerciciosRealizados = ejerciciosRealizados,
                        ejercicios = ejercicios,
                        seriesPorEjercicio = seriesPorEjercicio
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Función para repetir entrenamiento")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 50.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = darkPurple
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Repetir entrenamiento",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "REPETIR ESTE ENTRENAMIENTO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CabeceraEntrenamiento(
    entrenamiento: Entrenamientos?,
    entrenamientoRealizado: EntrenamientoRealizado,
    formatoFecha: SimpleDateFormat,
    navController: NavController,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    // Variable para la imagen del encabezado
    val imagenEncabezado = remember { mutableStateOf<Any?>(null) }
    val mostrarImagenEncabezado = remember { mutableStateOf(false) }

    // Cargar imagen de encabezado de manera segura
    LaunchedEffect(entrenamiento) {
        entrenamiento?.foto?.let { foto ->
            if (foto.isNotEmpty()) {
                try {
                    val bitmap = base64ToImageBitmap(foto)
                    if (bitmap != null) {
                        imagenEncabezado.value = bitmap
                        mostrarImagenEncabezado.value = true
                    }
                } catch (e: Exception) {
                    Log.e("DetallesEntrenamiento", "Error al cargar imagen encabezado: ${e.message}")
                    mostrarImagenEncabezado.value = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp) // Un poco más alto para mejor visualización
    ) {
        if (mostrarImagenEncabezado.value && imagenEncabezado.value != null) {
            // Imagen del entrenamiento
            Image(
                bitmap = imagenEncabezado.value as androidx.compose.ui.graphics.ImageBitmap,
                contentDescription = "Imagen de entrenamiento",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
        } else {
            // Si no hay imagen o hubo error, mostrar un fondo degradado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                darkPurple,
                                backgroundDark
                            )
                        )
                    )
            )
        }

        // Degradado para legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        0f to backgroundDark.copy(alpha = 0.8f),
                        0.5f to backgroundDark.copy(alpha = 0.5f),
                        1f to backgroundDark.copy(alpha = 0.8f)
                    )
                )
        )

        // Información del entrenamiento sobre la imagen
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 10.dp)
        ) {
            // Fecha del entrenamiento
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Fecha",
                    tint = primaryPurple,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = formatoFecha.format(entrenamientoRealizado.fecha),
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Duración
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Duración",
                    tint = primaryPurple,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Duración: ${entrenamientoRealizado.duracion}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre del entrenamiento
            Text(
                text = entrenamiento?.nombre ?: "Entrenamiento",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )

            // Categoría
            entrenamiento?.categoria?.let { categoria ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = darkPurple.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Categoría",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = categoria,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Botón de regreso
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp, 35.dp, 16.dp, 15.dp)
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
}

@Composable
fun TarjetaResumen(
    ejerciciosRealizados: List<EjercicioRealizado>,
    totalSeries: Int,
    entrenamientoRealizado: EntrenamientoRealizado
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .shadow(
                elevation = 8.dp,
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
                .padding(20.dp)
        ) {
            // Título de la sección
            Text(
                text = "Resumen del entrenamiento",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Divider(
                color = darkGray,
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Métricas de resumen en row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Ejercicios completados
                ResumenItem(
                    icono = Icons.Default.FitnessCenter,
                    valor = "${ejerciciosRealizados.size}",
                    descripcion = "Ejercicios",
                    modifier = Modifier.weight(1f)
                )

                // Series totales
                ResumenItem(
                    icono = Icons.Default.Star,
                    valor = "$totalSeries",
                    descripcion = "Series",
                    modifier = Modifier.weight(1f)
                )

                // Duración
                val duracionParts = entrenamientoRealizado.duracion.split(":")
                val duracionTexto = if (duracionParts.size >= 2) {
                    val horas = duracionParts[0].toIntOrNull() ?: 0
                    val minutos = duracionParts[1].toIntOrNull() ?: 0
                    if (horas > 0) "${horas}h ${minutos}m" else "${minutos}m"
                } else {
                    entrenamientoRealizado.duracion
                }

                ResumenItem(
                    icono = Icons.Default.AccessTime,
                    valor = duracionTexto,
                    descripcion = "Duración",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyExerciseMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = surfaceDark,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = primaryPurple.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No hay ejercicios registrados",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Este entrenamiento no contiene ejercicios",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ListaEjercicios(
    ejerciciosRealizados: List<EjercicioRealizado>,
    ejercicios: List<Ejercicios>,
    seriesPorEjercicio: Map<String, List<SerieRealizada>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ejerciciosRealizados.forEach { ejercicioRealizado ->
            // Obtener series para este ejercicio usando el mapa
            val seriesDelEjercicio = seriesPorEjercicio[ejercicioRealizado._id] ?: emptyList()

            // Encontrar el ejercicio original por ID desde la lista
            val ejercicioOriginal = ejercicios.find {
                it._id == ejercicioRealizado.ejercicio
            }

            // Mostrar información de depuración en el log
            Log.d("ListaEjercicios", "Ejercicio: ${ejercicioRealizado.nombre}, ID: ${ejercicioRealizado._id}, Series: ${seriesDelEjercicio.size}")

            EjercicioRealizadoCard(
                ejercicioRealizado = ejercicioRealizado,
                ejercicioOriginal = ejercicioOriginal,
                series = seriesDelEjercicio
            )
        }
    }
}

@Composable
fun ResumenItem(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(darkPurple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                tint = primaryPurple,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = valor,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Text(
            text = descripcion,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Gray
            )
        )
    }
}

@Composable
fun EjercicioRealizadoCard(
    ejercicioRealizado: EjercicioRealizado,
    ejercicioOriginal: Ejercicios?,
    series: List<SerieRealizada>
) {
    var expanded by remember { mutableStateOf(false) }
    // Variable para la imagen del ejercicio
    val imagenEjercicio = remember { mutableStateOf<Any?>(null) }
    val mostrarImagenEjercicio = remember { mutableStateOf(false) }

    // Cargar imagen del ejercicio de manera segura
    LaunchedEffect(ejercicioOriginal) {
        ejercicioOriginal?.foto?.let { foto ->
            if (foto.isNotEmpty()) {
                try {
                    val bitmap = base64ToImageBitmap(foto)
                    if (bitmap != null) {
                        imagenEjercicio.value = bitmap
                        mostrarImagenEjercicio.value = true
                    }
                } catch (e: Exception) {
                    Log.e("EjercicioCard", "Error al cargar imagen: ${e.message}")
                    mostrarImagenEjercicio.value = false
                }
            }
        }
    }

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
            modifier = Modifier.fillMaxWidth()
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
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = if (!expanded) 16.dp else 0.dp))
                ) {
                    // Mostrar imagen cargada o imagen por defecto
                    if (mostrarImagenEjercicio.value && imagenEjercicio.value != null) {
                        Image(
                            bitmap = imagenEjercicio.value as androidx.compose.ui.graphics.ImageBitmap,
                            contentDescription = "Imagen de ejercicio",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Imagen por defecto
                        Image(
                            painter = painterResource(id = R.drawable.logo),
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
                        text = ejercicioRealizado.nombre,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grupo muscular con icono si está disponible
                    ejercicioOriginal?.musculo?.let { musculo ->
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
                                text = musculo,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = primaryPurple
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Número de series realizadas
                    if (series.isNotEmpty()) {
                        Text(
                            text = "${series.size} ${if (series.size == 1) "serie" else "series"} realizadas",
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
                    // Lista de series realizadas
                    if (series.isEmpty()) {
                        Text(
                            text = "No se registraron series para este ejercicio",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else {
                        Text(
                            text = "Series realizadas",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Encabezado de la tabla
                        TablaSeries(series)
                    }
                }
            }

            // Si no está expandido, mostrar indicador visual de serie
            if (!expanded && series.isNotEmpty()) {
                IndicadorSeries(series.size)
            }
        }
    }
}

@Composable
fun TablaSeries(series: List<SerieRealizada>) {
    // Encabezado de la tabla
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            )
        }

        Text(
            text = "Peso",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Reps",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )
    }

    // Divider
    Divider(
        color = darkGray,
        thickness = 1.dp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Series ordenadas por número
    val seriesOrdenadas = series.sortedBy {
        try {
            it.numeroSerie.toString().toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    seriesOrdenadas.forEachIndexed { index, serie ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .background(
                    if (index % 2 == 1) darkGray.copy(alpha = 0.3f) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número de serie con círculo
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
                    text = "${serie.numeroSerie}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryPurple
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Peso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${serie.peso} kg",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Repeticiones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${serie.repeticiones}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        // Divider entre series (excepto la última)
        if (serie != seriesOrdenadas.last()) {
            Divider(
                color = darkGray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
    }
}

@Composable
fun IndicadorSeries(numSeries: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Puntos indicando series registradas
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(minOf(numSeries, 5)) {
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
            if (numSeries > 5) {
                Text(
                    text = "+${numSeries - 5}",
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


