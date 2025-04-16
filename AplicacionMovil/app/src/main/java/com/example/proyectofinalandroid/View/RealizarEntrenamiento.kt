package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.example.proyectofinalandroid.utils.base64ToBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComenzarEntrenamientoScreen(
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
    val scrollState = rememberScrollState()

    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(usuario!!)
            ejerciciosViewModel.setUsuario(usuario!!)
        }
    }

    var isAnimatedIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Mapa para almacenar los ejercicios cargados
    val ejerciciosCargados = remember { mutableStateMapOf<String, Ejercicios>() }

    // Observar el estado del entrenamiento seleccionado
    val entrenamientoSeleccionado by entrenamientosViewModel.entrenamientoSeleccionado.collectAsState()
    val isLoading by entrenamientosViewModel.isLoading.collectAsState()

    // Estado para controlar los diálogos
    var showCancelDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // Estado para el cronómetro
    var tiempoTranscurrido by remember { mutableStateOf(0L) }
    var tiempoActivo by remember { mutableStateOf(true) }

    // Iniciar cronómetro cuando se carga la pantalla
    LaunchedEffect(tiempoActivo) {
        while (tiempoActivo) {
            delay(1000)
            tiempoTranscurrido += 1
        }
    }

    // Cargar el entrenamiento por ID cuando se inicia la pantalla
    LaunchedEffect(entrenamientoId) {
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

    // Diálogo de cancelar entrenamiento
    if (showCancelDialog) {
        AlertaConfirmacion(
            titulo = "Cancelar entrenamiento",
            mensaje = "¿Estás seguro de que deseas cancelar el entrenamiento? No se guardarán tus progresos.",
            onConfirm = {
                tiempoActivo = false
                showCancelDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showCancelDialog = false
            }
        )
    }

    // Diálogo de finalizar entrenamiento
    if (showFinishDialog) {
        AlertaConfirmacion(
            titulo = "Finalizar entrenamiento",
            mensaje = "¿Estás seguro de que deseas finalizar el entrenamiento? Se guardarán tus progresos.",
            onConfirm = {
                tiempoActivo = false
                showFinishDialog = false
                // Aquí se guardarían los datos del entrenamiento
                navController.popBackStack()
            },
            onDismiss = {
                showFinishDialog = false
            }
        )
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
                    entrenamientoSeleccionado?.let { entrenamiento ->
                        // Estructura principal
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Barra superior con degradado y cronómetro
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
                                // Título de la aplicación y cronómetro en la barra superior
                                Column(
                                    modifier = Modifier
                                        .padding(top = 33.dp)
                                        .align(Alignment.TopCenter),
                                    horizontalAlignment = Alignment.CenterHorizontally
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
                                        )
                                    )

                                    Text(
                                        text = formatearTiempo(tiempoTranscurrido),
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFAB47BC)
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // Contenido desplazable (incluyendo imagen y tarjeta)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {
                                // Imagen de fondo (header) - Ahora comienza justo debajo de la barra superior
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .offset(y = (-30).dp) // Para que se una con la barra superior
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

                                    // Gradiente para mejorar legibilidad
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) {
                                        // Degradado de arriba hacia abajo
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

                                        // Degradado de abajo hacia arriba
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
                                    // Espacio para la imagen de fondo
                                    Spacer(modifier = Modifier.height(230.dp))

                                    // Tarjeta con la información del entrenamiento y registro de series
                                    EntrenamientoRegistroCard(
                                        entrenamiento = entrenamiento,
                                        ejerciciosCargados = ejerciciosCargados,
                                        onFinishClick = { showFinishDialog = true },
                                        onCancelClick = { showCancelDialog = true }
                                    )
                                }
                            }
                        }

                        // Botón de regreso
                        IconButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .padding(16.dp, 25.dp, 16.dp, 16.dp)
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
fun EntrenamientoRegistroCard(
    entrenamiento: Entrenamientos,
    ejerciciosCargados: Map<String, Ejercicios>,
    onFinishClick: () -> Unit,
    onCancelClick: () -> Unit
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

            // Tarjeta de categoría
            CategoryCardRE(entrenamiento.categoria)

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de ejercicios con registro de series
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
                    EjercicioConSeries(ejercicio)
                } else {
                    TarjetaEjercicioCargandoRE(ejercicioId)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de finalizar y cancelar entrenamiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de cancelar (X)
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .size(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Botón de finalizar entrenamiento
                Button(
                    onClick = onFinishClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "FINALIZAR ENTRENAMIENTO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EjercicioConSeries(ejercicio: Ejercicios) {
    // Estado para controlar la lista de series
    val series = remember { mutableStateListOf<Serie>() }

    // Estado para el diálogo de añadir serie
    var showAddSerieDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // Encabezado con información del ejercicio
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
            }

            // Lista de series registradas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (series.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mostrar cada serie registrada
                    series.forEachIndexed { index, serie ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF252525)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Serie ${index + 1} - ${serie.peso} kg - ${serie.repeticiones} reps",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Botón para eliminar serie
                                IconButton(
                                    onClick = { series.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color(0xFFE57373),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón de añadir serie
                Button(
                    onClick = { showAddSerieDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2).copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir serie",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Añadir serie",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Diálogo para añadir serie
    if (showAddSerieDialog) {
        DialogoAnadirSerie(
            onConfirm = { peso, repeticiones ->
                series.add(Serie(peso, repeticiones))
                showAddSerieDialog = false
            },
            onDismiss = { showAddSerieDialog = false }
        )
    }
}

data class Serie(val peso: String, val repeticiones: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAnadirSerie(
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
                    style = TextStyle(
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

@Composable
fun AlertaConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    .padding(16.dp)
            ) {
                Text(
                    text = titulo,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = mensaje,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.LightGray
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFAB47BC)
                        )
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCardRE(categoria: String) {
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
fun TarjetaEjercicioCargandoRE(ejercicioId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Espacio para imagen (cargando)
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .height(86.dp)
                    .background(Color(0xFF252525)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFAB47BC),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Información del ejercicio
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Placeholder para el nombre
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(16.dp)
                        .background(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder para el músculo
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .background(
                            color = Color(0xFF333333),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

// Función para formatear el tiempo transcurrido
fun formatearTiempo(segundos: Long): String {
    val horas = TimeUnit.SECONDS.toHours(segundos)
    val minutos = TimeUnit.SECONDS.toMinutes(segundos) % 60
    val segundosRestantes = segundos % 60

    return String.format("%02d:%02d:%02d", horas, minutos, segundosRestantes)
}