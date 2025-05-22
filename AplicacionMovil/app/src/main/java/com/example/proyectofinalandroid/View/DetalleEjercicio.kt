package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.base64ToBitmap
import kotlinx.coroutines.delay

private val backgroundDark = Color(0xFF0D0D0D)

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEjercicioScreen(
    navController: NavController,
    ejercicioId: String
) {
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()
    var isAnimatedIn by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(parentEntry)

    val usuario by usuariosViewModel.usuario.collectAsState()

    LaunchedEffect(usuario) {
        usuario?.let {
            ejerciciosViewModel.setUsuario(it)
        }
    }

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
            .background(Color.Black)
    ) {
        AnimatedVisibility(
            visible = isAnimatedIn,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Contenido principal
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
                        // Imagen de fondo (header)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(base64ToBitmap(ejercicio.foto))
                                        .error(R.drawable.logo)
                                        .build()
                                ),
                                contentDescription = "Imagen del ejercicio",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Gradiente para mejorar legibilidad
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to backgroundDark.copy(alpha = 0.9f),
                                            0.5f to backgroundDark.copy(alpha = 0.7f),
                                            1f to backgroundDark.copy(alpha = 0.9f)
                                        )
                                    )
                            )

                            // Título de la aplicación
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
                                    .padding(top = 35.dp)
                                    .align(Alignment.TopCenter)
                            )
                        }

                        // Contenido desplazable
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            // Espacio para la imagen de fondo
                            Spacer(modifier = Modifier.height(260.dp))

                            // Tarjeta con la información del ejercicio
                            EjercicioInfoCard(ejercicio)
                        }

                        // Botón de regreso (separado de la imagen)
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .padding(16.dp, 30.dp, 16.dp, 16.dp)
                                .size(48.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape
                                )
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
fun EjercicioInfoCard(ejercicio: Ejercicios) {
    val context = LocalContext.current
    var showYouTubeDialog by remember { mutableStateOf(false) }

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
            // Nombre del ejercicio
            Text(
                text = ejercicio.nombre,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tarjeta de músculo
            MuscleCard(ejercicio.musculo)

            Spacer(modifier = Modifier.height(20.dp))

            // Instrucciones y descripción
            Text(
                text = "Instrucciones",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
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
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Consejos adicionales
            TipsSection(ejercicio.consejos)

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para ver ejecución
            Button(
                onClick = {
                    if (ejercicio.tutorial.isNotBlank()) {
                        showYouTubeDialog = true
                    } else {
                        openYouTubeSearchFallback(context, ejercicio.nombre)
                    }
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
                    contentDescription = "Ver ejecución",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VER EJECUCIÓN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // DIÁLOGO DE YOUTUBE
    if (showYouTubeDialog) {
        YouTubeConfirmationDialog(
            ejercicioNombre = ejercicio.nombre,
            onConfirm = {
                showYouTubeDialog = false
                openSpecificYouTubeVideo(context, ejercicio.tutorial)
            },
            onDismiss = {
                showYouTubeDialog = false
            }
        )
    }
}

@Composable
fun MuscleCard(musculo: String) {
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
                    contentDescription = "Músculo",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Músculo Principal",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                )

                Text(
                    text = musculo,
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
fun TipsSection(consejos: List<String>) {
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
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Consejos",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Consejos para mejorar",
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
                modifier = Modifier.padding(vertical = 8.dp)
            )

            for (consejo in consejos) {
                TipItem(
                    icon = Icons.Default.Check,
                    text = consejo
                )
            }
        }
    }
}

@Composable
fun TipItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Green,
            modifier = Modifier
                .padding(top = 3.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.LightGray,
                lineHeight = 20.sp
            )
        )
    }
}

@Composable
fun YouTubeConfirmationDialog(
    ejercicioNombre: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ver en YouTube",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "¿Seguro que quieres salir de la aplicación?",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Se abrirá YouTube para ver el vídeo de: $ejercicioNombre",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B1FA2)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver vídeo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.LightGray
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Gray, Color.Gray)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Cancelar",
                    color = Color.LightGray
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

fun openSpecificYouTubeVideo(context: Context, videoUrl: String) {
    try {
        if (videoUrl.isBlank()) {
            return
        }

        // Extraer el ID del video de diferentes formatos de URL de YouTube
        val videoId = extractYouTubeVideoId(videoUrl)

        if (videoId != null) {
            // Intentar abrir en la app de YouTube primero
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            appIntent.setPackage("com.google.android.youtube")

            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
            } else {
                // Si no tiene la app, abrir en navegador con URL completa
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                context.startActivity(webIntent)
            }
        } else {
            // Si no se puede extraer el ID, abrir URL directamente
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            context.startActivity(webIntent)
        }

    } catch (e: Exception) {
        // Fallback: abrir en navegador
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            // Si todo falla, hacer búsqueda genérica
            openYouTubeSearchFallback(context, "ejercicio")
        }
    }
}

fun extractYouTubeVideoId(url: String): String? {
    return try {
        when {
            // Formato: https://www.youtube.com/watch?v=VIDEO_ID
            url.contains("youtube.com/watch?v=") -> {
                url.substringAfter("watch?v=").substringBefore("&")
            }
            // Formato: https://youtu.be/VIDEO_ID
            url.contains("youtu.be/") -> {
                url.substringAfter("youtu.be/").substringBefore("?")
            }
            // Formato: https://www.youtube.com/embed/VIDEO_ID
            url.contains("youtube.com/embed/") -> {
                url.substringAfter("embed/").substringBefore("?")
            }
            // Formato: https://m.youtube.com/watch?v=VIDEO_ID
            url.contains("m.youtube.com/watch?v=") -> {
                url.substringAfter("watch?v=").substringBefore("&")
            }
            // Si ya es solo el ID del video (11 caracteres)
            url.length == 11 && url.matches(Regex("[a-zA-Z0-9_-]+")) -> {
                url
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

fun openYouTubeSearchFallback(context: Context, ejercicioNombre: String) {
    try {
        val searchQuery = "como hacer $ejercicioNombre ejercicio"
        val searchUrl = "https://www.youtube.com/results?search_query=${searchQuery.replace(" ", "+")}"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
        intent.setPackage("com.google.android.youtube")

        if (intent.resolveActivity(context.packageManager) == null) {
            intent.setPackage(null)
        }

        context.startActivity(intent)

    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        context.startActivity(webIntent)
    }
}