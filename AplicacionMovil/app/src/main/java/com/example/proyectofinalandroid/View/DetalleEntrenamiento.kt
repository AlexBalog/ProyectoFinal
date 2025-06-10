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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Guardados
import com.example.proyectofinalandroid.Model.Likes
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.GuardadosViewModel
import com.example.proyectofinalandroid.ViewModel.LikesViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.base64ToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEntrenamientoScreen(
    navController: NavController,
    entrenamientoId: String
) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel(parentEntry)
    val likesViewModel: LikesViewModel = hiltViewModel()
    val guardadosViewModel: GuardadosViewModel = hiltViewModel()

    val usuario by usuariosViewModel.usuario.collectAsState()
    val scrollState = rememberScrollState()

    // Estados consolidados
    val entrenamientoSeleccionado by entrenamientosViewModel.entrenamientoSeleccionado.collectAsState()
    val isLoading by entrenamientosViewModel.isLoading.collectAsState()
    val ejerciciosCargados = remember { mutableStateMapOf<String, Ejercicios>() }
    val isLiked by likesViewModel.isLiked.collectAsState()
    val isSaved by guardadosViewModel.isSaved.collectAsState()
    val contadorLikes by likesViewModel.likesCount.collectAsState() // Usar solo el contador del LikesViewModel

    var isAnimatedIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // INICIALIZACIÓN - Solo se ejecuta una vez al cargar la pantalla
    LaunchedEffect(entrenamientoId, usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(it)
            ejerciciosViewModel.setUsuario(it)
            entrenamientosViewModel.getOne(entrenamientoId)
        }
        delay(100)
        isAnimatedIn = true
    }

    // CONFIGURACIÓN DE LIKES - Solo cuando cambia el entrenamiento o usuario
    LaunchedEffect(usuario, entrenamientoSeleccionado) {
        // Capturar valores primero
        val usuarioActual = usuario
        val entrenamientoActual = entrenamientoSeleccionado

        if (usuarioActual != null && entrenamientoActual != null && entrenamientoActual._id != null) {
            likesViewModel.setUsuarioYEntrenamiento(usuarioActual, entrenamientoActual)
            likesViewModel.cargarContadorLikes(entrenamientoActual._id)
            guardadosViewModel.setUsuarioYEntrenamiento(usuarioActual, entrenamientoActual)
        }
    }

    // CARGA DE EJERCICIOS
    LaunchedEffect(entrenamientoSeleccionado) {
        entrenamientoSeleccionado?.let { entrenamiento ->
            ejerciciosViewModel.getListaEjerciciosDesdeIds(entrenamiento.ejercicios)

            // Cargar ejercicios individuales si es necesario
            entrenamiento.ejercicios.forEach { ejercicioId ->
                if (!ejerciciosCargados.containsKey(ejercicioId)) {
                    coroutineScope.launch {
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
            Box(modifier = Modifier.fillMaxSize()) {
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
                    entrenamientoSeleccionado?.let { entrenamiento ->
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Barra superior
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(105.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFF0D0D0D),
                                                Color(0xFF0D0D0D).copy(alpha = 0.0f)
                                            )
                                        )
                                    )
                            ) {
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
                                                Color(0xFFAB47BC),
                                                Color(0xFF7B1FA2)
                                            )
                                        )
                                    ),
                                    modifier = Modifier
                                        .padding(top = 33.dp)
                                        .align(Alignment.TopCenter)
                                )
                            }

                            // Contenido principal
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                // Imagen de fondo
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .offset(y = (-30).dp)
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            ImageRequest.Builder(LocalContext.current)
                                                .data(base64ToBitmap(entrenamiento.foto))
                                                .error(R.drawable.logo)
                                                .build()
                                        ),
                                        contentDescription = "Imagen del entrenamiento",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Gradientes
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colorStops = arrayOf(
                                                            0.0f to Color(0xFF0D0D0D),
                                                            0.1f to Color(0xFF0D0D0D).copy(alpha = 0.6f),
                                                            0.2f to Color(0xFF0D0D0D).copy(alpha = 0.0f)
                                                        )
                                                    )
                                                )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colorStops = arrayOf(
                                                            0.0f to Color(0xFF0D0D0D).copy(alpha = 0.0f),
                                                            0.5f to Color(0xFF0D0D0D).copy(alpha = 0.6f),
                                                            1.0f to Color(0xFF0D0D0D)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }

                                // Contenido desplazable
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    Spacer(modifier = Modifier.height(230.dp))

                                    // Tarjeta con información
                                    EntrenamientoInfoCard(
                                        entrenamiento = entrenamiento,
                                        ejerciciosCargados = ejerciciosCargados,
                                        navController = navController,
                                        isLiked = isLiked,
                                        isSaved = isSaved,
                                        contadorLikes = contadorLikes,
                                        onLikeToggle = {
                                            // Lógica simplificada de toggle
                                            coroutineScope.launch {
                                                likesViewModel.toggleLike(entrenamiento._id, entrenamientosViewModel)
                                            }
                                        },
                                        onSaveToggle = {
                                            coroutineScope.launch {
                                                if (isSaved) {
                                                    guardadosViewModel.delete(
                                                        mapOf(
                                                            "usuario" to (usuario?._id ?: ""),
                                                            "entrenamiento" to entrenamiento._id
                                                        )
                                                    )
                                                } else {
                                                    guardadosViewModel.new(
                                                        Guardados(
                                                            usuario = usuario?._id ?: "",
                                                            entrenamiento = entrenamiento._id
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Botón de regreso
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp, 25.dp, 16.dp, 16.dp)
                                .size(48.dp)
                                .shadow(elevation = 4.dp, shape = CircleShape)
                                .background(
                                    color = Color(0xFF1A1A1A).copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EntrenamientoInfoCard(
    entrenamiento: Entrenamientos,
    ejerciciosCargados: Map<String, Ejercicios>,
    navController: NavController,
    isLiked: Boolean,
    isSaved: Boolean,
    onLikeToggle: () -> Unit,
    onSaveToggle: () -> Unit,
    contadorLikes: Int

) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-40).dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nombre del entrenamiento
                Text(
                    text = entrenamiento.nombre,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onLikeToggle,
                    modifier = Modifier.padding(bottom = 15.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isLiked) "Quitar like" else "Dar like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }

                IconButton(
                    onClick = onSaveToggle,
                    modifier = Modifier.padding(bottom = 15.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isSaved) "Quitar guardado" else "Guardar",
                        tint = if (isSaved) Color(0xFFDAA520) else Color.Gray
                    )
                }
            }

            // Tarjeta de categoría
            CategoryCard(entrenamiento.categoria)

            Spacer(modifier = Modifier.height(20.dp))

            // Detalles del entrenamiento
            EntrenamientoDetalles(entrenamiento, contadorLikes)

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de ejercicios
            Text(
                text = "Ejercicios",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFAB47BC)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            entrenamiento.ejercicios.forEach { ejercicioId ->
                val ejercicio = ejerciciosCargados[ejercicioId]
                if (ejercicio != null) {
                    TarjetaEjercicio(ejercicio, navController)
                } else {
                    TarjetaEjercicioCargando(ejercicioId)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para comenzar el entrenamiento
            Button(
                onClick = { navController.navigate("realizarEntrenamiento/${entrenamiento._id}") },
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CategoryCard(categoria: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF252525)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF7B1FA2).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Categoría",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Categoría",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                )

                Text(
                    text = categoria,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun EntrenamientoDetalles(entrenamiento: Entrenamientos, contadorLikes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF252525)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detalles",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalles del entrenamiento",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }

            Divider(
                color = Color(0xFF333333),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Músculos trabajados - Cambiado a lista vertical normal en lugar de FlowRow
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Músculos",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Músculos trabajados:",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color.LightGray,
                            lineHeight = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Lista normal de músculos con chips
                    Column(modifier = Modifier.fillMaxWidth()) {
                        entrenamiento.musculo.forEach { musculo ->
                            Card(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF7B1FA2).copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = musculo,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Duración
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Duración",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Duración: ${entrenamiento.duracion} minutos",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.LightGray,
                        lineHeight = 20.sp
                    )
                )
            }

            // Likes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Likes",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${contadorLikes} likes",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.LightGray,
                        lineHeight = 20.sp
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
            .padding(vertical = 4.dp) // Añadido padding para dar espacio a la sombra
            .shadow(
                elevation = 8.dp, // Aumentado para mayor visibilidad
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2),
                // No especificamos offset para que sea uniforme en todas direcciones
            )
            .clickable {
                navController.navigate("detalleEjercicio/${ejercicio._id}")
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del ejercicio
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .height(86.dp)
                    .background(Color(0xFF252525))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(base64ToBitmap(ejercicio.foto))
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
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ejercicio.musculo,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFFAB47BC)
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
            .padding(vertical = 4.dp) // Añadido padding para dar espacio a la sombra
            .shadow(
                elevation = 8.dp, // Aumentado para mayor visibilidad
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
                // No especificamos offset para que sea uniforme en todas direcciones
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
        ) {
            // Placeholder para imagen
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .height(86.dp)
                    .background(Color(0xFF252525)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }

            // Placeholder para información
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Cargando ejercicio...",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

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