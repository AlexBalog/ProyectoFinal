package com.example.proyectofinalandroid.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.Model.Conversacion
import com.example.proyectofinalandroid.ViewModel.IAViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import android.util.Log



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitMindScreen(
    navController: NavController,
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
    val conversaciones by viewModel.conversaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Variables de estado para el diálogo de nueva conversación
    var showNewChatDialog by remember { mutableStateOf(false) }

    // Efecto para cargar datos iniciales
    LaunchedEffect(usuario) {
        usuario?.let {
            viewModel.setUsuario(it)
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
            // Barra superior
            TopAppBar(
                title = {
                    Text(
                        text = "FitMind IA",
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
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva Conversación",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                }
            )

            // Contenido principal
            if (isLoading && conversaciones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFAB47BC))
                }
            } else if (conversaciones.isEmpty()) {
                // Pantalla de bienvenida cuando no hay conversaciones
                WelcomeScreen(onNewChat = { showNewChatDialog = true })
            } else {
                // Lista de conversaciones
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversaciones) { conversacion ->
                        ConversationCard(
                            conversacion = conversacion,
                            onClick = {
                                navController.navigate("chat/${conversacion._id}")
                            }
                        )
                    }
                    // Espacio para el footer
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        // Footer fijo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FooterNavigation(
                navController = navController,
                currentRoute = "fitmind",
                usuario = usuario
            )
        }

        // Diálogo para nueva conversación
        if (showNewChatDialog) {
            NewChatDialog(
                onDismiss = { showNewChatDialog = false },
                onCategorySelected = { categoria ->
                    viewModel.crearConversacion(categoria)
                    showNewChatDialog = false
                    // Redirigir al chat una vez creado
                    viewModel.conversacionActual.value?._id?.let { id ->
                        navController.navigate("chat/$id")
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(onNewChat: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono grande
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = "FitMind",
            tint = Color(0xFFAB47BC),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Texto de bienvenida
        Text(
            text = "Bienvenido a FitMind",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tu asistente personal de fitness y bienestar impulsado por IA",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Botón para iniciar conversación
        Button(
            onClick = onNewChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFAB47BC)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFF7B1FA2),
                    spotColor = Color(0xFF7B1FA2)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Iniciar chat",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Iniciar nueva conversación",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Categorías
        Text(
            text = "Pide ayuda sobre:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryChip(
                text = "Nutrición",
                icon = Icons.Default.Restaurant,
                color = Color(0xFF4CAF50),
                onClick = { /* No action */ }
            )

            Spacer(modifier = Modifier.width(8.dp))

            CategoryChip(
                text = "Entrenamiento",
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF2196F3),
                onClick = { /* No action */ }
            )

            Spacer(modifier = Modifier.width(8.dp))

            CategoryChip(
                text = "Hábitos",
                icon = Icons.Default.Lightbulb,
                color = Color(0xFFFFC107),
                onClick = { /* No action */ }
            )
        }
    }
}

@Composable
fun ConversationCard(
    conversacion: Conversacion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono basado en categoría
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (conversacion.categoria) {
                            "nutricion" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "entrenamiento" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            "habitos" -> Color(0xFFFFC107).copy(alpha = 0.2f)
                            else -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (conversacion.categoria) {
                        "nutricion" -> Icons.Default.Restaurant
                        "entrenamiento" -> Icons.Default.FitnessCenter
                        "habitos" -> Icons.Default.Lightbulb
                        else -> Icons.Default.Psychology
                    },
                    contentDescription = "Categoría",
                    tint = when (conversacion.categoria) {
                        "nutricion" -> Color(0xFF4CAF50)
                        "entrenamiento" -> Color(0xFF2196F3)
                        "habitos" -> Color(0xFFFFC107)
                        else -> Color(0xFFAB47BC)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversacion.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale("es", "ES"))
                        .format(conversacion.updatedAt),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Abrir",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                Text(
                    text = "Nueva Conversación",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 22.sp,
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

                Text(
                    text = "Selecciona una categoría",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Categorías
                CategoryButton(
                    text = "Nutrición",
                    icon = Icons.Default.Restaurant,
                    color = Color(0xFF4CAF50),
                    onClick = { onCategorySelected("nutricion") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryButton(
                    text = "Entrenamiento",
                    icon = Icons.Default.FitnessCenter,
                    color = Color(0xFF2196F3),
                    onClick = { onCategorySelected("entrenamiento") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryButton(
                    text = "Hábitos",
                    icon = Icons.Default.Lightbulb,
                    color = Color(0xFFFFC107),
                    onClick = { onCategorySelected("habitos") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancelar", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Seleccionar",
                tint = color
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                fontSize = 12.sp,
                color = color
            )
        }
    }
}