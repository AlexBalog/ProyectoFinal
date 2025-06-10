package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import com.example.proyectofinalandroid.Model.Mediciones
import com.example.proyectofinalandroid.Model.TipoMedicion
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.MedicionesViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.util.Log

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialMedicionesScreen(
    navController: NavController
) {
    // Obtener ViewModels de forma estable
    val userEntry = remember { navController.getBackStackEntry("root") }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val medicionesViewModel: MedicionesViewModel = hiltViewModel()

    // Estados
    val usuario by usuariosViewModel.usuario.collectAsState()
    val scope = rememberCoroutineScope()
    val mediciones by medicionesViewModel.mediciones.collectAsState()
    val estadisticas by medicionesViewModel.estadisticas.collectAsState()
    val isLoading by medicionesViewModel.isLoading.collectAsState()

    // Estado para diálogo
    var showNuevaMedicionDialog by remember { mutableStateOf(false) }
    var selectedMedicion by remember { mutableStateOf<Mediciones?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // Estado para filtros
    var filtroFecha by remember { mutableStateOf("todo") } // "todo", "semana", "mes", "anyo"
    var isAnimatedIn by remember { mutableStateOf(false) }

    // Cargar datos de forma segura
    LaunchedEffect(usuario) {
        usuario?.let { currentUser ->
            try {
                medicionesViewModel.setUsuario(currentUser)
                medicionesViewModel.cargarMedicionesPorUsuario(tipo = TipoMedicion.PESO)
                medicionesViewModel.cargarEstadisticas(TipoMedicion.PESO)
                delay(300)
                isAnimatedIn = true
            } catch (e: Exception) {
                Log.e("HistorialMediciones", "Error al cargar datos: ${e.message}")
            }
        }
    }

    // Filtrar mediciones por fecha de forma segura
    val medicionesFiltradas = remember(mediciones, filtroFecha) {
        try {
            val currentDate = LocalDate.now()
            mediciones?.filter { medicion ->
                // Verificar que la medición no sea nula y tenga el tipo correcto
                if (medicion?.tipo != TipoMedicion.PESO.name) return@filter false

                val medicionDate = medicion.fecha?.let { fecha ->
                    try {
                        fecha.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    } catch (e: Exception) {
                        Log.e("HistorialMediciones", "Error al parsear fecha: ${e.message}")
                        null
                    }
                } ?: return@filter false

                when (filtroFecha) {
                    "semana" -> ChronoUnit.DAYS.between(medicionDate, currentDate) <= 7
                    "mes" -> ChronoUnit.DAYS.between(medicionDate, currentDate) <= 30
                    "anyo" -> ChronoUnit.DAYS.between(medicionDate, currentDate) <= 365
                    else -> true // "todo"
                }
            }?.sortedByDescending { it?.fecha } ?: emptyList()
        } catch (e: Exception) {
            Log.e("HistorialMediciones", "Error al filtrar mediciones: ${e.message}")
            emptyList()
        }
    }

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fondo con efecto de partículas
        if (isAnimatedIn) {
            ParticlesBackground()
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {
            // Barra superior
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Peso",
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
                ),
                actions = {
                    // Botón para agregar nueva medición
                    if (usuario!!.formulario == true) {
                        IconButton(onClick = { showNuevaMedicionDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar medición",
                                tint = Color(0xFFAB47BC)
                            )
                        }
                    }
                }
            )

            // Filtros de período
            PeriodoFilterChips(
                selectedPeriodo = filtroFecha,
                onPeriodoSelected = { filtroFecha = it }
            )

            // Resumen de estadísticas - Solo mostrar si hay datos válidos
            if (estadisticas != null && usuario != null) {
                ResumenEstadisticasPeso(
                    estadisticas = estadisticas,
                    usuario = usuario,
                    medicion = mediciones.take(1).last()
                )
            }

            // Etiqueta "Historial de mediciones"
            Text(
                text = "Historial de mediciones (${medicionesFiltradas.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Lista de mediciones
            when {
                isLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFAB47BC))
                    }
                }
                medicionesFiltradas.isEmpty() -> {
                    if (usuario!!.formulario == true) {
                        EmptyMedicionesView {
                            showNuevaMedicionDialog = true
                        }
                    } else {
                        FormularioView(onClick = {navController.navigate("formulario")})
                    }

                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
                    ) {
                        items(
                            items = medicionesFiltradas,
                            key = { it?._id ?: UUID.randomUUID().toString() }
                        ) { medicion ->
                            medicion?.let { safeMedicion ->
                                MedicionItem(
                                    medicion = safeMedicion,
                                    onClick = {
                                        selectedMedicion = safeMedicion
                                        showDetailDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para nueva medición
    if (showNuevaMedicionDialog) {
        NuevaMedicionDialog(
            onDismiss = { showNuevaMedicionDialog = false },
            onConfirm = { valor, notas ->
                scope.launch {
                    try {
                        medicionesViewModel.registrarPeso(valor, notas)
                        showNuevaMedicionDialog = false
                    } catch (e: Exception) {
                        Log.e("HistorialMediciones", "Error al registrar peso: ${e.message}")
                    }
                }
            }
        )
    }

    // Diálogo de detalle de medición
    if (showDetailDialog && selectedMedicion != null) {
        MedicionDetailDialog(
            medicion = selectedMedicion!!,
            onDismiss = { showDetailDialog = false },
            onDelete = {
                scope.launch {
                    try {
                        selectedMedicion?.let { medicion ->
                            medicionesViewModel.eliminarMedicion(medicion._id)
                        }
                        showDetailDialog = false
                    } catch (e: Exception) {
                        Log.e("HistorialMediciones", "Error al eliminar medición: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun PeriodoFilterChips(
    selectedPeriodo: String,
    onPeriodoSelected: (String) -> Unit
) {
    val periodos = listOf(
        "todo" to "Todo",
        "semana" to "Última semana",
        "mes" to "Último mes",
        "anyo" to "Último año"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periodos.forEach { (value, text) ->
            FilterChip(
                selected = selectedPeriodo == value,
                onClick = { onPeriodoSelected(value) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF252525),
                    selectedContainerColor = Color(0xFF7B1FA2),
                    labelColor = Color.Gray,
                    selectedLabelColor = Color.White
                ),
                label = {
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 1.dp),
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ResumenEstadisticasPeso(
    estadisticas: Any?, // Reemplaza con el tipo real de tu objeto estadísticas
    usuario: Usuarios?,
    medicion: Mediciones?
) {
    // Verificaciones de seguridad
    if (estadisticas == null || usuario == null) {
        return
    }

    // Extracción segura de valores
    val pesoActual = try {
        // Aquí debes adaptar según la estructura real de tu objeto estadísticas
        // estadisticas.ultimo?.toFloat() ?: usuario.peso
        medicion?.valor // Valor por defecto
    } catch (e: Exception) {
        Log.e("ResumenEstadisticas", "Error al obtener peso actual: ${e.message}")
        usuario.peso
    }

    val pesoInicial = usuario.peso ?: 0f
    val cambio = try {
        val pesoActualSafe = pesoActual ?: 0f
        val pesoInicialSafe = pesoInicial ?: 0f
        pesoActualSafe - pesoInicialSafe
    } catch (e: Exception) {
        Log.e("ResumenEstadisticas", "Error al obtener cambio: ${e.message}")
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Peso inicial
            StatItem(
                icon = Icons.Default.Scale,
                value = String.format("%.1f kg", pesoInicial),
                label = "Peso inicial",
                modifier = Modifier.weight(1f)
            )

            // Peso actual
            StatItem(
                icon = Icons.Default.Scale,
                value = String.format("%.1f kg", pesoActual),
                label = "Peso actual",
                modifier = Modifier.weight(1f)
            )

            // Cambio
            val cambioTexto = if (cambio >= 0) "+${String.format("%.1f", cambio)}" else String.format("%.1f", cambio)
            val cambioColor = try {
                val objetivoPesoSafe = usuario.objetivoPeso ?: 0f
                val pesoUsuarioSafe = usuario.peso ?: 0f
                val pesoActualSafe = pesoActual ?: 0f
                val pesoInicialSafe = pesoInicial ?: 0f

                if (objetivoPesoSafe < pesoUsuarioSafe) {
                    if (pesoActualSafe > pesoInicialSafe)
                        Color(0xFFE57373)
                    else Color(0xFF4CAF50)
                } else {
                    if (pesoActualSafe > pesoInicialSafe)
                        Color(0xFF4CAF50)
                    else Color(0xFFE57373)
                }
            } catch (e: Exception) {
                Color.White // Color por defecto
            }

            StatItem(
                icon = Icons.Default.TrendingUp,
                value = "$cambioTexto kg",
                valueColor = cambioColor,
                label = "Cambio",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF7B1FA2).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MedicionItem(
    medicion: Mediciones,
    onClick: () -> Unit
) {
    // Verificación de seguridad
    if (medicion.fecha == null) {
        Log.w("MedicionItem", "Medición con fecha nula")
        return
    }

    // Formatear fecha de forma segura
    val (fechaFormateada, horaFormateada) = try {
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        Pair(
            dateFormatter.format(medicion.fecha),
            timeFormatter.format(medicion.fecha)
        )
    } catch (e: Exception) {
        Log.e("MedicionItem", "Error al formatear fecha: ${e.message}")
        Pair("--/--/----", "--:--")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF202020)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de peso
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B1FA2).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = "Peso",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la medición
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${medicion.valor} ${medicion.obtenerUnidad()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = fechaFormateada,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Hora",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = horaFormateada,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Notas (si hay)
                if (!medicion.notas.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Notas",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = medicion.notas,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Flecha para indicar que se puede ver detalle
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = Color(0xFFAB47BC)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicionDetailDialog(
    medicion: Mediciones,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    // Verificación de seguridad
    if (medicion.fecha == null) {
        onDismiss()
        return
    }

    // Formatear fecha de forma segura
    val (fechaFormateada, horaFormateada) = try {
        val dateFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        Pair(
            dateFormatter.format(medicion.fecha)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            timeFormatter.format(medicion.fecha)
        )
    } catch (e: Exception) {
        Log.e("MedicionDetailDialog", "Error al formatear fecha: ${e.message}")
        Pair("Fecha no disponible", "--:--")
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
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
                // Icono y título
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = "Detalle de Peso",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp)
                )

                // Valor principal
                Text(
                    text = "${medicion.valor} ${medicion.obtenerUnidad()}",
                    style = TextStyle(
                        fontSize = 32.sp,
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

                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = fechaFormateada,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Hora",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = horaFormateada,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                // Notas (si hay)
                if (!medicion.notas.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF252525)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Notas:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFAB47BC),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = medicion.notas,
                                fontSize = 16.sp,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón eliminar
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        border = BorderStroke(1.dp, Color(0xFFE57373)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE57373)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }

                    // Botón cerrar
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B1FA2)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Eliminar medición",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar esta medición? Esta acción no se puede deshacer.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE57373)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color(0xFF252525),
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }
}

@Composable
fun EmptyMedicionesView(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Scale,
            contentDescription = "Sin mediciones",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay mediciones registradas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Registra tu peso para comenzar a seguir tu progreso",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B1FA2)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar medición"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Registrar medición")
        }
    }
}


@Composable
fun FormularioView(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Scale,
            contentDescription = "Sin mediciones",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay mediciones registradas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Completa el formulario inicial para emprender el camino hacia tus sueños",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7B1FA2)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Completar Formulario"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Completar Formulario")
        }
    }
}