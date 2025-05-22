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
import com.example.proyectofinalandroid.Model.Eventos
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.utils.base64ToImageBitmap
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import java.time.LocalDate
import java.time.ZoneId
import com.example.proyectofinalandroid.Model.Ejercicios
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import java.util.concurrent.TimeUnit
import com.example.proyectofinalandroid.worker.EntrenamientoReminderWorker



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

    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val eventosUsuariosViewModel: EventosUsuarioViewModel = hiltViewModel()
    val eventosViewModel: EventosViewModel = hiltViewModel()

    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()

    val usuario by usuariosViewModel.usuario.collectAsState()

    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(usuario!!)
            eventosViewModel.setUsuario(usuario!!)
            eventosUsuariosViewModel.setUsuario(usuario!!)
            ejerciciosViewModel.setUsuario(usuario!!)
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
    val context = LocalContext.current

    // Datos
    val entrenamientos by entrenamientosViewModel.entrenamientos.collectAsState()
    val ejercicios by ejerciciosViewModel.ejercicios.collectAsState()
    val eventosUsuario by eventosUsuariosViewModel.eventosUsuarioLista.collectAsState()

    val entrenamientosDestacados = remember(entrenamientos) {
        entrenamientos?.sortedByDescending { it.likes.toInt() }?.take(5) ?: emptyList()
    }

    val ejerciciosRecomendados = remember(ejercicios) {
        ejercicios?.take(10) ?: emptyList()
    }

    val fechasConEventos = remember(eventosUsuario) {
        eventosUsuario?.map { it.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() } ?: emptyList()
    }
    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        eventosViewModel.cargarEventosYTipos()
        eventosUsuariosViewModel.getFilter(mapOf("usuario" to usuario!!._id))
        ejerciciosViewModel.getAll()
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
                        onMonthChanged = { newMonth -> currentMonth = newMonth },
                        fechasConEventos = fechasConEventos
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Eventos programados para la fecha seleccionada
                    ProgrammedEventsSection(
                        date = selectedDate,
                        eventosUsuarioViewModel = eventosUsuariosViewModel,
                        eventosViewModel = eventosViewModel
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mis Entrenamientos (solo se muestra si hay entrenamientos disponibles)
                    if (entrenamientos?.isNotEmpty() == true) {
                        MisEntrenamientosSection(
                            entrenamientos = entrenamientos!!,
                            navController = navController,
                            usuario = usuario as Usuarios
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (entrenamientosDestacados.isNotEmpty()) {
                        EntrenamientosDestacadosSection(
                            entrenamientos = entrenamientosDestacados,
                            navController = navController
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Ejercicios Recomendados
                    if (ejerciciosRecomendados.isNotEmpty()) {
                        EjerciciosRecomendadosSection(
                            ejercicios = ejerciciosRecomendados,
                            navController = navController
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
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
    onMonthChanged: (YearMonth) -> Unit,
    fechasConEventos: List<LocalDate>
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
            DiasDelMes(currentMonth, selectedDate, onDateSelected, fechasConEventos = fechasConEventos)
        }
    }
}

@Composable
fun DiasDelMes(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    fechasConEventos: List<LocalDate>
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
                        val tieneEvento = fechasConEventos.contains(date)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Color(0xFFAB47BC)
                                        isToday -> Color(0xFF7B1FA2).copy(alpha = 0.3f)
                                        tieneEvento -> Color(0xFF3F51B5).copy(alpha = 0.3f)
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
                                    tieneEvento -> Color(0xFF9FA8DA)
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
    eventosViewModel: EventosViewModel,
    eventosUsuarioViewModel: EventosUsuarioViewModel
) {
    val eventos by eventosUsuarioViewModel.eventosUsuarioLista.collectAsState()
    val eventosFiltrados = remember(eventos, date) {
        eventos?.filter { evento ->
            val localDate = evento.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            localDate == date
        } ?: emptyList()
    }

    var showAddEventoDialog by remember { mutableStateOf(false) }
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
                    onClick = { showAddEventoDialog = true }
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

            when {
                eventosFiltrados == null -> {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
                eventosFiltrados!!.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay eventos programados para este día",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    eventosFiltrados?.forEach { evento ->
                        EventoItem(evento = evento, eventosViewModel = eventosViewModel, eventosUsuarioViewModel = eventosUsuarioViewModel)
                        if (evento != eventos?.last()) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFF2A2A2A)
                            )
                        }
                    }
                }
            }
        }
        if (showAddEventoDialog) {
            DialogoEventos(
                onConfirm = { evento, hora, notas ->
                    showAddEventoDialog = false
                },
                onDismiss = { showAddEventoDialog = false },
                eventosViewModel = eventosViewModel,
                eventosUsuarioViewModel = eventosUsuarioViewModel,
                selectedDate = date
            )
        }
    }
}

@Composable
fun EventoItem(evento: EventosUsuario, eventosViewModel: EventosViewModel, eventosUsuarioViewModel: EventosUsuarioViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    var showEditEventoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventoRelacionado by remember { mutableStateOf<Eventos?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(evento.evento) {
        eventoRelacionado = eventosViewModel.getOneReturn(evento.evento)
        isLoading = false
    }

    when {
        isLoading -> {
            CircularProgressIndicator(modifier = Modifier.size(20.dp))
        }
        eventoRelacionado == null -> {
            Text("Error cargando evento", color = Color.Red)
        }
        else -> {
            val ev = eventoRelacionado!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable{ showEditEventoDialog = true }
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
                        imageVector = when (ev.tipo) {
                            "Entrenamiento" -> Icons.Default.FitnessCenter
                            "Nutrición" -> Icons.Default.Restaurant
                            "Progreso" -> Icons.Default.TrendingUp
                            "Salud" -> Icons.Default.Favorite
                            else -> Icons.Default.Event
                        },
                        contentDescription = "Tipo de evento",
                        tint = Color(0xFFAB47BC)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ev.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Text(
                        text = ev.descripcion,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = evento!!.hora,
                    fontSize = 14.sp,
                    color = Color(0xFFAB47BC),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    if (showEditEventoDialog) {
        DialogoEventos(
            onConfirm = { _, _, _ ->
                showEditEventoDialog = false
            },
            onDismiss = { showEditEventoDialog = false },
            onEdit = { eventoUsuario, hora, notas ->
                scope.launch {
                    eventosUsuarioViewModel.update(
                        mapOf(
                            "hora" to hora,
                            "notas" to notas
                        ),
                        _id = eventoUsuario
                    )
                }
                showEditEventoDialog = false
            },
            onDelete = {
                showDeleteDialog = true
            },
            eventosViewModel = eventosViewModel,
            eventosUsuarioViewModel = eventosUsuarioViewModel,
            selectedDate = evento.fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            eventoExistente = eventoRelacionado,
            eventoUsuarioExistente = evento
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "¿Eliminar evento?") },
            text = { Text(text = "¿Estás seguro de que quieres eliminar este evento?")},
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        eventosUsuarioViewModel.delete(evento._id)
                    }
                    showDeleteDialog = false
                    showEditEventoDialog = false
                }) {
                    Text(text = "Sí", color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
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

@Composable
fun MisEntrenamientosSection(
    entrenamientos: List<Entrenamientos>,
    navController: NavController,
    usuario: Usuarios
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Mis Entrenamientos",
                    tint = Color(0xFF3a2bc2),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mis Entrenamientos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = { navController.navigate("misEntrenamientos") },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
            ) {
                Text(
                    text = "Ver más",
                    fontSize = 14.sp,
                    color = Color(0xFF3a2bc2)
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más",
                    tint = Color(0xFF3a2bc2)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(entrenamientos.filter { it.creador == usuario._id }) { entrenamiento ->
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
                            .background(Color(0xFF3a2bc2).copy(alpha = 0.8f))
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
                        tint = Color(0xFF3a2bc2),
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
            onClick = { navController.navigate("principal") }
        )

        FooterNavItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = "BUSCAR",
            isSelected = currentRoute == "search",
            onClick = { navController.navigate("buscador") }
        )

        FooterNavItem(
            icon = { Icon(Icons.Default.Psychology, contentDescription = "FitMind") },
            label = "FitMind",
            isSelected = currentRoute == "fitmind",
            onClick = { navController.navigate("fitmind") }
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
            isSelected = currentRoute == "perfil",
            onClick = { navController.navigate("perfil") }
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

@Composable
fun EntrenamientosDestacadosSection(
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = "Destacados",
                    tint = Color(0xFFbf3b26),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Entrenamientos Destacados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = { navController.navigate("buscador?orden=likes&tipoBusqueda=entrenamientos&ordenAscDesc=desc") },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
            ) {
                Text(
                    text = "Ver más",
                    fontSize = 14.sp,
                    color = Color(0xFFbf3b26)
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más",
                    tint = Color(0xFFbf3b26)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(entrenamientos.filter { it.aprobado == true }) { entrenamiento ->
                EntrenamientoDestacadoItem(
                    entrenamiento = entrenamiento,
                    onClick = { navController.navigate("detalleEntrenamiento/${entrenamiento._id}") }
                )
            }
        }
    }
}

@Composable
fun EntrenamientoDestacadoItem(
    entrenamiento: Entrenamientos,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
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
                    contentDescription = "Imagen de entrenamiento",
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

                // Badge de TOP
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFbf3b26))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TOP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Duración
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
                            text = "${entrenamiento.duracion} min",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = entrenamiento.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = entrenamiento.categoria,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color(0xFFbf3b26),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entrenamiento.likes}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EjerciciosRecomendadosSection(
    ejercicios: List<Ejercicios>,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Recommend,
                    contentDescription = "Recomendados",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ejercicios Recomendados",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = { navController.navigate("buscador?tipoBusqueda=ejercicios") }, // Ajustar la ruta si es necesario
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFAB47BC))
            ) {
                Text(
                    text = "Ver más",
                    fontSize = 14.sp,
                    color = Color(0xFFAB47BC)
                )
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Ver más",
                    tint = Color(0xFFAB47BC)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(ejercicios) { ejercicio ->
                EjercicioRecomendadoItem(
                    ejercicio = ejercicio,
                    onClick = { navController.navigate("detalleEjercicio/${ejercicio._id}") }
                )
            }
        }
    }
}

@Composable
fun EjercicioRecomendadoItem(
    ejercicio: Ejercicios,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
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
                    painter = remember { BitmapPainter(base64ToBitmap(ejercicio.foto)!!.asImageBitmap()) },
                    contentDescription = "Imagen de ejercicio",
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

                // Etiqueta de músculo
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF7B1FA2).copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ejercicio.musculo,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = ejercicio.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (ejercicio.consejos.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Consejos",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${ejercicio.consejos.size} consejos",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEventos(
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
    onEdit: ((String, String, String) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    eventosUsuarioViewModel: EventosUsuarioViewModel,
    eventosViewModel: EventosViewModel,
    selectedDate: LocalDate,
    eventoExistente: Eventos? = null,
    eventoUsuarioExistente: EventosUsuario? = null
) {
    val isEditMode = eventoUsuarioExistente != null

    var selectedEventName by remember { mutableStateOf<String?>(null) }
    var expandedEventType by remember { mutableStateOf(false) }
    var expandedEvent by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var notas by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<Eventos?>(null) }
    val eventTypes by eventosViewModel.tipoEventos.collectAsState()
    var selectedType by remember { mutableStateOf<String?>(null) }
    val allEvents by eventosViewModel.eventos.collectAsState()

    LaunchedEffect(eventoUsuarioExistente) {
        if (isEditMode && eventoUsuarioExistente != null) {
            // Cargar el evento relacionado para obtener tipo y nombre
            selectedEvent = eventoExistente
            selectedType = eventoExistente!!.tipo
            selectedEventName = eventoExistente!!.nombre

            // Parsear la hora existente (HH:MM)
            val horaParts = eventoUsuarioExistente.hora.split(":")
            if (horaParts.size == 2) {
                selectedHour = horaParts[0].toInt()
                selectedMinute = horaParts[1].toInt()
            }

            // Establecer las notas
            notas = eventoUsuarioExistente.notas ?: ""
        }
    }

    val filteredEvents = remember(allEvents, selectedType) {
        if (selectedType == null) allEvents else allEvents.filter { it.tipo == selectedType }
    }



    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(1.0f) // Aumentado para que ocupe más espacio
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
                // Header
                Text(
                    text = if (isEditMode) "Editar Evento" else "Programar Evento",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFAB47BC),
                                Color(0xFF7B1FA2)
                            )
                        )
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Event Type Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tipo de evento",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF252525))
                            .clickable(enabled = !isEditMode) { expandedEventType = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedType ?: "Selecciona un tipo",
                                color = if (selectedType != null) Color.White else Color.Gray
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedEventType && !isEditMode,
                        onDismissRequest = { expandedEventType = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF252525))
                    ) {
                        eventTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type, color = Color.White) },
                                onClick = {
                                    selectedType = type
                                    selectedEventName = null
                                    expandedEventType = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            "Entrenamiento" -> Icons.Default.FitnessCenter
                                            "Nutrición" -> Icons.Default.Restaurant
                                            "Progreso" -> Icons.Default.TrendingUp
                                            "Salud" -> Icons.Default.Favorite
                                            else -> Icons.Default.Event
                                        },
                                        contentDescription = type,
                                        tint = Color(0xFFAB47BC)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Event Dropdown (only visible if event type is selected)
                if (selectedType != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Evento",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF252525))
                                .clickable(enabled = !isEditMode) { expandedEvent = true }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedEventName ?: "Selecciona un evento",
                                    color = if (selectedEventName != null) Color.White else Color.Gray
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir",
                                    tint = Color(0xFFAB47BC)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expandedEvent && !isEditMode,
                            onDismissRequest = { expandedEvent = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF252525))
                        ) {
                            filteredEvents?.forEach { evento ->
                                DropdownMenuItem(
                                    text = { Text(text = evento.nombre, color = Color.White) },
                                    onClick = {
                                        selectedEventName = evento.nombre
                                        selectedEvent = evento
                                        expandedEvent = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (selectedType) {
                                                "Entrenamiento" -> Icons.Default.FitnessCenter
                                                "Nutrición" -> Icons.Default.Restaurant
                                                "Progreso" -> Icons.Default.TrendingUp
                                                "Salud" -> Icons.Default.Favorite
                                                else -> Icons.Default.Event
                                            },
                                            contentDescription = selectedType,
                                            tint = Color(0xFFAB47BC)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Time Selector - MEJORADO
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hora",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically // Alineación vertical central
                    ) {
                        // Hour selector (formato 24 horas)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp) // Altura fija para uniformidad
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF252525)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedHour > 0) selectedHour-- else selectedHour = 23
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Disminuir hora",
                                        tint = Color(0xFFAB47BC)
                                    )
                                }

                                Text(
                                    text = String.format("%02d", selectedHour),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                IconButton(
                                    onClick = {
                                        if (selectedHour < 23) selectedHour++ else selectedHour = 0
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar hora",
                                        tint = Color(0xFFAB47BC)
                                    )
                                }
                            }
                        }

                        // Dos puntos centrados verticalmente
                        Text(
                            text = ":",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .wrapContentHeight() // Para centrar verticalmente
                        )

                        // Minute selector
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp) // Altura fija para uniformidad
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF252525)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedMinute > 0) selectedMinute -= 5 else selectedMinute = 55
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Disminuir minutos",
                                        tint = Color(0xFFAB47BC)
                                    )
                                }

                                Text(
                                    text = String.format("%02d", selectedMinute),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                IconButton(
                                    onClick = {
                                        if (selectedMinute < 55) selectedMinute += 5 else selectedMinute = 0
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar minutos",
                                        tint = Color(0xFFAB47BC)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nuevo campo de notas
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Notas",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        placeholder = { Text("Escribe notas sobre este evento...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // Altura fija para el campo de notas
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFF252525),
                            cursorColor = Color(0xFFAB47BC),
                            focusedBorderColor = Color(0xFFAB47BC),
                            unfocusedBorderColor = Color(0xFF3A3A3A)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                if (isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botón Eliminar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF5252),
                                            Color(0xFF8B0000)
                                        ),
                                        start = Offset.Infinite,
                                        end = Offset.Zero
                                    )
                                )
                                .clickable {
                                    onDelete?.invoke()
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp) // Este tamaño real del icono
                            )
                        }


                        // Espaciador entre botones
                        Spacer(modifier = Modifier.width(8.dp))

                        // Botones de Cancelar y Editar
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF3F51B5),
                                                Color(0xFF9FA8DA)
                                            ),
                                            center = Offset(0.5f, 0.5f),
                                            radius = 200f
                                        )
                                    )
                                    .clickable {
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp) // Este tamaño real del icono
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF4A148C),
                                                Color(0xFFd260e6)
                                            ),
                                            start = Offset(200f, 0f),
                                            end = Offset(0f, 200f)
                                        )
                                    )
                                    .clickable {
                                        val formattedHour = String.format(
                                            "%02d:%02d",
                                            selectedHour,
                                            selectedMinute
                                        )
                                        eventoUsuarioExistente?._id?.let { id ->
                                            onEdit?.invoke(id, formattedHour, notas)
                                        }
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp) // Este tamaño real del icono
                                )
                            }
                        }
                    }
                } else {
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
                                val formattedHour = String.format(
                                    "%02d:%02d",
                                    selectedHour,
                                    selectedMinute
                                )
                                onConfirm(selectedEventName ?: "", formattedHour, notas)
                                eventosUsuarioViewModel.new(
                                    EventosUsuario(
                                        evento = selectedEvent!!._id,
                                        usuario = eventosViewModel.usuario.value!!._id,
                                        fecha = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                        hora = formattedHour,
                                        notas = notas
                                    )
                                )
                            },
                            enabled = selectedEventName != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFAB47BC),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF7B1FA2).copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Guardar", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}