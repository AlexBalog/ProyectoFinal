package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientoRealizadoViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.roundToInt
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.Dialog
import com.example.proyectofinalandroid.ViewModel.MedicionesViewModel
import com.example.proyectofinalandroid.utils.getImageBitmapSafely
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import com.example.proyectofinalandroid.Model.TipoMedicion
import com.example.proyectofinalandroid.Model.Mediciones
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface



@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    // Obtener ViewModels
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val entrenamientoRealizadoViewModel: EntrenamientoRealizadoViewModel = hiltViewModel()
    val medicionesViewModel: MedicionesViewModel = hiltViewModel()

    // Estados
    var isAnimatedIn by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val usuario by usuariosViewModel.usuario.collectAsState()

    // Lista de entrenamientos realizados
    val entrenamientosRealizados by entrenamientoRealizadoViewModel.entrenamientoRealizado.collectAsState(initial = emptyList())

    // Estadísticas
    val totalEntrenamientos = entrenamientosRealizados?.size ?: 0
    val horasTotales = entrenamientosRealizados!!.sumOf {
        val duracion = it.duracion
        // Formato "HH:mm:ss"
        val parts = duracion.split(":")
        val hours = parts[0].toInt()
        val minutes = if (parts.size > 1) parts[1].toInt() else 0
        val seconds = if (parts.size > 2) parts[2].toInt() else 0
        hours + (minutes / 60.0) + (seconds / 3600.0)
    }.roundToInt()

    // Extraer objetivos del usuario para visualización
    val objetivoPeso = usuario?.objetivoPeso ?: 0f
    val pesoActual = usuario?.peso ?: 0f
    val objetivoTiempo = usuario?.objetivoTiempo ?: 0f // en semanas

    // Calcular progreso (simulación - a implementar con datos reales)
    val progresoObjetivo = if (objetivoPeso > 0f && objetivoTiempo > 0f) {
        val tiempoTranscurrido = 2f // Semanas transcurridas (simular - implementar con datos reales)
        val progresoPeso = if (objetivoPeso > pesoActual) {
            (objetivoPeso - pesoActual) / (objetivoPeso - pesoActual) * (tiempoTranscurrido / objetivoTiempo)
        } else {
            (pesoActual - objetivoPeso) / (pesoActual - objetivoPeso) * (tiempoTranscurrido / objetivoTiempo)
        }
        (progresoPeso * 100).coerceIn(0f, 100f)
    } else {
        0f
    }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        // Aquí debes implementar la carga de datos del perfil y entrenamientos realizados
        entrenamientoRealizadoViewModel.getEntrenamientosRealizadosByUsuario(usuario!! as Usuarios)
        medicionesViewModel.setUsuario(usuario!!)
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
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                actions = {
                    var showDialog by remember { mutableStateOf(false) }

                    IconButton(onClick = {
                        if (usuario!!.formulario == true) {
                            navController.navigate("configuracion")
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                    if (showDialog) {
                        FormularioRequiredDialog(
                            onDismiss = { showDialog = false },
                            onGoToForm = {
                                showDialog = false
                                navController.navigate("formulario")
                            },
                            onLogout = {
                                showDialog = false
                                // Cerrar sesión
                                usuariosViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("root") { inclusive = true }
                                }
                            }
                        )
                    }
                }
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
                    // Perfil de usuario
                    UserProfileSection(usuario = usuario)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones de acción
                    ActionButtonsRow(navController = navController, medicionesViewModel = medicionesViewModel)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Historial de entrenamientos
                    TrainingHistorySection(
                        entrenamientosRealizados = entrenamientosRealizados!!.sortedByDescending { it.fecha }.take(5),
                        entrenamientosViewModel = entrenamientosViewModel,
                        navController = navController
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Estadísticas
                    StatisticsSection(
                        totalEntrenamientos = totalEntrenamientos,
                        horasTotales = horasTotales
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Objetivos
                    GoalsSection(
                        usuario = usuario,
                        medicionesViewModel = medicionesViewModel
                    )

                    // Espacio adicional al final para el footer
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }

        // Footer fijo en la parte inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FooterNavigation(
                navController = navController,
                currentRoute = "perfil",
                usuario = usuario
            )
        }
    }
}

@Composable
fun UserProfileSection(usuario: Usuarios?) {
    if (usuario == null) return

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil
            val showDefaultImage = shouldShowDefaultImage(usuario.foto)

            if (!showDefaultImage) {
                // Añadir try-catch para manejar errores de conversión
                val bitmap = getImageBitmapSafely(usuario.foto ?: "")
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap as ImageBitmap,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2A2A2A))
                    )
                } else {
                    DefaultProfileImage()
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del usuario
            Column {
                Text(
                    text = "${usuario.nombre} ${usuario.apellido}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = usuario.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Nivel de actividad
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7B1FA2).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (usuario.nivelActividad.isNullOrEmpty()) {
                                    "Sin nivel de actividad"
                                } else {
                                    usuario.nivelActividad
                                },
                            fontSize = 12.sp,
                            color = Color(0xFFAB47BC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(navController: NavController, medicionesViewModel: MedicionesViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            icon = Icons.Default.FitnessCenter,
            text = "Mis Rutinas",
            onClick = { navController.navigate("misEntrenamientos") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ActionButton(
            icon = Icons.Default.Add,
            text = "Crear",
            onClick = { navController.navigate("crearEntrenamiento") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ActionButton(
            icon = Icons.Default.Straighten,
            text = "Ver mediciones",
            onClick = {  },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF252525)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TrainingHistorySection(
    entrenamientosRealizados: List<EntrenamientoRealizado>,
    entrenamientosViewModel: EntrenamientosViewModel,
    navController: NavController
) {
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
            // Título y botón ver más
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Entrenamientos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(
                    onClick = { navController.navigate("historialEntrenamientosRealizados") },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
                ) {
                    Text(
                        text = "Ver todo",
                        fontSize = 14.sp
                    )
                }
            }

            // Divider
            Divider(
                color = Color(0xFF2A2A2A),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista de entrenamientos realizados
            if (entrenamientosRealizados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No has realizado ningún entrenamiento aún",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    entrenamientosRealizados.forEach { entrenamiento ->
                        EntrenamientoRealizadoItem(
                            entrenamientoRealizado = entrenamiento,
                            entrenamientosViewModel = entrenamientosViewModel,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EntrenamientoRealizadoItem(
    entrenamientoRealizado: EntrenamientoRealizado,
    entrenamientosViewModel: EntrenamientosViewModel,
    navController: NavController
) {
    var nombreEntrenamiento by remember { mutableStateOf("Cargando...") }
    var categoriaEntrenamiento by remember { mutableStateOf("") }
    var fotoEntrenamiento by remember { mutableStateOf("") }

    // Obtener información del entrenamiento
    LaunchedEffect(entrenamientoRealizado.entrenamiento) {
        try {
            val entrenamiento = entrenamientosViewModel.getEntrenamientoById(entrenamientoRealizado.entrenamiento)
            entrenamiento?.let {
                nombreEntrenamiento = it.nombre
                categoriaEntrenamiento = it.categoria
                fotoEntrenamiento = it.foto ?: ""
            }
        } catch (e: Exception) {
            Log.e("EntrenamientoRealizado", "Error al cargar entrenamiento: ${e.message}")
        }

    }

    // Formatear fecha
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaFormateada = dateFormat.format(entrenamientoRealizado.fecha)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("detalleEntrenamientoRealizado/${entrenamientoRealizado._id}") },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B1FA2).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (fotoEntrenamiento.isNotEmpty()) {
                    val bitmap = getImageBitmapSafely(fotoEntrenamiento)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap as ImageBitmap,
                            contentDescription = "Imagen de entrenamiento",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Entrenamiento",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Entrenamiento",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombreEntrenamiento,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = categoriaEntrenamiento,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = fechaFormateada,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Duración",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = entrenamientoRealizado.duracion,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Flecha para indicar que se puede entrar
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Ver detalles",
                tint = Color(0xFFAB47BC)
            )
        }
    }
}

@Composable
fun StatisticsSection(
    totalEntrenamientos: Int,
    horasTotales: Int
) {
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
            Text(
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Divider
            Divider(
                color = Color(0xFF2A2A2A),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Total de entrenamientos
                StatisticItem(
                    icon = Icons.Default.FitnessCenter,
                    value = totalEntrenamientos.toString(),
                    label = "Entrenamientos realizados"
                )

                // Horas totales
                StatisticItem(
                    icon = Icons.Default.HourglassTop,
                    value = "$horasTotales h",
                    label = "Horas dedicadas"
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF7B1FA2).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GoalsSection(
    usuario: Usuarios?,
    medicionesViewModel: MedicionesViewModel
) {
    if (usuario == null) return

    // Estados de mediciones
    val mediciones by medicionesViewModel.mediciones.collectAsState()
    val progresoPeso by medicionesViewModel.progresoPeso.collectAsState()
    val estadisticas by medicionesViewModel.estadisticas.collectAsState()
    var showNuevaMedicionDialog by remember { mutableStateOf(false) }

    // Cargar datos al inicializar
    LaunchedEffect(Unit) {
        medicionesViewModel.cargarMedicionesPorUsuario(tipo = TipoMedicion.PESO)

        delay(300)

        medicionesViewModel.verificarYCrearMedicionInicial()

        medicionesViewModel.cargarEstadisticas(TipoMedicion.PESO)
    }

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
            // Encabezado con botón para añadir medición
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi Progreso de Peso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Botón para agregar medición
                IconButton(
                    onClick = { showNuevaMedicionDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7B1FA2).copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar Medición",
                        tint = Color(0xFFAB47BC)
                    )
                }
            }

            // Divider
            Divider(
                color = Color(0xFF2A2A2A),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tarjetas de estadísticas - solo si hay datos
            if (estadisticas != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Peso actual
                    WeightStatCard(
                        title = "Peso actual",
                        value = "${estadisticas?.ultimo ?: "--"} kg",
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Cambio desde el peso inicial
                    val pesoInicial = usuario.peso
                    val pesoActual = estadisticas?.ultimo ?: pesoInicial
                    val cambio = pesoActual - pesoInicial
                    val cambioTexto = if (cambio >= 0) "+${String.format("%.1f", cambio)} kg" else "${String.format("%.1f", cambio)} kg"
                    val cambioColor = if ((usuario.objetivoPeso > pesoInicial && cambio > 0) ||
                        (usuario.objetivoPeso < pesoInicial && cambio < 0))
                        Color(0xFF4CAF50) else Color(0xFFE57373)

                    WeightStatCard(
                        title = "Cambio",
                        value = cambioTexto,
                        valueColor = cambioColor,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Objetivo de peso (si está configurado)
            if (usuario.objetivoPeso > 0f) {
                GoalProgressItem(
                    icon = Icons.Default.Scale,
                    title = "Objetivo de Peso",
                    initialValue = usuario.peso,
                    currentValue = estadisticas?.ultimo ?: usuario.peso,
                    targetValue = usuario.objetivoPeso,
                    unit = "kg",
                    progress = progresoPeso
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Gráfica de progreso con datos reales
            Text(
                text = "Historial de Peso",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Gráfico con datos reales
            WeightHistoryChart(
                mediciones = mediciones.filter { it.tipo == TipoMedicion.PESO.name }
            )
        }
    }

    // Diálogo para nueva medición
    if (showNuevaMedicionDialog) {
        NuevaMedicionDialog(
            onDismiss = { showNuevaMedicionDialog = false },
            onConfirm = { valor, notas ->
                medicionesViewModel.registrarPeso(valor, notas)
                showNuevaMedicionDialog = false
            }
        )
    }
}


@Composable
fun WeightStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeightHistoryChart(mediciones: List<Mediciones>) {
    // Si no hay datos, mostrar mensaje
    if (mediciones.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF252525))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aún no has registrado mediciones de peso",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Ordenar mediciones por fecha y tomar las últimas 7
    val medicionesOrdenadas = mediciones
        .sortedBy { it.fecha }
        .takeLast(7)

    // Extraer datos para el gráfico
    val datos = medicionesOrdenadas.map { it.valor }

    // Calcular rango del eje Y para mejor visualización
    val (minValue, maxValue) = calcularRangoEjeY(datos)

    // Formatear fechas para etiquetas
    val formato = SimpleDateFormat("dd/MM", Locale.getDefault())
    val etiquetas = medicionesOrdenadas.map { formato.format(it.fecha) }

    val density = LocalDensity.current

    val divisionesY = 4

    // Gráfico
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252525))
            .padding(
                start = 25.dp,
                end = 15.dp,
                top = 15.dp,
                bottom = 15.dp
            )
    ) {
        // Etiquetas del eje Y
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-40).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Valores del eje Y (5 puntos)
            val yValues = List(divisionesY + 1) { i ->
                maxValue - (i * (maxValue - minValue) / divisionesY)
            }

            yValues.forEach { value ->
                Text(
                    text = String.format("%.1f", value),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            if (datos.size < 2) {
                // Si solo hay un dato, mostrar un punto
                val x = width / 2
                val y = height / 2

                // Obtener los valores correctos de radio con densidad
                val radiusOuter = with(density) { 6.dp.toPx() }
                val radiusInner = with(density) { 3.dp.toPx() }
                val glowRadius = with(density) { 8.dp.toPx() }

                // Dibujar el punto con brillo
                drawCircle(
                    color = Color(0xFFAB47BC).copy(alpha = 0.3f),
                    radius = glowRadius,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFF7B1FA2),
                    radius = radiusOuter,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color(0xFFAB47BC),
                    radius = radiusInner,
                    center = Offset(x, y)
                )
                return@Canvas
            }

            val stepX = width / (datos.size - 1).coerceAtLeast(1)

            // Calcular altura por unidad
            val valueRange = maxValue - minValue
            val unitHeight = height / valueRange

            // Líneas de cuadrícula
            for (i in 0..divisionesY) {
                val y = height * i / divisionesY
                drawLine(
                    color = Color(0xFF3A3A3A),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = with(density) { 1.dp.toPx() }
                )
            }

            // Línea de progreso - usando Path para mejor apariencia
            val linePath = Path()
            datos.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value - minValue) * unitHeight)

                if (index == 0) {
                    linePath.moveTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                }
            }

            // Dibujar línea de progreso con suavizado
            drawPath(
                path = linePath,
                color = Color(0xFFAB47BC),
                style = Stroke(
                    width = with(density) { 3.dp.toPx() },
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Puntos con etiquetas de valor
            datos.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value - minValue) * unitHeight)

                // Obtener los valores correctos de radio con densidad
                val glowRadius = with(density) { 8.dp.toPx() }
                val outerRadius = with(density) { 6.dp.toPx() }
                val innerRadius = with(density) { 3.dp.toPx() }

                // Círculo exterior luminoso (efecto de brillo)
                drawCircle(
                    color = Color(0xFFAB47BC).copy(alpha = 0.3f),
                    radius = glowRadius,
                    center = Offset(x, y)
                )

                // Círculo exterior
                drawCircle(
                    color = Color(0xFF7B1FA2),
                    radius = outerRadius,
                    center = Offset(x, y)
                )

                // Círculo interior
                drawCircle(
                    color = Color(0xFFAB47BC),
                    radius = innerRadius,
                    center = Offset(x, y)
                )
            }
        }

        // Etiquetas del eje X con mejor presentación
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 30.dp)
        ) {
            // Líneas verticales de referencia para las etiquetas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val stepX = width / (etiquetas.size - 1).coerceAtLeast(1)

                for (i in etiquetas.indices) {
                    val x = if (etiquetas.size == 1) width / 2 else i * stepX

                    // Línea vertical sutil
                    drawLine(
                        color = Color(0xFF3A3A3A),
                        start = Offset(x, 0f),
                        end = Offset(x, with(density) { -5.dp.toPx() }),
                        strokeWidth = with(density) { 1.dp.toPx() }
                    )
                }
            }

            // Etiquetas de fecha
            if (etiquetas.size <= 1) {
                // Caso especial para un solo punto
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (etiquetas.isNotEmpty()) {
                        Text(
                            text = etiquetas.first(),
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Múltiples puntos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    etiquetas.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(40.dp) // Ancho fijo para todas las etiquetas
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calcula el rango óptimo para el eje Y basado en los datos
 */
private fun calcularRangoEjeY(datos: List<Float>): Pair<Float, Float> {
    if (datos.isEmpty()) return Pair(70f, 90f) // Valores por defecto si no hay datos

    val min = datos.minOrNull() ?: 0f
    val max = datos.maxOrNull() ?: 100f

    // Si solo hay un valor o todos son iguales, crear un rango artificial
    if (min == max) {
        val valor = min
        return Pair(
            (valor - valor * 0.05f).coerceAtLeast(0f), // 5% menos
            valor + valor * 0.05f // 5% más
        )
    }

    // Calcular rango con margen para mejor visualización
    val rango = max - min
    val margen = rango * 0.2f // 20% de margen

    return Pair(
        (min - margen).coerceAtLeast(0f),
        max + margen
    )
}



@Composable
fun GoalProgressItem(
    icon: ImageVector,
    title: String,
    initialValue: Float,
    currentValue: Float,
    targetValue: Float,
    unit: String,
    progress: Float
) {
    // Animación del progreso
    val progressAnimated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Título con icono
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        // Valores en tres filas separadas para mejor espaciado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            // Tres columnas con valores
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Inicial",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = String.format("%.1f %s", initialValue, unit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Actual",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = String.format("%.1f %s", currentValue, unit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Objetivo",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = String.format("%.1f %s", targetValue, unit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso con efecto de brillo - COMPLETAMENTE REDISEÑADA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            // Barra de progreso con animación
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressAnimated / 100)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    )
            )

            // Efecto de brillo en el borde del progreso solo cuando el progreso es visible pero no completo
            if (progressAnimated > 0 && progressAnimated < 100) {
                // Calcular la posición para el brillo relativo al ancho
                // No necesitamos usar conversiones toPx ya que usamos posicionamiento relativo
                val glowPosition = progressAnimated / 100

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    // El brillo se posiciona en el borde derecho del progreso
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        val center = Offset(
                            x = width * glowPosition,
                            y = height / 2
                        )

                        // Dibujar círculo de brillo
                        drawCircle(
                            color = Color(0xFFAB47BC).copy(alpha = 0.7f),
                            radius = height / 2,
                            center = center
                        )
                    }
                }
            }
        }

        // Porcentaje de progreso (más grande y prominente)
        Text(
            text = "${progressAnimated.roundToInt()}% completado",
            fontSize = 14.sp,
            color = Color(0xFFAB47BC),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
        )
    }
}



@Composable
fun ProgressChart(progreso: Float) {
    // Datos simulados para la gráfica (implementar con datos reales)
    val data = listOf(10f, 20f, 30f, 25f, 40f, 45f, progreso)
    val labels = listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4", "Sem 5", "Sem 6", "Ahora")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252525))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = width / (data.size - 1)
            val maxValue = data.maxOrNull() ?: 100f
            val stepY = height / maxValue

            // Dibujar líneas de la cuadrícula
            for (i in 0..4) {
                val y = height - (height * i / 4)
                drawLine(
                    color = Color(0xFF3A3A3A),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Dibujar línea de progreso
            for (i in 0 until data.size - 1) {
                val startX = i * stepX
                val startY = height - (data[i] * stepY)
                val endX = (i + 1) * stepX
                val endY = height - (data[i + 1] * stepY)

                drawLine(
                    color = Color(0xFFAB47BC),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Dibujar puntos
            data.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - (value * stepY)

                drawCircle(
                    color = Color(0xFF7B1FA2),
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color(0xFFAB47BC),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Etiquetas en el eje X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}


@Composable
private fun DefaultProfileImage() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(40.dp)
        )
    }
}

private fun shouldShowDefaultImage(foto: String?): Boolean {
    return foto == null || foto.isEmpty()
}


@Composable
fun FormularioRequiredDialog(
    onDismiss: () -> Unit,
    onGoToForm: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                // Icono animado
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Información",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )

                // Título con gradiente
                Text(
                    text = "Formulario Requerido",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mensaje
                Text(
                    text = "Para configurar tu perfil, primero debes completar el formulario de información personal.",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Botón para ir al formulario
                Button(
                    onClick = onGoToForm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFAB47BC),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Formulario",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Completar Formulario",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Separador con texto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF3A3A3A)
                    )
                    Text(
                        text = "o",
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF3A3A3A)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de cerrar sesión
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE57373)
                    ),
                    border = BorderStroke(2.dp, Color(0xFFE57373)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar sesión",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancelar", fontSize = 14.sp)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaMedicionDialog(
    onDismiss: () -> Unit,
    onConfirm: (valor: Float, notas: String) -> Unit
) {
    var valorTexto by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    // Validación de entrada
    val valorError = remember(valorTexto) {
        when {
            valorTexto.isEmpty() -> "Ingrese un valor"
            valorTexto.toFloatOrNull() == null -> "Debe ser un número válido"
            valorTexto.toFloat() < 20f || valorTexto.toFloat() > 300f ->
                "El peso debe estar entre 20 y 300 kg"
            else -> ""
        }
    }

    val isValid = valorTexto.isNotEmpty() && valorError.isEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                // Título
                Text(
                    text = "Registrar Peso",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Icono de peso
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = "Peso",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )

                // Campo para ingresar el valor
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = { valorTexto = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    trailingIcon = {
                        Text(
                            text = "kg",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    isError = valorError.isNotEmpty(),
                    supportingText = {
                        if (valorError.isNotEmpty()) {
                            Text(
                                text = valorError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray
                    ),
                    textStyle = TextStyle(color = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo para notas (opcional)
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = Color(0xFFAB47BC),
                        unfocusedLabelColor = Color.Gray
                    ),
                    textStyle = TextStyle(color = Color.White)
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
                        Text("Cancelar")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            valorTexto.toFloatOrNull()?.let { valor ->
                                onConfirm(valor, notas)
                            }
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFAB47BC),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFAB47BC).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}