package com.example.proyectofinalandroid.View

import android.util.Log
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.Model.Mensaje
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.ViewModel.IAViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    conversacionId: String,
) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }
    val viewModel: IAViewModel = hiltViewModel(parentEntry)
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val usuario by usuariosViewModel.usuario.collectAsState()
    val mensajes by viewModel.mensajes.collectAsState()
    val conversacionActual by viewModel.conversacionActual.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Estado para el campo de texto
    var messageText by remember { mutableStateOf("") }

    // Cargar mensajes de la conversación
    LaunchedEffect(usuario, conversacionId) {
        usuario?.let {
            viewModel.setUsuario(it)
            viewModel.cargarConversacion(conversacionId)
        }
    }

    // Scroll al último mensaje cuando llegan nuevos mensajes
    LaunchedEffect(mensajes) {
        if (mensajes.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Barra superior con título de la conversación
            TopAppBar(
                title = {
                    Text(
                        text = conversacionActual?.titulo ?: "Conversación",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    // Icono basado en categoría
                    Icon(
                        imageVector = when (conversacionActual?.categoria) {
                            "nutricion" -> Icons.Default.Restaurant
                            "entrenamiento" -> Icons.Default.FitnessCenter
                            "habitos" -> Icons.Default.Lightbulb
                            else -> Icons.Default.Psychology
                        },
                        contentDescription = "Categoría",
                        tint = when (conversacionActual?.categoria) {
                            "nutricion" -> Color(0xFF4CAF50)
                            "entrenamiento" -> Color(0xFF2196F3)
                            "habitos" -> Color(0xFFFFC107)
                            else -> Color(0xFFAB47BC)
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )

            if (conversacionActual != null) {
                val usuario = viewModel.usuario.value

                AnimatedVisibility(
                    visible = mensajes.isEmpty() && !isLoading,
                    enter = fadeIn() + expandVertically()
                ) {
                    // Reemplaza el uso actual en ChatScreen con esto:
                    if (conversacionActual != null && mensajes.isEmpty() && !isLoading) {
                        CollapsibleProfileCard(
                            usuario = usuario,
                            conversacionActual = conversacionActual,
                            navController = navController
                        )
                    }
                }
            }

            // Mensajes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mensajes.reversed()) { mensaje ->
                        SimpleEnhancedMessageBubble(
                            mensaje = mensaje,
                            isUserMessage = mensaje.esDeUsuario
                        )
                    }

                    // Indicador de escritura
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF333333)
                                    ),
                                    modifier = Modifier.padding(end = 64.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFAB47BC))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF7B1FA2))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4A148C))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Mensaje inicial o de bienvenida
                    if (mensajes.isEmpty() && !isLoading) {
                        item {
                            WelcomeMessage(
                                categoria = conversacionActual?.categoria ?: "general"
                            )
                        }
                    }
                }
            }

            // Campo de entrada de texto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 50.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF7B1FA2),
                        spotColor = Color(0xFF7B1FA2)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = Color(0xFFAB47BC),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (messageText.isNotEmpty() && !isLoading) {
                                    viewModel.enviarMensaje(messageText)
                                    messageText = ""
                                }
                            }
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty() && !isLoading) {
                                viewModel.enviarMensaje(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotEmpty() && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (messageText.isNotEmpty() && !isLoading)
                                Color(0xFFAB47BC) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    mensaje: Mensaje,
    isUserMessage: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUserMessage) 16.dp else 4.dp,
                    bottomEnd = if (isUserMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUserMessage) Color(0xFF7B1FA2) else Color(0xFF333333)
                ),
                modifier = Modifier.padding(
                    start = if (isUserMessage) 64.dp else 0.dp,
                    end = if (isUserMessage) 0.dp else 64.dp
                )
            ) {
                Text(
                    text = mensaje.contenido,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }

            Text(
                text = SimpleDateFormat("HH:mm", Locale("es", "ES"))
                    .format(mensaje.timestamp),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun WelcomeMessage(
    categoria: String,
    usuarios: Usuarios? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icono
        Icon(
            imageVector = when (categoria) {
                "nutricion" -> Icons.Default.Restaurant
                "entrenamiento" -> Icons.Default.FitnessCenter
                "habitos" -> Icons.Default.Lightbulb
                else -> Icons.Default.Psychology
            },
            contentDescription = "Categoría",
            tint = when (categoria) {
                "nutricion" -> Color(0xFF4CAF50)
                "entrenamiento" -> Color(0xFF2196F3)
                "habitos" -> Color(0xFFFFC107)
                else -> Color(0xFFAB47BC)
            },
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título
        Text(
            text = when (categoria) {
                "nutricion" -> "Asistente de Nutrición"
                "entrenamiento" -> "Entrenador Personal"
                "habitos" -> "Coach de Hábitos"
                else -> "FitMind"
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Descripción
        Text(
            text = when (categoria) {
                "nutricion" -> "Pregúntame sobre dietas, alimentos, planes de nutrición o cualquier duda sobre alimentación saludable."
                "entrenamiento" -> "Pregúntame sobre rutinas de ejercicio, técnicas correctas, planes de entrenamiento o cualquier duda fitness."
                "habitos" -> "Pregúntame sobre cómo mejorar tus hábitos diarios, rutinas saludables o consejos para una vida más equilibrada."
                else -> "Soy tu asistente de fitness y bienestar. ¿En qué puedo ayudarte hoy?"
            },
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sugerencias
        Text(
            text = "Prueba preguntarme:",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ejemplos de preguntas
        val sugerencias = when (categoria) {
            "nutricion" -> listOf(
                "¿Cómo puedo mantener una dieta equilibrada?",
                "¿Qué alimentos me ayudan a ganar masa muscular?",
                "¿Cuál es la mejor dieta para perder grasa?"
            )
            "entrenamiento" -> listOf(
                "¿Cómo puedo mejorar mi resistencia?",
                "Dame una rutina para trabajar pecho y brazos",
                "¿Cuáles son los mejores ejercicios para principiantes?"
            )
            "habitos" -> listOf(
                "¿Cómo puedo mejorar mi calidad de sueño?",
                "Dame consejos para mantener la motivación",
                "¿Cómo incorporar más actividad física en mi día a día?"
            )
            else -> listOf(
                "¿Cómo puedo mejorar mi condición física?",
                "Dame consejos para una alimentación saludable",
                "¿Cómo mantener un buen equilibrio entre ejercicio y descanso?"
            )
        }

        sugerencias.forEach { sugerencia ->
            SuggestionBubble(
                text = sugerencia,
                color = when (categoria) {
                    "nutricion" -> Color(0xFF4CAF50)
                    "entrenamiento" -> Color(0xFF2196F3)
                    "habitos" -> Color(0xFFFFC107)
                    else -> Color(0xFFAB47BC)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SuggestionBubble(
    text: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clickable { /* Enviar sugerencia */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Enviar",
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProfileDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CollapsibleProfileCard(
    usuario: Usuarios?,
    conversacionActual: Conversacion?,
    navController: NavController
) {
    // Estado para controlar si la tarjeta está expandida o no
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize( // Animación suave al expandir/colapsar
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
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
            // Encabezado con título y botón para expandir/colapsar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono según categoría
                    Icon(
                        imageVector = when (conversacionActual?.categoria) {
                            "nutricion" -> Icons.Default.Restaurant
                            "entrenamiento" -> Icons.Default.FitnessCenter
                            "habitos" -> Icons.Default.Lightbulb
                            else -> Icons.Default.Psychology
                        },
                        contentDescription = "Categoría",
                        tint = when (conversacionActual?.categoria) {
                            "nutricion" -> Color(0xFF4CAF50)
                            "entrenamiento" -> Color(0xFF2196F3)
                            "habitos" -> Color(0xFFFFC107)
                            else -> Color(0xFFAB47BC)
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Tu perfil actual",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Botón para expandir/colapsar
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = Color(0xFFAB47BC)
                    )
                }
            }

            // Vista compacta (siempre visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mostrar solo 3 datos principales en vista compacta
                when (conversacionActual?.categoria) {
                    "nutricion" -> {
                        ProfileDataChip(Icons.Default.Info, "IMC", "${usuario?.IMC ?: "?"}")
                        ProfileDataChip(Icons.Default.LineWeight, "Peso", "${usuario?.peso ?: "?"} kg")
                        ProfileDataChip(Icons.Default.Star, "Objetivo", "${usuario?.objetivoPeso ?: "?"} kg")
                    }
                    "entrenamiento" -> {
                        ProfileDataChip(Icons.Default.Speed, "Nivel", usuario?.nivelActividad ?: "?")
                        ProfileDataChip(Icons.Default.LineWeight, "Peso", "${usuario?.peso ?: "?"} kg")
                        ProfileDataChip(Icons.Default.Straighten, "Altura", "${usuario?.altura ?: "?"} cm")
                    }
                    "habitos" -> {
                        ProfileDataChip(Icons.Default.Speed, "Nivel", usuario?.nivelActividad ?: "?")
                        ProfileDataChip(Icons.Default.Info, "IMC", "${usuario?.IMC ?: "?"}")
                        ProfileDataChip(Icons.Default.Star, "Objetivo", "${usuario?.objetivoPeso ?: "?"} kg")
                    }
                    else -> {
                        ProfileDataChip(Icons.Default.Speed, "Nivel", usuario?.nivelActividad ?: "?")
                        ProfileDataChip(Icons.Default.Info, "IMC", "${usuario?.IMC ?: "?"}")
                        ProfileDataChip(Icons.Default.LineWeight, "Peso", "${usuario?.peso ?: "?"} kg")
                    }
                }
            }

            // Vista expandida (visible solo cuando isExpanded es true)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(
                        color = Color(0xFF444444),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Datos detallados dependiendo de la categoría
                    when (conversacionActual?.categoria) {
                        "nutricion" -> {
                            ProfileDataRow(Icons.Default.Whatshot, "Calorías de mantenimiento", usuario?.caloriasMantenimiento ?: "No calculadas")
                            ProfileDataRow(Icons.Default.Whatshot, "Calorías objetivo", usuario?.objetivoCalorias.toString() ?: "No calculadas")
                            if (usuario?.objetivoPeso != null && usuario.peso != null) {
                                val diferencia = usuario.objetivoPeso - usuario.peso
                                val estado = if (diferencia > 0) "Aumentar" else "Reducir"
                                ProfileDataRow(Icons.Default.Update, "Meta", "$estado ${Math.abs(diferencia)} kg")
                            }
                        }
                        "entrenamiento" -> {
                            ProfileDataRow(Icons.Default.Info, "IMC", "${usuario?.IMC ?: "No calculado"}")
                            ProfileDataRow(Icons.Default.Refresh, "Frecuencia", "3-4 días/semana")
                        }
                        "habitos" -> {
                            ProfileDataRow(Icons.Default.WaterDrop, "Hidratación", "Recuerda beber 2 litros/día")
                            ProfileDataRow(Icons.Default.Nightlight, "Sueño", "Duerme 7-8 h/día")
                            ProfileDataRow(Icons.Default.DirectionsWalk, "Actividad diaria", "Haz 10000 pasos diarios")
                        }
                        else -> {
                            ProfileDataRow(Icons.Default.Straighten, "Altura", "${usuario?.altura ?: "No especificada"} cm")
                            ProfileDataRow(Icons.Default.Person, "Edad", "No especificada")
                            ProfileDataRow(Icons.Default.Face, "Género", "No especificado")
                        }
                    }
                }
            }

            // Botón para actualizar perfil
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { navController.navigate("profile") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFAB47BC)
                        )
                    ) {
                        Text("Actualizar perfil")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDataChip(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color = Color(0xFF333333),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProfileDataRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun SimpleEnhancedMessageBubble(
    mensaje: Mensaje,
    isUserMessage: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
        ) {
            // Avatar para mensajes de IA
            if (!isUserMessage) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF9C27B0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "FitMind IA",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "FitMind",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Burbuja de mensaje
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUserMessage) 16.dp else 4.dp,
                    bottomEnd = if (isUserMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUserMessage) Color(0xFF7B1FA2) else Color(0xFF252525)
                ),
                modifier = Modifier
                    .padding(
                        start = if (isUserMessage) 64.dp else 0.dp,
                        end = if (isUserMessage) 0.dp else 64.dp
                    )
                    .animateContentSize() // Animación suave al expandir el contenido
            ) {
                if (isUserMessage) {
                    // Mensaje del usuario (texto simple)
                    Text(
                        text = mensaje.contenido,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White
                    )
                } else {
                    // Mensaje de la IA (con formato mejorado sin AnnotatedString)
                    FormattedAIMessage(mensaje.contenido)
                }
            }

            // Timestamp
            Text(
                text = SimpleDateFormat("HH:mm", Locale("es", "ES"))
                    .format(mensaje.timestamp),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun FormattedAIMessage(content: String) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        // Dividir el contenido en párrafos
        val paragraphs = content.split("\n\n")

        paragraphs.forEachIndexed { index, paragraph ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comprobar si es un título (empieza y termina con **)
            if (paragraph.startsWith("**") && paragraph.contains(":**")) {
                val titleText = paragraph
                    .substringAfter("**")
                    .substringBefore(":**")

                Text(
                    text = "$titleText:",
                    color = Color(0xFFE1BEE7),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            // Si es una lista o un texto regular
            else {
                val lines = paragraph.split("\n")

                lines.forEach { line ->
                    if (line.trim().startsWith("* ")) {
                        // Elemento de lista principal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "• ",
                                color = Color(0xFFE1BEE7),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            Text(
                                text = line.substringAfter("* "),
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    else if (line.trim().startsWith("*** ")) {
                        // Subelemento de lista
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 2.dp, 0.dp, 2.dp)
                        ) {
                            Text(
                                text = "◦ ",
                                color = Color(0xFFBA68C8),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )

                            Text(
                                text = line.substringAfter("*** "),
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    else {
                        // Texto normal - destacar partes en negrita
                        val parts = line.split("**")
                        if (parts.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                parts.forEachIndexed { i, part ->
                                    if (i % 2 == 0) {
                                        // Texto normal
                                        Text(
                                            text = part,
                                            color = Color.White,
                                            lineHeight = 20.sp
                                        )
                                    } else {
                                        // Texto en negrita
                                        Text(
                                            text = part,
                                            color = Color(0xFFE1BEE7),
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Texto simple sin formato
                            Text(
                                text = line,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}