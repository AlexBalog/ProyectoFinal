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
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@SuppressLint("UnrememberedGetBackStackEntry", "StateFlowValueCalledInComposition")
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
    var showConfigDialog by remember { mutableStateOf(false) }


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
                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración de la conversación",
                            tint = Color(0xFFAB47BC)
                        )
                    }
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
                        UltraMessageBubble(
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
                                categoria = conversacionActual?.categoria ?: "general",
                                viewModel = viewModel
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
        if (showConfigDialog) {
            // Add state variables for the dialog
            var editedTitle by remember { mutableStateOf(conversacionActual?.titulo ?: "") }
            var showDeleteConfirmation by remember { mutableStateOf(false) }

            // Settings Dialog
            Dialog(
                onDismissRequest = { showConfigDialog = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0xFF7B1FA2),
                                spotColor = Color(0xFF7B1FA2)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Header with purple accent
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = Color(0xFFAB47BC),
                                    modifier = Modifier.size(28.dp)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = "Configuración",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = { showConfigDialog = false },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF333333))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Title section with label and subtitle
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Title,
                                        contentDescription = "Título",
                                        tint = Color(0xFFAB47BC),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Título de la conversación",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }

                                Text(
                                    text = "Edita el título para identificarla fácilmente",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Custom styled text field with purple underline
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2A2A2A))
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF444444),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        BasicTextField(
                                            value = editedTitle,
                                            onValueChange = { editedTitle = it },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = Color.White,
                                                fontSize = 16.sp
                                            ),
                                            singleLine = true,
                                            cursorBrush = SolidColor(Color(0xFFAB47BC)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            // Danger zone
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF2A1216))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Advertencia",
                                        tint = Color(0xFFE57373),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Zona de peligro",
                                        color = Color(0xFFE57373),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                }

                                Text(
                                    text = "Las siguientes acciones no se pueden deshacer",
                                    color = Color(0xFFBBBBBB),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Delete button with improved styling
                                Button(
                                    onClick = { showDeleteConfirmation = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3D1A1A),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFF5D2D2D),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFE57373),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = "Eliminar conversación",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(30.dp))

                            // Action buttons with improved styling
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { showConfigDialog = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(top = 5.dp)
                                ) {
                                    Text("Cancelar")
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = {
                                        if (editedTitle.isNotEmpty() && editedTitle != conversacionActual?.titulo) {
                                            conversacionActual?._id?.let { id ->
                                                viewModel.actualizarTituloConversacion(id, editedTitle)
                                            }
                                        }
                                        showConfigDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF7B1FA2),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Guardar",
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "Guardar cambios",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = {
                        Text(
                            text = "Confirmar eliminación",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "¿Estás seguro de que deseas eliminar esta conversación? Esta acción no se puede deshacer y se perderán todos los mensajes.",
                            color = Color.White
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                // Delete conversation and navigate back
                                conversacionActual?._id?.let { id ->
                                    viewModel.eliminarConversacion(id)
                                    navController.popBackStack()
                                }
                                showDeleteConfirmation = false
                                showConfigDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB71C1C))
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirmation = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                        ) {
                            Text("Cancelar")
                        }
                    },
                    containerColor = Color(0xFF252525),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
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
    usuarios: Usuarios? = null,
    viewModel: IAViewModel
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
                },
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SuggestionBubble(
    text: String,
    color: Color,
    viewModel: IAViewModel
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clickable { viewModel.enviarMensaje(text) }
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
                            ProfileDataRow(Icons.Default.Whatshot, "Calorías de mantenimiento", usuario?.caloriasMantenimiento.toString() ?: "No calculadas")
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
fun UltraMessageBubble(
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
            // Avatar o estado del mensaje
            if (!isUserMessage) {
                // Avatar para mensajes de IA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                ) {
                    // Avatar con sombra y borde
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF7B1FA2))
                            .border(1.dp, Color(0xFFBB86FC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                mensaje.contenido.contains("dominadas") -> Icons.Default.FitnessCenter
                                mensaje.contenido.contains("ejercicio") -> Icons.Default.FitnessCenter
                                mensaje.contenido.contains("entrenamiento") -> Icons.Default.FitnessCenter
                                mensaje.contenido.contains("dieta") -> Icons.Default.Restaurant
                                mensaje.contenido.contains("nutrición") -> Icons.Default.Restaurant
                                mensaje.contenido.contains("alimentación") -> Icons.Default.Restaurant
                                mensaje.contenido.contains("hábitos") -> Icons.Default.Lightbulb
                                mensaje.contenido.contains("rutina") -> Icons.Default.AccessTime
                                else -> Icons.Default.Psychology
                            },
                            contentDescription = "FitMind IA",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "FitMind",
                        fontSize = 12.sp,
                        color = Color(0xFFE1BEE7),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Para mensajes del usuario, mostrar estado de envío
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Enviado",
                        tint = Color(0xFFAA77FF),
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Enviado",
                        fontSize = 10.sp,
                        color = Color(0xFFAA77FF),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Burbuja de mensaje
            Card(
                shape = RoundedCornerShape(
                    topStart = if (isUserMessage) 16.dp else 4.dp,
                    topEnd = if (isUserMessage) 4.dp else 16.dp,
                    bottomStart = if (isUserMessage) 16.dp else 8.dp,
                    bottomEnd = if (isUserMessage) 8.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUserMessage) Color(0xFF8E24AA) else Color(0xFF252525)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isUserMessage) 3.dp else 3.dp
                ),
                modifier = Modifier
                    .padding(
                        start = if (isUserMessage) 64.dp else 0.dp,
                        end = if (isUserMessage) 0.dp else 64.dp
                    )
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                if (isUserMessage) {
                    // Mensaje del usuario con diseño mejorado
                    UserMessageContent(mensaje.contenido)
                } else {
                    // Mensaje de la IA con formato avanzado
                    AIMessageContent(mensaje.contenido)
                }
            }

            // Timestamp con efecto de difuminado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    start = if (isUserMessage) 0.dp else 8.dp,
                    end = if (isUserMessage) 8.dp else 0.dp,
                    top = 2.dp,
                    bottom = 2.dp
                )
            ) {
                if (!isUserMessage) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Leído",
                        tint = Color(0xFF9575CD),
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = SimpleDateFormat("HH:mm", Locale("es", "ES"))
                        .format(mensaje.timestamp),
                    fontSize = 11.sp,
                    color = if (isUserMessage) Color(0xFFE1BEE7) else Color(0xFF9E9E9E)
                )

                if (isUserMessage) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Enviado",
                        tint = Color(0xFFCE93D8),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diseño mejorado para mensajes del usuario
 */
@Composable
fun UserMessageContent(content: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = content,
                color = Color.White,
                lineHeight = 22.sp,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Pequeño elemento decorativo en la esquina
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Color(0xFFEA80FC).copy(alpha = 0.2f))
        )
    }
}

/**
 * Diseño avanzado para mensajes de la IA
 */
@Composable
fun AIMessageContent(content: String) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        // Dividir el contenido en párrafos
        val paragraphs = content.split("\n\n")

        paragraphs.forEachIndexed { index, paragraph ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comprobar si es un título (empieza con ** y contiene :**)
            if (paragraph.startsWith("**") && paragraph.contains(":**")) {
                val titleText = paragraph
                    .substringAfter("**")
                    .substringBefore(":**")

                // Título con línea decorativa y color de acento
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$titleText:",
                        color = Color(0xFFBB86FC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Línea decorativa bajo el título
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(40.dp)
                            .background(Color(0xFFBB86FC).copy(alpha = 0.5f))
                    )
                }
            }
            // Si es una lista o un texto regular
            else {
                val lines = paragraph.split("\n")

                lines.forEach { line ->
                    // Elemento principal de lista
                    if (line.trim().startsWith("* ")) {
                        // Analizar si contiene un título con dos puntos
                        val lineText = line.substringAfter("* ")

                        if (lineText.contains(":") && !lineText.contains(": ")) {
                            // Es un título con viñeta - formato especial
                            val titlePart = lineText.substringBefore(":")
                            val contentPart = lineText.substringAfter(":")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                // Título con viñeta
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFFBB86FC), CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Título en color
                                    Text(
                                        text = "$titlePart:",
                                        color = Color(0xFFBB86FC),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                // Contenido en la siguiente línea
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contentPart.trim(),
                                    color = Color.White,
                                    lineHeight = 22.sp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(start = 22.dp)
                                )
                            }
                        } else {
                            // Lista normal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFFBB86FC), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                FormatText(lineText)
                            }
                        }
                    }
                    // Sublista
                    else if (line.trim().startsWith("*** ")) {
                        // Analizar si contiene un título con dos puntos
                        val lineText = line.substringAfter("*** ")

                        if (lineText.contains(":") && !lineText.contains(": ")) {
                            // Es un subtítulo con viñeta - formato especial
                            val titlePart = lineText.substringBefore(":")
                            val contentPart = lineText.substringAfter(":")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, top = 3.dp, bottom = 3.dp)
                            ) {
                                // Subtítulo con viñeta
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(Color(0xFFCE93D8), CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Subtítulo en color
                                    Text(
                                        text = "$titlePart:",
                                        color = Color(0xFFCE93D8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                // Contenido en la siguiente línea
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contentPart.trim(),
                                    color = Color(0xFFE1BEE7),
                                    lineHeight = 22.sp,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(start = 20.dp)
                                )
                            }
                        } else {
                            // Sublista normal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, top = 3.dp, bottom = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(Color(0xFFCE93D8), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                FormatText(
                                    text = lineText,
                                    textColor = Color(0xFFE1BEE7)
                                )
                            }
                        }
                    }
                    // Texto normal
                    else {
                        FormatText(line)
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar texto con formato.
 * Procesa texto en negrita e ítems de lista.
 */
// Reemplaza la función FormatText actual con esta versión mejorada
@Composable
fun FormatText(
    text: String,
    textColor: Color = Color.White
) {
    // Si el texto empieza con **, procesarlo de manera especial para ocultar los asteriscos
    if (text.trim().startsWith("**")) {
        val cleanText = text.trim().removePrefix("**")
        val parts = cleanText.split("**")

        if (parts.size > 1) {
            // Formato especial para texto que comenzaba con **
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in parts.indices) {
                    Text(
                        text = parts[i],
                        color = if (i % 2 == 0) Color(0xFFBB86FC) else textColor,
                        fontWeight = if (i % 2 == 0) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = 22.sp,
                        fontSize = if (i % 2 == 0) 16.sp else 15.sp
                    )

                    if (i < parts.size - 1 && parts[i].isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        } else {
            // Solo un segmento, pero empezaba con **
            Text(
                text = cleanText,
                color = Color(0xFFBB86FC),
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp,
                fontSize = 16.sp
            )
        }
    }
    // Procesar texto normal con posibles segmentos en negrita
    else {
        val parts = text.split("**")

        if (parts.size > 1) {
            // Formato para texto con partes en negrita
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in parts.indices) {
                    if (parts[i].isNotEmpty()) {
                        Text(
                            text = parts[i],
                            color = if (i % 2 == 0) textColor else Color(0xFFBB86FC),
                            fontWeight = if (i % 2 == 0) FontWeight.Normal else FontWeight.Bold,
                            lineHeight = 22.sp,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Texto simple sin formato
            Text(
                text = text,
                color = textColor,
                lineHeight = 22.sp,
                fontSize = 15.sp
            )
        }
    }
}