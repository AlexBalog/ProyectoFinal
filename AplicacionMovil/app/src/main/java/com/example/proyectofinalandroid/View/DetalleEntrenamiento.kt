package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String
) {
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel()
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(parentEntry)

    val usuario by usuariosViewModel.usuario.collectAsState()

    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(usuario!!)
            ejerciciosViewModel.setUsuario(usuario!!)
        }
    }

    var isAnimatedIn by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Mapa para almacenar los ejercicios cargados
    val ejerciciosCargados = remember { mutableStateMapOf<String, Ejercicios>() }

    // Observar el estado del entrenamiento seleccionado
    val entrenamientoSeleccionado by entrenamientosViewModel.entrenamientoSeleccionado.collectAsState()
    val isLoading by entrenamientosViewModel.isLoading.collectAsState()

    // Cargar el entrenamiento por ID cuando se inicia la pantalla
    LaunchedEffect(entrenamientoId) {
        Log.d("FalloDetalle", entrenamientoId)
        entrenamientosViewModel.getOne(entrenamientoId)
        delay(100)
        isAnimatedIn = true
    }

    // Cargar los ejercicios cuando cambia el entrenamiento seleccionado
    LaunchedEffect(entrenamientoSeleccionado) {
        entrenamientoSeleccionado?.ejercicios?.forEach { ejercicioId ->
            coroutineScope.launch {
                // Verificar si ya se ha cargado este ejercicio
                if (!ejerciciosCargados.containsKey(ejercicioId)) {
                    ejerciciosViewModel.getOne(ejercicioId)

                    // Esperar a que se cargue el ejercicio y almacenarlo en el mapa
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        AnimatedVisibility(
            visible = isAnimatedIn,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFAB47BC),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                } else {
                    entrenamientoSeleccionado?.let { entrenamiento ->
                        // Cabecera con imagen y detalles principales
                        item {
                            HeaderEntrenamiento(entrenamiento, navController)
                        }

                        // Detalles del entrenamiento
                        item {
                            DetallesEntrenamiento(entrenamiento)
                        }

                        // Título para la sección de ejercicios
                        item {
                            Text(
                                text = "Ejercicios",
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }

                        // Lista de ejercicios
                        items(entrenamiento.ejercicios) { ejercicioId ->
                            val ejercicio = ejerciciosCargados[ejercicioId]
                            if (ejercicio != null) {
                                TarjetaEjercicio(ejercicio, navController)
                            } else {
                                TarjetaEjercicioCargando(ejercicioId)
                            }
                        }

                        // Botón para comenzar el entrenamiento
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    navController.navigate("comenzar_entrenamiento/${entrenamiento._id}")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7B1FA2)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Comenzar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "COMENZAR ENTRENAMIENTO",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderEntrenamiento(entrenamiento: Entrenamientos, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Imagen de fondo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = entrenamiento.foto)
                            .error(R.drawable.logo)
                            .build()
                    ),
                    contentDescription = "Imagen de entrenamiento",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradiente oscuro para mejor legibilidad del texto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0D0D0D).copy(alpha = 0.7f),
                                    Color(0xFF0D0D0D)
                                )
                            )
                        )
                )

                // Botón de volver
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(48.dp)
                        .background(
                            color = Color(0xFF1A1A1A).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                // Título del entrenamiento
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = entrenamiento.nombre,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                blurRadius = 6f
                            )
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Categoría",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entrenamiento.categoria,
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Color.LightGray
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetallesEntrenamiento(entrenamiento: Entrenamientos) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .shadow(
                elevation = 8.dp,
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
                .padding(16.dp)
        ) {
            // Duración
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Duración",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Duración: ${entrenamiento.duracion} minutos",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }

            // Músculos trabajados
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Músculos",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Músculos trabajados:",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        entrenamiento.musculos.forEachIndexed { index, musculo ->
                            if (index > 0) {
                                Text(
                                    text = " • ",
                                    color = Color(0xFFAB47BC),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = musculo,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.LightGray
                                )
                            )
                        }
                    }
                }
            }

            // Likes
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Likes",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${entrenamiento.likes} likes",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun TarjetaEjercicio(ejercicio: Ejercicios, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2).copy(alpha = 0.5f),
                spotColor = Color(0xFF7B1FA2).copy(alpha = 0.5f)
            )
            .clickable {
                navController.navigate("detalle_ejercicio/${ejercicio._id}")
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            // Imagen del ejercicio
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(96.dp)
                    .background(Color(0xFF252525))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = ejercicio.foto)
                            .error(R.drawable.logo)
                            .build()
                    ),
                    contentDescription = "Imagen de ejercicio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Información del ejercicio
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ejercicio.nombre,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Músculo: ${ejercicio.musculo}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFAB47BC)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = ejercicio.descripcion,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.LightGray
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Icono de flecha
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TarjetaEjercicioCargando(ejercicioId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            // Placeholder para imagen
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(96.dp)
                    .background(Color(0xFF252525)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFAB47BC),
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }

            // Placeholder para información
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Cargando ejercicio...",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFFAB47BC),
                    trackColor = Color(0xFF252525)
                )
            }
        }
    }
}

@Composable
fun DetalleEjercicioScreen(
    navController: NavController,
    ejercicioId: String
) {
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()
    var isAnimatedIn by remember { mutableStateOf(false) }

    // Observar el estado del ejercicio seleccionado
    val ejercicioSeleccionado by ejerciciosViewModel.ejercicioSeleccionado.collectAsState()
    val isLoading by ejerciciosViewModel.isLoading.collectAsState()

    // Cargar el ejercicio por ID cuando se inicia la pantalla
    LaunchedEffect(ejercicioId) {
        ejerciciosViewModel.getOne(ejercicioId)
        delay(100)
        isAnimatedIn = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
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
                    .padding(16.dp)
                    .systemBarsPadding()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFAB47BC),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                } else {
                    ejercicioSeleccionado?.let { ejercicio ->
                        // Cabecera con imagen y botón de volver
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        ) {
                            // Imagen del ejercicio
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = ejercicio.foto)
                                        .error(R.drawable.logo)
                                        .build()
                                ),
                                contentDescription = "Imagen del ejercicio",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )

                            // Gradiente oscuro para mejor legibilidad
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF0D0D0D).copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )

                            // Botón de volver
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF1A1A1A).copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }

                            // Nombre del ejercicio
                            Text(
                                text = ejercicio.nombre,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = Color.White,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        blurRadius = 6f
                                    )
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tarjeta con información del ejercicio
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
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
                                    .padding(16.dp)
                            ) {
                                // Músculo
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "Músculo",
                                        tint = Color(0xFFAB47BC),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Músculo: ${ejercicio.musculo}",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    )
                                }

                                Divider(
                                    color = Color(0xFF252525),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // Descripción
                                Text(
                                    text = "Descripción",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFFAB47BC)
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = ejercicio.descripcion,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Esta vista sería a la que navegaríamos al pulsar el botón "Comenzar entrenamiento"
@Composable
fun ComenzarEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String
) {
    // Esta función se implementaría con la lógica para comenzar el entrenamiento
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla de ejecución del entrenamiento\nEn desarrollo",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }
}