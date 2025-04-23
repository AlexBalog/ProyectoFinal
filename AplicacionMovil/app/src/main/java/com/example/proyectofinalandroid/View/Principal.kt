package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import android.text.Layout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlinx.coroutines.delay
import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.proyectofinalandroid.ViewModel.EventosUsuarioViewModel
import com.example.proyectofinalandroid.utils.base64ToBitmap
import com.example.proyectofinalandroid.ViewModel.EventosViewModel
import com.example.proyectofinalandroid.Model.EventosUsuario
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner

@SuppressLint("UnrememberedGetBackStackEntry", "RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Declarar el estado de diálogo primero
    var showExitDialog by remember { mutableStateOf(false) }

    val activity = LocalContext.current as? Activity

    val backDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    DisposableEffect(backDispatcherOwner) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog = true
            }
        }

        backDispatcherOwner?.onBackPressedDispatcher?.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(parentEntry)
    val eventosUsuariosViewModel: EventosUsuarioViewModel = hiltViewModel()
    val eventosViewModel: EventosViewModel = hiltViewModel()

    val usuario by usuariosViewModel.usuario.collectAsState()

    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(usuario!!)
        }
    }

    // Estados
    var isAnimatedIn by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Valores derivados - no usar remember aquí
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()

    val scrollState = rememberScrollState()

    // Datos simulados
    val misEntrenamientos by entrenamientosViewModel.entrenamientos.collectAsState()
    val programasDestacados by remember { mutableStateOf(entrenamientosViewModel.obtenerProgramasDestacados()) }
    val eventosUsuario by eventosUsuariosViewModel.eventosUsuarioLista.collectAsState()

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
            TopAppBar(
                title = {
                    Text(
                        text = "FitSphere",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFAB47BC),
                                    Color(0xFF7B1FA2)
                                )
                            )
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = { /* Acción de notificaciones */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color(0xFFAB47BC)
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
                    // Calendario
                    CalendarSection(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        onMonthChanged = { newMonth -> currentMonth = newMonth }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Eventos programados para la fecha seleccionada
                    ProgrammedEventsSection(
                        date = selectedDate,
                        eventos = eventosUsuario as List<EventosUsuario>
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mis Entrenamientos (solo se muestra si hay entrenamientos disponibles)
                    if (misEntrenamientos?.isNotEmpty() == true) {
                        MisEntrenamientosSection(
                            entrenamientos = misEntrenamientos!!,
                            navController = navController
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Programas Destacados
                    EntrenamientosDestacadosSection(
                        programas = programasDestacados,
                        navController = navController
                    )

                    // Espacio adicional al final
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
                currentRoute = "home",
                usuario = usuario
            )
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(text = "¿Salir de la app?") },
                text = { Text(text = "¿Estás seguro de que quieres salir?")},
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }) {
                        Text(text = "Sí", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text(text = "Cancelar", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF7B1FA2),
                        spotColor = Color(0xFF7B1FA2)
                    ),
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun CalendarSection(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    Log.d("Investigar", "$currentMonth")
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
            // Título del mes y año
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val texto = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES")))
                val textoCapitalizado = texto.replaceFirstChar { it.uppercase() }
                Text(
                    text = textoCapitalizado,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row {
                    IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Mes anterior",
                            tint = Color(0xFFAB47BC)
                        )
                    }

                    IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Mes siguiente",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Días de la semana
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in DayOfWeek.values()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")).uppercase(),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Días del mes
            DiasDelMes(currentMonth, selectedDate, onDateSelected)
        }
    }
}

@Composable
fun DiasDelMes(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfWeek = DayOfWeek.MONDAY
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonthValue = currentMonth.atDay(1).dayOfWeek.value
    val offset = (firstDayOfMonthValue - firstDayOfWeek.value + 7) % 7

    val totalDaysToShow = daysInMonth + offset
    val rowsNeeded = (totalDaysToShow + 6) / 7

    for (row in 0 until rowsNeeded) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (column in 0 until 7) {
                val day = row * 7 + column - offset + 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Color(0xFFAB47BC)
                                        isToday -> Color(0xFF7B1FA2).copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> Color(0xFFAB47BC)
                                    else -> Color.White
                                },
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgrammedEventsSection(
    date: LocalDate,
    eventos: List<EventosUsuario>
) {
    var showAddSerieDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Programado para ${date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { showAddSerieDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar evento",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFFAB47BC).copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (eventos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nada programado",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                eventos.forEach { evento ->
                  //  EventoItem(evento = evento)
                    if (evento != eventos.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFF2A2A2A)
                        )
                    }
                }
            }
        }
        if (showAddSerieDialog) {
            DialogoEventos(
                onConfirm = { peso, repeticiones ->
                  //  eventos.new(Serie(peso, repeticiones))
                    showAddSerieDialog = false
                },
                onDismiss = { showAddSerieDialog = false }
            )
        }
    }
}

/*@Composable
fun EventoItem(evento: EventosUsuario) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF7B1FA2).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (evento.tipo) {
                    TipoEvento.ENTRENAMIENTO -> Icons.Default.FitnessCenter
                    TipoEvento.MEDICION -> Icons.Default.Monitor
                    TipoEvento.NUTRICION -> Icons.Default.Restaurant
                },
                contentDescription = evento.tipo.name,
                tint = Color(0xFFAB47BC)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = evento.titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Text(
                text = evento.descripcion,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = evento.hora,
            fontSize = 14.sp,
            color = Color(0xFFAB47BC),
            fontWeight = FontWeight.Medium
        )
    }
}*/

@Composable
fun MisEntrenamientosSection(
    entrenamientos: List<Entrenamientos>,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis Entrenamientos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TextButton(
                onClick = { /* Ver todos los entrenamientos */ },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
            ) {
                Text(
                    text = "Ver todo",
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más"
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(entrenamientos) { entrenamiento ->
                EntrenamientoItem(
                    entrenamiento = entrenamiento,
                    onClick = { navController.navigate("detalleEntrenamiento/${entrenamiento._id}") }
                )
            }
        }
    }
}

@Composable
fun EntrenamientoItem(
    entrenamiento: Entrenamientos,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Image(
                    painter = remember { BitmapPainter(base64ToBitmap(entrenamiento.foto)!!.asImageBitmap()) },
                    contentDescription = "Imagen de sesión",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0D0D0D).copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7B1FA2).copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Duración",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entrenamiento.duracion}",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = entrenamiento.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = entrenamiento.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entrenamiento.likes}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun EntrenamientosDestacadosSection(
    programas: List<ProgramaDestacado>,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Programas Destacados",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TextButton(
                onClick = { /* Ver todos los programas destacados */ },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
            ) {
                Text(
                    text = "Ver todo",
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más"
                )
            }
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(programas) { programa ->
            ProgramaDestacadoItem(
                programa = programa,
                onClick = { /* Navegar a detalles del programa */ }
            )
        }
    }
}


@Composable
fun ProgramaDestacadoItem(
    programa: ProgramaDestacado,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = programa.imagenResId),
                    contentDescription = "Imagen de programa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0D0D0D).copy(alpha = 0.8f)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7B1FA2).copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Puntuación",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${programa.puntuacion}",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = programa.nivel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (programa.nivel) {
                                    "Principiante" -> Color(0xFF4CAF50).copy(alpha = 0.8f)
                                    "Intermedio" -> Color(0xFFFFC107).copy(alpha = 0.8f)
                                    else -> Color(0xFFFF5722).copy(alpha = 0.8f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = programa.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = programa.descripcion,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Ejercicios",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${programa.ejercicios} ejercicios",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${programa.likes}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FooterNavigation(
    navController: NavController,
    currentRoute: String,
    usuario: Usuarios?
) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.height(105.dp)
    ) {
        FooterNavItem(
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendario") },
            label = "HOY",
            isSelected = currentRoute == "home",
            onClick = { /* Navegar a home */ }
        )

        FooterNavItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = "BUSCAR",
            isSelected = currentRoute == "search",
            onClick = { /* Navegar a búsqueda */ }
        )

        FooterNavItem(
            icon = { Icon(Icons.Default.Psychology, contentDescription = "FitMind") },
            label = "FitMind",
            isSelected = currentRoute == "fitmind",
            onClick = { /* Navegar a FitMind */ }
        )

        FooterNavItem(
            icon = {
                if (!usuario?.foto.isNullOrEmpty()) {
                    Image(
                        bitmap = base64ToImageBitmap(usuario!!.foto) as ImageBitmap,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
            },
            label = "PERFIL",
            isSelected = currentRoute == "profile",
            onClick = { /* Navegar a perfil */ }
        )
    }
}

@Composable
fun RowScope.FooterNavItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = icon,
        label = {
            Text(
                text = label,
                fontSize = 10.sp
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFFAB47BC),
            selectedTextColor = Color(0xFFAB47BC),
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = Color.Black
        )
    )
}

// Clases de modelo para simulación de datos
data class EventoProgramado(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val hora: String,
    val tipo: TipoEvento
)

enum class TipoEvento {
    ENTRENAMIENTO, MEDICION, NUTRICION
}

data class ProgramaDestacado(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val nivel: String,
    val ejercicios: Int,
    val likes: Int,
    val puntuacion: Double,
    val imagenResId: Int
)

// Extensiones del ViewModel para simulación de datos
fun EntrenamientosViewModel.obtenerEventosFecha(fecha: LocalDate): List<EventoProgramado> {
    // Simula eventos desde la base de datos
    return if (fecha == LocalDate.now()) {
        listOf(
            EventoProgramado(
                id = "1",
                titulo = "Entrenamiento de Fuerza",
                descripcion = "Enfocado en piernas y glúteos",
                hora = "10:30",
                tipo = TipoEvento.ENTRENAMIENTO
            ),
            EventoProgramado(
                id = "2",
                titulo = "Medición Mensual",
                descripcion = "Control de peso y composición corporal",
                hora = "17:00",
                tipo = TipoEvento.MEDICION
            )
        )
    } else if (fecha == LocalDate.now().plusDays(1)) {
        listOf(
            EventoProgramado(
                id = "3",
                titulo = "Planificación Nutricional",
                descripcion = "Revisión de dieta semanal",
                hora = "11:00",
                tipo = TipoEvento.NUTRICION
            )
        )
    } else {
        emptyList() // Sin eventos para esta fecha
    }
}

fun EntrenamientosViewModel.obtenerProgramasDestacados(): List<ProgramaDestacado> {
    // Simula programas destacados desde la base de datos
    return listOf(
        ProgramaDestacado(
            id = "1",
            nombre = "Definición Total",
            descripcion = "Programa de 4 semanas para definir todo el cuerpo",
            nivel = "Intermedio",
            ejercicios = 24,
            likes = 487,
            puntuacion = 4.8,
            imagenResId = R.drawable.logo // Usa un placeholder para el ejemplo
        ),
        ProgramaDestacado(
            id = "2",
            nombre = "Quema Grasa Extrema",
            descripcion = "Rutinas HIIT de alta intensidad para pérdida de peso",
            nivel = "Avanzado",
            ejercicios = 18,
            likes = 362,
            puntuacion = 4.7,
            imagenResId = R.drawable.logo // Usa un placeholder para el ejemplo
        ),
        ProgramaDestacado(
            id = "3",
            nombre = "Iniciación Fitness",
            descripcion = "Programa para principiantes con ejercicios básicos",
            nivel = "Principiante",
            ejercicios = 15,
            likes = 298,
            puntuacion = 4.9,
            imagenResId = R.drawable.logo // Usa un placeholder para el ejemplo
        ),
        ProgramaDestacado(
            id = "4",
            nombre = "Fuerza y Volumen",
            descripcion = "Programa para ganar masa muscular y fuerza",
            nivel = "Intermedio",
            ejercicios = 20,
            likes = 421,
            puntuacion = 4.6,
            imagenResId = R.drawable.logo // Usa un placeholder para el ejemplo
        ),
        ProgramaDestacado(
            id = "5",
            nombre = "Core Power",
            descripcion = "Especializado en fortalecer el núcleo y abdominales",
            nivel = "Intermedio",
            ejercicios = 16,
            likes = 375,
            puntuacion = 4.5,
            imagenResId = R.drawable.logo // Usa un placeholder para el ejemplo
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEventos(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var peso by remember { mutableStateOf("") }
    var repeticiones by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Añadir Serie",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAB47BC)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Campo de peso
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        //textColor = Color.White,
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFAB47BC)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Campo de repeticiones
                OutlinedTextField(
                    value = repeticiones,
                    onValueChange = { repeticiones = it },
                    label = { Text("Repeticiones") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color(0xFFAB47BC),
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFAB47BC)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
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

                    TextButton(
                        onClick = { onConfirm(peso, repeticiones) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFAB47BC)
                        ),
                        enabled = peso.isNotEmpty() && repeticiones.isNotEmpty()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}