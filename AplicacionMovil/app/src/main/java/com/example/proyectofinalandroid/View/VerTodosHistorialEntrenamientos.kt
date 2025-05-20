package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.EntrenamientoRealizado
import com.example.proyectofinalandroid.ViewModel.EntrenamientoRealizadoViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEntrenamientosScreen(
    navController: NavController
) {
    // Obtenemos los ViewModels que necesitamos
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val mainEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(mainEntry)
    val historialViewModel: EntrenamientoRealizadoViewModel = hiltViewModel(mainEntry)

    // Obtenemos el usuario actual
    val usuario by usuariosViewModel.usuario.collectAsState()

    // Producimos el estado de la pantalla de forma centralizada
    val screenState = produceState(
        initialValue = HistorialScreenState(
            isLoading = true,
            entrenamientosRealizados = emptyList(),
            entrenamientosInfoMap = emptyMap()
        )
    ) {
        usuario?.let { currentUser ->
            // Cargamos los entrenamientos realizados
            historialViewModel.setUsuario(currentUser)
            historialViewModel.getEntrenamientosRealizadosByUsuario(currentUser)

            // Esperamos a que los datos estén disponibles
            val entrenamientosRealizados = historialViewModel.entrenamientoRealizado.value ?: emptyList()

            // Construimos un mapa con la información de los entrenamientos
            val entrenamientosInfoMap = mutableMapOf<String, EntrenamientoInfo>()

            // Obtenemos la información de cada entrenamiento
            entrenamientosRealizados.forEach { entRealizado ->
                try {
                    val entrenamiento = entrenamientosViewModel.getEntrenamientoById(entRealizado.entrenamiento)
                    if (entrenamiento != null) {
                        entrenamientosInfoMap[entRealizado._id] = EntrenamientoInfo(
                            nombre = entrenamiento.nombre,
                            imagen = entrenamiento.foto,
                            categoria = entrenamiento.categoria,
                            musculoPrincipal = entrenamiento.musculoPrincipal
                        )
                    }
                } catch (e: Exception) {
                    // Si no podemos obtener el entrenamiento, usamos valores por defecto
                    entrenamientosInfoMap[entRealizado._id] = EntrenamientoInfo(
                        nombre = "Entrenamiento",
                        imagen = null,
                        categoria = "Sin categoría",
                        musculoPrincipal = "No disponible"
                    )
                }
            }

            // Actualizamos el estado con los datos obtenidos
            value = HistorialScreenState(
                isLoading = false,
                entrenamientosRealizados = entrenamientosRealizados.sortedByDescending { it.fecha },
                entrenamientosInfoMap = entrenamientosInfoMap
            )
        }
    }

    // UI principal
    HistorialEntrenamientosContent(
        state = screenState.value,
        onBackClick = remember { { navController.popBackStack() } },
        onEntrenamientoClick = remember { { id -> navController.navigate("detalleEntrenamientoRealizado/$id") } }
    )
}

// Modelo de estado para la UI
data class HistorialScreenState(
    val isLoading: Boolean,
    val entrenamientosRealizados: List<EntrenamientoRealizado>,
    val entrenamientosInfoMap: Map<String, EntrenamientoInfo>
)

// Información adicional del entrenamiento
data class EntrenamientoInfo(
    val nombre: String,
    val imagen: String?,
    val categoria: String,
    val musculoPrincipal: String
)

// Componente principal de UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialEntrenamientosContent(
    state: HistorialScreenState,
    onBackClick: () -> Unit,
    onEntrenamientoClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido según el estado
            when {
                state.isLoading -> LoadingContent()
                state.entrenamientosRealizados.isEmpty() -> EmptyHistorialContent()
                else -> EntrenamientosHistorialList(
                    entrenamientosRealizados = state.entrenamientosRealizados,
                    entrenamientosInfoMap = state.entrenamientosInfoMap,
                    onEntrenamientoClick = onEntrenamientoClick
                )
            }
        }
    }
}

// Contenido cuando está cargando
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFAB47BC))
    }
}

// Contenido cuando no hay entrenamientos realizados
@Composable
private fun EmptyHistorialContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "No hay entrenamientos realizados",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No has realizado entrenamientos",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Comienza un entrenamiento para ver tu historial",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Lista de entrenamientos realizados
// Lista de entrenamientos realizados
@Composable
private fun EntrenamientosHistorialList(
    entrenamientosRealizados: List<EntrenamientoRealizado>,
    entrenamientosInfoMap: Map<String, EntrenamientoInfo>,
    onEntrenamientoClick: (String) -> Unit
) {
    // Instancia de calendario que usaremos en todo el componente
    val calendar = remember { Calendar.getInstance() }

    // Agrupar por mes y año
    val entrenamientosPorFecha = remember(entrenamientosRealizados) {
        entrenamientosRealizados.groupBy { entrenamiento ->
            calendar.time = entrenamiento.fecha
            Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Estadísticas generales
        item {
            EstadisticasHistorial(entrenamientosRealizados)
        }

        // Para cada mes, mostrar los entrenamientos
        entrenamientosPorFecha.forEach { (yearMonth, entrenamientos) ->
            val (year, month) = yearMonth

            // Configurar el calendario para obtener el nombre del mes
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)

            val nombreMes = SimpleDateFormat("MMMM", Locale.getDefault())
                .format(calendar.time)
                .capitalize()

            // Encabezado del mes
            item {
                Text(
                    text = "$nombreMes $year",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Divider(
                    color = Color(0xFF3A3A3A),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Entrenamientos del mes
            items(
                items = entrenamientos,
                key = { it._id }
            ) { entrenamiento ->
                val onClick = remember(entrenamiento._id) {
                    { onEntrenamientoClick(entrenamiento._id) }
                }

                EntrenamientoRealizadoItem(
                    entrenamiento = entrenamiento,
                    entrenamientoInfo = entrenamientosInfoMap[entrenamiento._id],
                    onClick = onClick
                )
            }
        }

        // Espacio al final
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Componente para estadísticas generales
@Composable
private fun EstadisticasHistorial(entrenamientosRealizados: List<EntrenamientoRealizado>) {
    val numEntrenamientos = entrenamientosRealizados.size
    val totalMinutos = entrenamientosRealizados.sumOf { it.duracion.toIntOrNull() ?: 0 }
    val horasEntrenadas = totalMinutos / 60
    val minutosRestantes = totalMinutos % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
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
                text = "Resumen de Actividad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    icon = Icons.Default.FitnessCenter,
                    value = numEntrenamientos.toString(),
                    label = "Entrenamientos"
                )

                EstadisticaItem(
                    icon = Icons.Default.Timer,
                    value = if (horasEntrenadas > 0) "$horasEntrenadas h $minutosRestantes min" else "$totalMinutos min",
                    label = "Tiempo total"
                )
            }
        }
    }
}

// Item individual de estadística
@Composable
private fun EstadisticaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

// Item de entrenamiento realizado
@Composable
private fun EntrenamientoRealizadoItem(
    entrenamiento: EntrenamientoRealizado,
    entrenamientoInfo: EntrenamientoInfo?,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(entrenamiento.fecha) { formatter.format(entrenamiento.fecha) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono o imagen
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF7B1FA2),
                                Color(0xFFAB47BC)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Si tenemos imagen, la mostraríamos aquí
                // Por ahora, mostramos un icono
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del entrenamiento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entrenamientoInfo?.nombre ?: "Entrenamiento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Categoría y músculo
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = entrenamientoInfo?.categoria ?: "Sin categoría",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = entrenamientoInfo?.musculoPrincipal ?: "No disponible",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fecha y duración
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${entrenamiento.duracion} min",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            // Indicador para ver detalles
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                tint = Color(0xFFAB47BC)
            )
        }
    }
}

// Extensión para capitalizar strings
private fun String.capitalize(): String {
    return if (this.isNotEmpty()) {
        this[0].uppercase() + this.substring(1)
    } else {
        this
    }
}