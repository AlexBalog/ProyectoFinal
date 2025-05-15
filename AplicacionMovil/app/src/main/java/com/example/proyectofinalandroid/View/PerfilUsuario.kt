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
import com.example.proyectofinalandroid.utils.getImageBitmapSafely


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

    // Este ViewModel debe ser implementado para obtener los entrenamientos realizados
    val entrenamientoRealizadoViewModel: EntrenamientoRealizadoViewModel = hiltViewModel()

    // Estados
    var isAnimatedIn by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val usuario by usuariosViewModel.usuario.collectAsState()

    // Lista de entrenamientos realizados
    // Aquí deberías implementar un método en tu ViewModel para obtener esta lista filtrada por el usuario actual
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
        // entrenamientoRealizadoViewModel.getEntrenamientosRealizadosByUsuario(usuario?._id ?: "")
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
                    ActionButtonsRow(navController = navController)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Historial de entrenamientos
                    TrainingHistorySection(
                        entrenamientosRealizados = entrenamientosRealizados!!.take(10),
                        entrenamientosViewModel = entrenamientosViewModel
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
                        progresoObjetivo = progresoObjetivo
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
fun ActionButtonsRow(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            icon = Icons.Default.FitnessCenter,
            text = "Mis Rutinas",
            onClick = { /* Navegar a mis entrenamientos */ },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ActionButton(
            icon = Icons.Default.Add,
            text = "Crear",
            onClick = { /* Navegar a crear entrenamiento */ },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ActionButton(
            icon = Icons.Default.SupportAgent,
            text = "Asesor",
            onClick = { /* Navegar a asesor */ },
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
    entrenamientosViewModel: EntrenamientosViewModel
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
                    onClick = { /* Ver todo el historial */ },
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
                            entrenamientosViewModel = entrenamientosViewModel
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
    entrenamientosViewModel: EntrenamientosViewModel
) {
    var nombreEntrenamiento by remember { mutableStateOf("Cargando...") }
    var categoriaEntrenamiento by remember { mutableStateOf("") }

    // Obtener información del entrenamiento
    LaunchedEffect(entrenamientoRealizado.entrenamiento) {
        // Aquí deberías implementar la obtención del entrenamiento por ID
        // Este es un lugar donde debes implementar tu propia lógica en el ViewModel
        val entrenamiento = entrenamientosViewModel.getEntrenamientoById(entrenamientoRealizado.entrenamiento)
        entrenamiento?.let {
            nombreEntrenamiento = it.nombre
            categoriaEntrenamiento = it.categoria
        }
    }

    // Formatear fecha
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaFormateada = dateFormat.format(entrenamientoRealizado.fecha)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Ver detalles del entrenamiento realizado */ },
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
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Entrenamiento",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
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
    progresoObjetivo: Float
) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mis Objetivos",
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

            // Objetivo de peso
            if (usuario.objetivoPeso > 0f) {
                GoalProgressItem(
                    icon = Icons.Default.Scale,
                    title = "Objetivo de Peso",
                    currentValue = usuario.peso,
                    targetValue = usuario.objetivoPeso,
                    unit = "kg",
                    progress = progresoObjetivo
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Objetivo de calorías
            if (usuario.objetivoCalorias > 0f) {
                GoalProgressItem(
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Objetivo de Calorías",
                    currentValue = 0f, // Implementar valor actual
                    targetValue = usuario.objetivoCalorias,
                    unit = "kcal",
                    progress = 0f // Implementar progreso real
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Gráfica de progreso (gráfica simplificada)
            Text(
                text = "Progreso",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ProgressChart(progresoObjetivo)
        }
    }
}

@Composable
fun GoalProgressItem(
    icon: ImageVector,
    title: String,
    currentValue: Float,
    targetValue: Float,
    unit: String,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
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

        Spacer(modifier = Modifier.height(8.dp))

        // Valores actual y objetivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Actual: $currentValue $unit",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text(
                text = "Objetivo: $targetValue $unit",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress / 100)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Porcentaje de progreso
        Text(
            text = "${progress.roundToInt()}% completado",
            fontSize = 12.sp,
            color = Color(0xFFAB47BC),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.End)
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

// Función para obtener entrenamientos por ViewModel (a implementar)
private fun EntrenamientosViewModel.getEntrenamientoById(id: String): Entrenamientos? {
    // Implementar método en tu ViewModel para obtener entrenamiento por ID
    // return this.obtenerEntrenamientoPorId(id)
    return null
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