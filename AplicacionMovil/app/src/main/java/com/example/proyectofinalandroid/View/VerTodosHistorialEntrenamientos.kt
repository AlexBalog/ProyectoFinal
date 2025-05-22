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
import androidx.compose.material.icons.outlined.FitnessCenter
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.ViewModel.EntrenamientoRealizadoViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import com.example.proyectofinalandroid.utils.getImageBitmapSafely
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Constantes globales para evitar recrear objetos
private val CATEGORIAS_PREDEFINIDAS = listOf("Fuerza", "Hipertrofia", "Resistencia", "Cardio", "HIIT", "Full Body")
private val MUSCULOS_PREDEFINIDOS = listOf(
    "Pecho", "Espalda", "Hombros", "Bíceps", "Tríceps", "Cuádriceps",
    "Isquiotibiales", "Glúteos", "Abdominales", "Antebrazos", "Gemelos", "Trapecio", "Lumbar"
)
private val FORMATO_FECHA = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val FORMATO_FECHA_CABECERA = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
private val FORMATO_HORA = SimpleDateFormat("HH:mm", Locale.getDefault())

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEntrenamientosRealizadosScreen(
    navController: NavController
) {
    // Obtener ViewModels de forma estable
    val userEntry = remember { navController.getBackStackEntry("root") }
    val mainEntry = remember { navController.getBackStackEntry("main") }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(mainEntry)
    val entrenamientoRealizadoViewModel: EntrenamientoRealizadoViewModel = hiltViewModel()

    // Estados
    val usuario by usuariosViewModel.usuario.collectAsState()
    val scope = rememberCoroutineScope()

    // Filtros (uso de immutable state para reducir recomposiciones)
    val filtrosState = remember { FiltrosState() }
    val (filtros, actualizarFiltros) = filtrosState

    // Datos de entrenamientos (StateFlow en ViewModel es eficiente)
    val entrenamientosRealizados by entrenamientoRealizadoViewModel.entrenamientoRealizado.collectAsState(initial = emptyList())

    // Cache de información de entrenamientos para evitar múltiples llamadas a la API
    val infoEntrenamientosCache = remember { mutableStateMapOf<String, Entrenamientos>() }

    // Para la vista de calendario
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Animación de entrada (solo se ejecuta una vez)
    var isAnimatedIn by remember { mutableStateOf(false) }

    // Modelo para las estadísticas (separado para reducir recomposiciones)
    val estadisticasState = remember { EstadisticasState() }

    // Cargar datos (solo una vez)
    LaunchedEffect(Unit) {
        delay(300) // Pequeña demora para la animación inicial
        usuario?.let { currentUser ->
            entrenamientoRealizadoViewModel.getEntrenamientosRealizadosByUsuario(currentUser)
        }
        isAnimatedIn = true
    }

    // Entrenamientos filtrados (calculados solo cuando cambian los filtros o datos)
    val entrenamientosFiltrados by produceState(
        initialValue = emptyList<EntrenamientoRealizado>(),
        key1 = entrenamientosRealizados,
        key2 = filtros
    ) {
        filtrosState.setLoading(true)

        // Aplicamos filtros
        val listaFiltrada = entrenamientosRealizados?.filter { entrenamiento ->
            var pasaFiltro = true

            // Obtenemos info del entrenamiento bajo demanda y lo cacheamos
            val infoEntrenamiento = infoEntrenamientosCache.getOrPut(entrenamiento.entrenamiento) {
                try {
                    entrenamientosViewModel.getEntrenamientoById(entrenamiento.entrenamiento) ?: return@getOrPut Entrenamientos(
                        _id = "",
                        nombre = "Desconocido",
                        categoria = "",
                        musculoPrincipal = "",
                        duracion = 0,
                        foto = "",
                        musculo = emptyList(),
                        likes = 0,
                        ejercicios = emptyList(),
                        creador = "",
                        aprobado = false,
                        pedido = false,
                        motivoRechazo = ""
                    )
                } catch (e: Exception) {
                    return@getOrPut Entrenamientos(
                        _id = "",
                        nombre = "Error",
                        categoria = "",
                        musculoPrincipal = "",
                        duracion = 0,
                        foto = "",
                        musculo = emptyList(),
                        likes = 0,
                        ejercicios = emptyList(),
                        creador = "",
                        aprobado = false,
                        pedido = false,
                        motivoRechazo = ""
                    )
                }
            }

            // Filtro por texto (en nombre de entrenamiento)
            if (filtros.texto.isNotEmpty()) {
                pasaFiltro = pasaFiltro && infoEntrenamiento.nombre.contains(filtros.texto, ignoreCase = true)
            }

            // Filtro por categoría
            if (filtros.categoriaSeleccionada != null) {
                pasaFiltro = pasaFiltro && infoEntrenamiento.categoria == filtros.categoriaSeleccionada
            }

            // Filtro por músculo
            if (filtros.musculoSeleccionado != null) {
                pasaFiltro = pasaFiltro && infoEntrenamiento.musculo.contains(filtros.musculoSeleccionado)
            }

            // Filtro por periodo de tiempo
            val fechaActual = Calendar.getInstance().time
            val fechaEntrenamiento = entrenamiento.fecha
            val diferenciaDias = ((fechaActual.time - fechaEntrenamiento.time) / (1000 * 60 * 60 * 24)).toInt()

            pasaFiltro = pasaFiltro && when (filtros.periodo) {
                "semana" -> diferenciaDias <= 7
                "mes" -> diferenciaDias <= 30
                "anyo" -> diferenciaDias <= 365
                else -> true // "todo"
            }

            pasaFiltro
        } ?: emptyList()

        // Actualizamos la lista filtrada (utilizando sortedByDescending solo una vez)
        value = listaFiltrada.sortedByDescending { it.fecha }
        filtrosState.setLoading(false)
        filtrosState.setFiltering(
            filtros.texto.isNotEmpty() ||
                    filtros.categoriaSeleccionada != null ||
                    filtros.musculoSeleccionado != null ||
                    filtros.periodo != "todo"
        )
    }

    // Calcular estadísticas cuando cambian los entrenamientos filtrados
    LaunchedEffect(entrenamientosFiltrados) {
        estadisticasState.actualizarEstadisticas(
            entrenamientosFiltrados,
            infoEntrenamientosCache
        )
    }

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fondo con efecto de partículas (optimizado)
        if (isAnimatedIn) {
            ParticlesBackground()
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {
            // Barra superior (callbacks estables)
            TopBar(
                navController = navController,
                mostrarFiltros = filtros.mostrarPanel,
                onToggleFiltros = remember { { filtrosState.togglePanel() } },
                onCambiarVista = remember { { filtrosState.setVista(it) } }
            )

            // Panel de filtros (animado)
            AnimatedVisibility(
                visible = filtros.mostrarPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FiltrosPanel(
                    filtros = filtros,
                    actualizarFiltros = actualizarFiltros
                )
            }

            // Estadísticas de resumen (siempre visible, optimizada)
            ResumenEstadisticas(
                entrenamientos = entrenamientosFiltrados,
                isFiltering = filtros.isFiltering
            )

            // Contenido principal según la vista seleccionada
            when (filtros.vista) {
                "lista" -> ListaEntrenamientos(
                    entrenamientos = entrenamientosFiltrados,
                    infoEntrenamientosCache = infoEntrenamientosCache,
                    entrenamientosViewModel = entrenamientosViewModel,
                    isLoading = filtros.isLoading,
                    navController = navController,
                    isAnimatedIn = isAnimatedIn
                )
                "calendario" -> CalendarioEntrenamientos(
                    entrenamientos = entrenamientosFiltrados,
                    infoEntrenamientosCache = infoEntrenamientosCache,
                    entrenamientosViewModel = entrenamientosViewModel,
                    navController = navController,
                    selectedDate = selectedDate,
                    currentMonth = currentMonth,
                    onDateSelected = remember { { selectedDate = it } },
                    onMonthChange = remember { { currentMonth = it } }
                )
                "estadisticas" -> EstadisticasEntrenamientos(
                    estadisticasState = estadisticasState
                )
            }
        }
    }
}

// Clase para manejar el estado de filtros de forma eficiente
class FiltrosState {
    // Estado de filtros inmutable para reducir recomposiciones
    private val _filtros = mutableStateOf(
        Filtros(
            texto = "",
            categoriaSeleccionada = null,
            musculoSeleccionado = null,
            periodo = "todo",
            seleccionado = "categoria",
            mostrarPanel = false,
            vista = "lista",
            isLoading = true,
            isFiltering = false
        )
    )

    val filtros: Filtros get() = _filtros.value

    // Función actualizadora para batches de cambios
    val actualizarFiltros: (Filtros) -> Unit = { newFiltros ->
        _filtros.value = newFiltros
    }

    fun setLoading(isLoading: Boolean) {
        _filtros.value = _filtros.value.copy(isLoading = isLoading)
    }

    fun setFiltering(isFiltering: Boolean) {
        _filtros.value = _filtros.value.copy(isFiltering = isFiltering)
    }

    fun togglePanel() {
        _filtros.value = _filtros.value.copy(mostrarPanel = !_filtros.value.mostrarPanel)
    }

    fun setVista(vista: String) {
        _filtros.value = _filtros.value.copy(vista = vista)
    }

    operator fun component1() = filtros
    operator fun component2() = actualizarFiltros
}

// Clase de datos inmutable para los filtros
data class Filtros(
    val texto: String,
    val categoriaSeleccionada: String?,
    val musculoSeleccionado: String?,
    val periodo: String,
    val seleccionado: String,
    val mostrarPanel: Boolean,
    val vista: String,
    val isLoading: Boolean,
    val isFiltering: Boolean
)

// Clase para manejar estadísticas
class EstadisticasState {
    var categoriaEntrenamientos by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var musculosEntrenamientos by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var entrenamientosPorSemana by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set

    // Actualizar estadísticas en batch
    suspend fun actualizarEstadisticas(
        entrenamientos: List<EntrenamientoRealizado>,
        infoEntrenamientos: Map<String, Entrenamientos>
    ) {
        // Categorías
        val categorias = mutableMapOf<String, Int>()
        val musculos = mutableMapOf<String, Int>()

        // Procesar entrenamientos (ahora usando el cache)
        entrenamientos.forEach { entrenamiento ->
            val infoEntrenamiento = infoEntrenamientos[entrenamiento.entrenamiento] ?: return@forEach

            // Contar categorías
            categorias[infoEntrenamiento.categoria] = categorias.getOrDefault(infoEntrenamiento.categoria, 0) + 1

            // Contar músculos
            infoEntrenamiento.musculo.forEach { musculo ->
                musculos[musculo] = musculos.getOrDefault(musculo, 0) + 1
            }
        }

        // Ordenar categorías por frecuencia
        categoriaEntrenamientos = categorias.toList()
            .sortedByDescending { it.second }
            .toMap()

        // Ordenar músculos por frecuencia
        musculosEntrenamientos = musculos.toList()
            .sortedByDescending { it.second }
            .toMap()

        // Calcular entrenamientos por semana (últimas 4 semanas)
        val fechaActual = Calendar.getInstance()
        val semanas = mutableMapOf<Int, Int>()

        entrenamientos.forEach { entrenamiento ->
            val fechaEntrenamiento = Calendar.getInstance()
            fechaEntrenamiento.time = entrenamiento.fecha

            // Calcular diferencia en semanas
            val diff = ((fechaActual.timeInMillis - fechaEntrenamiento.timeInMillis) / (1000 * 60 * 60 * 24 * 7)).toInt()

            if (diff < 4) {
                semanas[diff] = semanas.getOrDefault(diff, 0) + 1
            }
        }

        entrenamientosPorSemana = semanas
    }
}

// Composable optimizado para las partículas de fondo
@Composable
fun ParticlesBackground() {
    val density = LocalDensity.current
    val canvasSize = remember { mutableStateOf(Size.Zero) }

    // Crear partículas solo una vez
    val particles = remember {
        mutableListOf<Particle>().apply {
            repeat(30) {
                add(Particle(
                    position = Offset(Math.random().toFloat() * 1000, Math.random().toFloat() * 2000),
                    velocity = Offset(
                        (Math.random().toFloat() - 0.5f) * 3f,
                        (Math.random().toFloat() - 0.5f) * 3f
                    ),
                    radius = (Math.random().toFloat() * 4f + 1f),
                    alpha = (Math.random().toFloat() * 0.3f + 0.1f)
                ))
            }
        }
    }

    // Estado mutable para actualizar posiciones
    val posiciones = remember { mutableStateListOf<Offset>() }
    posiciones.addAll(particles.map { it.position })

    // Animación de partículas más eficiente
    LaunchedEffect(Unit) {
        while (true) {
            particles.forEachIndexed { index, particle ->
                // Solo actualizar la posición, no todo el objeto
                val newPos = Offset(
                    posiciones[index].x + particle.velocity.x,
                    posiciones[index].y + particle.velocity.y
                )

                // Rebote en los bordes
                val size = canvasSize.value
                if (size.width > 0 && size.height > 0) {
                    var newVelX = particle.velocity.x
                    var newVelY = particle.velocity.y

                    if (newPos.x < 0 || newPos.x > size.width) newVelX = -particle.velocity.x
                    if (newPos.y < 0 || newPos.y > size.height) newVelY = -particle.velocity.y

                    if (newVelX != particle.velocity.x || newVelY != particle.velocity.y) {
                        particles[index] = particle.copy(velocity = Offset(newVelX, newVelY))
                    }
                }

                // Actualizar posición
                posiciones[index] = newPos
            }
            delay(16) // aproximadamente 60 FPS
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.5f)
            .onSizeChanged { size ->
                canvasSize.value = Size(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        particles.forEachIndexed { index, particle ->
            drawCircle(
                color = Color(0xFFAB47BC).copy(alpha = particle.alpha),
                radius = particle.radius.dp.toPx(),
                center = posiciones[index]
            )
        }
    }
}

data class Particle(
    val position: Offset,
    val velocity: Offset,
    val radius: Float,
    val alpha: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    mostrarFiltros: Boolean,
    onToggleFiltros: () -> Unit,
    onCambiarVista: (String) -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Historial de Entrenamientos",
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
            // Botón de filtros
            IconButton(onClick = onToggleFiltros) {
                Icon(
                    imageVector = if (mostrarFiltros) Icons.Default.FilterAlt else Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (mostrarFiltros) Color(0xFFAB47BC) else Color.White
                )
            }

            // Menú de cambio de vista
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.ViewModule,
                    contentDescription = "Cambiar vista",
                    tint = Color(0xFFAB47BC)
                )
            }

            // Menú desplegable
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1A1A1A))
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = Color(0xFFAB47BC),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lista", color = Color.White)
                        }
                    },
                    onClick = {
                        onCambiarVista("lista")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFFAB47BC),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calendario", color = Color.White)
                        }
                    },
                    onClick = {
                        onCambiarVista("calendario")
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = Color(0xFFAB47BC),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Estadísticas", color = Color.White)
                        }
                    },
                    onClick = {
                        onCambiarVista("estadisticas")
                        expanded = false
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosPanel(
    filtros: Filtros,
    actualizarFiltros: (Filtros) -> Unit
) {
    // CORRECCIÓN: Agregar filtros como dependencia en remember o usar callbacks más simples
    val onFiltroTextoChange = remember(filtros) { { texto: String ->
        actualizarFiltros(filtros.copy(texto = texto))
    } }

    val onFiltroCategoriaChange = remember(filtros) { { categoria: String? ->
        actualizarFiltros(filtros.copy(categoriaSeleccionada = categoria))
    } }

    val onFiltroMusculoChange = remember(filtros) { { musculo: String? ->
        actualizarFiltros(filtros.copy(musculoSeleccionado = musculo))
    } }

    val onFiltroPeriodoChange = remember(filtros) { { periodo: String ->
        actualizarFiltros(filtros.copy(periodo = periodo))
    } }

    val onFiltroSeleccionadoChange = remember(filtros) { { seleccionado: String ->
        actualizarFiltros(filtros.copy(seleccionado = seleccionado))
    } }

    val onLimpiarFiltros = remember(filtros) { {
        actualizarFiltros(filtros.copy(
            texto = "",
            categoriaSeleccionada = null,
            musculoSeleccionado = null,
            periodo = "todo"
        ))
    } }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título y botón para limpiar filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(
                    onClick = onLimpiarFiltros,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFAB47BC)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Limpiar filtros",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de búsqueda
            OutlinedTextField(
                value = filtros.texto,
                onValueChange = onFiltroTextoChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = Color(0xFFAB47BC),
                    focusedBorderColor = Color(0xFFAB47BC),
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    containerColor = Color(0xFF252525)
                ),
                placeholder = { Text("Buscar por nombre...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFFAB47BC)
                    )
                },
                trailingIcon = {
                    if (filtros.texto.isNotEmpty()) {
                        IconButton(onClick = { onFiltroTextoChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                maxLines = 1,
                textStyle = TextStyle(color = Color.White),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector tipo de filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterTypeButton(
                    text = "Categorías",
                    selected = filtros.seleccionado == "categoria",
                    onClick = { onFiltroSeleccionadoChange("categoria") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                FilterTypeButton(
                    text = "Músculos",
                    selected = filtros.seleccionado == "musculo",
                    onClick = { onFiltroSeleccionadoChange("musculo") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar filtros según tipo seleccionado
            when (filtros.seleccionado) {
                "categoria" -> {
                    // Selector de categoría
                    Text(
                        text = "Categoría:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opción "Todas"
                        item(key = "todas-categorias") {
                            CategoryChip(
                                text = "Todas",
                                selected = filtros.categoriaSeleccionada == null,
                                onClick = { onFiltroCategoriaChange(null) }
                            )
                        }

                        // Categorías disponibles
                        items(
                            items = CATEGORIAS_PREDEFINIDAS,
                            key = { "categoria-$it" }
                        ) { categoria ->
                            CategoryChip(
                                text = categoria,
                                selected = filtros.categoriaSeleccionada == categoria,
                                onClick = { onFiltroCategoriaChange(categoria) }
                            )
                        }
                    }
                }
                "musculo" -> {
                    // Selector de músculo
                    Text(
                        text = "Músculo principal:",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Opción "Todos"
                        item(key = "todos-musculos") {
                            CategoryChip(
                                text = "Todos",
                                selected = filtros.musculoSeleccionado == null,
                                onClick = { onFiltroMusculoChange(null) }
                            )
                        }

                        // Músculos disponibles
                        items(
                            items = MUSCULOS_PREDEFINIDOS,
                            key = { "musculo-$it" }
                        ) { musculo ->
                            CategoryChip(
                                text = musculo,
                                selected = filtros.musculoSeleccionado == musculo,
                                onClick = { onFiltroMusculoChange(musculo) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de periodo
            Text(
                text = "Periodo:",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periodos = listOf(
                    "todo" to "Todo",
                    "semana" to "Semana",
                    "mes" to "Mes",
                    "anyo" to "Año"
                )

                periodos.forEach { (value, text) ->
                    CategoryChip(
                        text = text,
                        selected = filtros.periodo == value,
                        onClick = { onFiltroPeriodoChange(value) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF7B1FA2) else Color(0xFF252525),
            contentColor = if (selected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (selected) Color(0xFF7B1FA2) else Color(0xFF252525),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (selected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ResumenEstadisticas(
    entrenamientos: List<EntrenamientoRealizado>,
    isFiltering: Boolean
) {
    // Usar derivedStateOf para calcular estadísticas solo cuando cambian los datos relevantes
    val estadisticas by remember(entrenamientos) {
        derivedStateOf {
            // Calcular estadísticas
            val totalEntrenamientos = entrenamientos.size

            // Tiempo total de entrenamiento (formato HH:mm:ss)
            val tiempoTotal = entrenamientos.sumOf {
                val partes = it.duracion.split(":")
                val horas = partes[0].toInt()
                val minutos = if (partes.size > 1) partes[1].toInt() else 0
                val segundos = if (partes.size > 2) partes[2].toInt() else 0

                horas * 3600 + minutos * 60 + segundos
            }

            val horasTotales = tiempoTotal / 3600
            val minutosTotales = (tiempoTotal % 3600) / 60

            val tiempoFormateado = when {
                horasTotales > 0 -> "$horasTotales h $minutosTotales min"
                else -> "$minutosTotales min"
            }

            // Promedio de entrenamientos por semana (últimas 4 semanas)
            val fechaActual = Calendar.getInstance().time
            val fechaHaceCuatroSemanas = Calendar.getInstance().apply {
                time = fechaActual
                add(Calendar.DAY_OF_YEAR, -28)
            }.time

            val entrenamientosRecientes = entrenamientos.filter {
                it.fecha.after(fechaHaceCuatroSemanas)
            }

            val promedioSemanal = if (entrenamientosRecientes.isNotEmpty()) {
                (entrenamientosRecientes.size / 4.0).roundToInt()
            } else {
                0
            }

            Triple(totalEntrenamientos, tiempoFormateado, promedioSemanal)
        }
    }

    // Extraer valores una vez calculados
    val (totalEntrenamientos, tiempoFormateado, promedioSemanal) = estadisticas

    // UI
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
            // Total de entrenamientos
            StatItem(
                icon = Icons.Default.FitnessCenter,
                value = totalEntrenamientos.toString(),
                label = if (isFiltering) "Entrenamientos filtrados" else "Total de entrenamientos",
                modifier = Modifier.weight(1f)
            )

            // Tiempo total
            StatItem(
                icon = Icons.Default.Timer,
                value = tiempoFormateado,
                label = "Tiempo total",
                modifier = Modifier.weight(1f)
            )

            // Promedio semanal
            StatItem(
                icon = Icons.Default.CalendarToday,
                value = promedioSemanal.toString(),
                label = "Promedio semanal",
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
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
fun ListaEntrenamientos(
    entrenamientos: List<EntrenamientoRealizado>,
    infoEntrenamientosCache: Map<String, Entrenamientos>,
    entrenamientosViewModel: EntrenamientosViewModel,
    isLoading: Boolean,
    navController: NavController,
    isAnimatedIn: Boolean
) {
    // Agrupar entrenamientos por fecha (optimizado con derivedStateOf)
    val entrenamientosAgrupados by remember(entrenamientos) {
        derivedStateOf {
            // Crear mapa optimizado
            val mapa = mutableMapOf<String, List<EntrenamientoRealizado>>()

            // Agrupar por fecha (una sola iteración)
            entrenamientos.forEach { entrenamiento ->
                val fechaKey = FORMATO_FECHA.format(entrenamiento.fecha)
                mapa[fechaKey] = (mapa[fechaKey] ?: emptyList()) + entrenamiento
            }

            // Ordenar claves una sola vez
            mapa.entries.sortedByDescending { it.key }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFAB47BC)
            )
        } else if (entrenamientos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentDissatisfied,
                    contentDescription = "Sin resultados",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No se encontraron entrenamientos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Prueba a cambiar los filtros o realiza tu primer entrenamiento",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Button(
                    onClick = { navController.navigate("buscador") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Realizar entrenamiento"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Realizar entrenamiento")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                entrenamientosAgrupados.forEach { (fechaKey, entrenamientosDia) ->
                    // Cabecera de fecha (reutilizar formato de fecha)
                    item(key = "fecha-$fechaKey") {
                        val fechaDate = FORMATO_FECHA.parse(fechaKey)
                        val fechaFormateada = if (fechaDate != null) {
                            FORMATO_FECHA_CABECERA.format(fechaDate)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        } else {
                            fechaKey
                        }

                        Text(
                            text = fechaFormateada,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFAB47BC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        )
                    }

                    // Entrenamientos del día
                    items(
                        items = entrenamientosDia,
                        key = { it._id }
                    ) { entrenamiento ->


                        EntrenamientoItemOptimizado(
                            entrenamientoRealizado = entrenamiento,
                            infoEntrenamientosCache = infoEntrenamientosCache,
                            entrenamientosViewModel = entrenamientosViewModel,
                            onEntrenamientoClick = remember(entrenamiento._id) { {
                                navController.navigate("detalleEntrenamientoRealizado/${entrenamiento._id}")
                            } },
                        )
                    }
                }

                // Espacio al final para el FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun EntrenamientoItemOptimizado(
    entrenamientoRealizado: EntrenamientoRealizado,
    infoEntrenamientosCache: Map<String, Entrenamientos>,
    entrenamientosViewModel: EntrenamientosViewModel,
    onEntrenamientoClick: () -> Unit
) {
    // Usar estados derivados para evitar recomposiciones innecesarias
    val infoEntrenamiento by produceState<Entrenamientos?>(
        initialValue = infoEntrenamientosCache[entrenamientoRealizado.entrenamiento],
        key1 = entrenamientoRealizado.entrenamiento
    ) {
        // Si no está en caché, cargarlo
        if (value == null) {
            value = try {
                entrenamientosViewModel.getEntrenamientoById(entrenamientoRealizado.entrenamiento)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Extraer información del entrenamiento (solo si está disponible)
    val nombreEntrenamiento = infoEntrenamiento?.nombre ?: "Cargando..."
    val categoriaEntrenamiento = infoEntrenamiento?.categoria ?: ""
    val fotoEntrenamiento = infoEntrenamiento?.foto ?: ""

    // Formatear hora (una sola vez)
    val horaFormateada = remember(entrenamientoRealizado.fecha) {
        FORMATO_HORA.format(entrenamientoRealizado.fecha)
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
            .clickable(onClick = onEntrenamientoClick),
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
            // Imagen o icono del entrenamiento (optimizado)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                if (fotoEntrenamiento.isNotEmpty()) {
                    // Usar imágenes bitmap de manera eficiente
                    val bitmap = getImageBitmapSafely(fotoEntrenamiento)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Imagen de entrenamiento",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DefaultTrainingIcon()
                    }
                } else {
                    DefaultTrainingIcon()
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del entrenamiento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombreEntrenamiento,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Categoría (solo mostrar si disponible)
                if (categoriaEntrenamiento.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF7B1FA2).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = categoriaEntrenamiento,
                            fontSize = 12.sp,
                            color = Color(0xFFAB47BC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hora y duración
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
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

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Duración",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = entrenamientoRealizado.duracion,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Indicador para ver detalle
            Icon(
                imageVector = Icons.Default.NavigateNext,
                contentDescription = "Ver detalle",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DefaultTrainingIcon() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = "Entrenamiento",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(32.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioEntrenamientos(
    entrenamientos: List<EntrenamientoRealizado>,
    infoEntrenamientosCache: Map<String, Entrenamientos>,
    entrenamientosViewModel: EntrenamientosViewModel,
    navController: NavController,
    selectedDate: LocalDate?,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate?) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    // Calcular entrenamientos por fecha de forma eficiente
    val entrenamientosPorFecha by remember(entrenamientos) {
        derivedStateOf {
            val map = mutableMapOf<LocalDate, List<EntrenamientoRealizado>>()
            entrenamientos.forEach { entrenamiento ->
                val fecha = entrenamiento.fecha.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                map[fecha] = (map[fecha] ?: emptyList()) + entrenamiento
            }
            map
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Encabezado del calendario
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mes anterior",
                    tint = Color(0xFFAB47BC)
                )
            }

            Text(
                text = "${currentMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Mes siguiente",
                    tint = Color(0xFFAB47BC)
                )
            }
        }

        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach { day ->
                Text(
                    text = day,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cuadrícula del calendario
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Lunes, 7 = Domingo
        val lastDayOfMonth = currentMonth.atEndOfMonth().dayOfMonth

        val rows = (lastDayOfMonth + firstDayOfWeek - 1 + 6) / 7 // Número de filas necesarias

        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 1..7) {
                        val day = row * 7 + col - firstDayOfWeek + 1
                        if (day in 1..lastDayOfMonth) {
                            val date = currentMonth.atDay(day)
                            val hasEntrenamientos = entrenamientosPorFecha.containsKey(date)
                            val isSelected = selectedDate == date

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> Color(0xFF7B1FA2)
                                            hasEntrenamientos -> Color(0xFF7B1FA2).copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color(0xFFAB47BC) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            onDateSelected(null)
                                        } else {
                                            onDateSelected(date)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = when {
                                        isSelected -> Color.White
                                        hasEntrenamientos -> Color(0xFFAB47BC)
                                        else -> Color.White
                                    },
                                    fontWeight = if (hasEntrenamientos || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            // Espacio en blanco para días fuera del mes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar entrenamientos del día seleccionado
        selectedDate?.let { date ->
            val entrenamientosDia = entrenamientosPorFecha[date] ?: emptyList()

            Text(
                text = "Entrenamientos del ${date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (entrenamientosDia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay entrenamientos para este día",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = entrenamientosDia,
                        key = { it._id }
                    ) { entrenamiento ->
                        EntrenamientoItemOptimizado(
                            entrenamientoRealizado = entrenamiento,
                            infoEntrenamientosCache = infoEntrenamientosCache,
                            entrenamientosViewModel = entrenamientosViewModel,
                            onEntrenamientoClick = remember(entrenamiento._id) { {
                                navController.navigate("detalleEntrenamientoRealizado/${entrenamiento._id}")
                            } }
                        )
                    }
                }
            }
        } ?: run {
            // Si no hay fecha seleccionada, mostrar mensaje informativo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selecciona un día para ver los entrenamientos",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EstadisticasEntrenamientos(
    estadisticasState: EstadisticasState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título
        Text(
            text = "Estadísticas de Entrenamiento",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Gráfico de Categorías
        if (estadisticasState.categoriaEntrenamientos.isNotEmpty()) {
            StatisticsCard(
                title = "Entrenamientos por Categoría",
                icon = Icons.Default.Category
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    estadisticasState.categoriaEntrenamientos.entries.take(5).forEach { (categoria, count) ->
                        BarChartItem(
                            label = categoria,
                            value = count,
                            maxValue = estadisticasState.categoriaEntrenamientos.values.maxOrNull() ?: 1
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de Músculos
        if (estadisticasState.musculosEntrenamientos.isNotEmpty()) {
            StatisticsCard(
                title = "Músculos más Trabajados",
                icon = Icons.Outlined.FitnessCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    estadisticasState.musculosEntrenamientos.entries.take(5).forEach { (musculo, count) ->
                        BarChartItem(
                            label = musculo,
                            value = count,
                            maxValue = estadisticasState.musculosEntrenamientos.values.maxOrNull() ?: 1,
                            color = Color(0xFF6A1B9A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de Entrenamientos por Semana
        StatisticsCard(
            title = "Entrenamientos Recientes",
            icon = Icons.Default.Timeline
        ) {
            val semanas = listOf("Esta semana", "Hace 1 semana", "Hace 2 semanas", "Hace 3 semanas")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                for (i in 0 until 4) {
                    val count = estadisticasState.entrenamientosPorSemana.getOrDefault(i, 0)

                    BarChartItem(
                        label = semanas[i],
                        value = count,
                        maxValue = estadisticasState.entrenamientosPorSemana.values.maxOrNull()?.coerceAtLeast(1) ?: 1,
                        color = Color(0xFF00897B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Espacio al final
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatisticsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Separador
            Divider(color = Color(0xFF333333))

            // Contenido
            content()
        }
    }
}

@Composable
fun BarChartItem(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color = Color(0xFFAB47BC)
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Etiqueta y valor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Barra de progreso
        val progress = if (maxValue > 0) value.toFloat() / maxValue else 0f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF333333))
        ) {
            // Animación para la barra de progreso
            val animatedProgress = animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }
    }
}