package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import android.util.Log
import com.example.proyectofinalandroid.Model.Usuarios
import kotlinx.coroutines.flow.filter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.ImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEntrenamientosScreen(
    navController: NavController
) {
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }

    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(parentEntry)

    val usuario by usuariosViewModel.usuario.collectAsState()
    val entrenamientos by entrenamientosViewModel.entrenamientos.collectAsState()
    val scope = rememberCoroutineScope()

    // Estados para el diálogo de confirmación
    var entrenamientoAEliminar by remember { mutableStateOf<Entrenamientos?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // ✅ Filtrar entrenamientos del usuario directamente en el Composable
    val misEntrenamientos = remember(entrenamientos, usuario) {
        entrenamientos?.filter {
            it.creador == usuario?._id && !it.baja // Solo entrenamientos activos del usuario
        } ?: emptyList()
    }

    // ✅ Cargar datos cuando el usuario cambie
    LaunchedEffect(usuario) {
        usuario?.let { currentUser ->
            isLoading = true
            try {
                entrenamientosViewModel.setUsuario(currentUser)
                val filtros = mapOf(
                    "creador" to (currentUser._id ?: ""),
                    "baja" to "false"
                )
                entrenamientosViewModel.getFilter(filtros)
            } finally {
                isLoading = false
            }
        }
    }

    // Función para manejar la baja del entrenamiento
    val onDeleteEntrenamiento = remember {
        { entrenamiento: Entrenamientos ->
            entrenamientoAEliminar = entrenamiento
            showDeleteDialog = true
        }
    }

    // ✅ Función para confirmar baja MEJORADA - siempre cierra el diálogo
    val onConfirmDelete = {
        entrenamientoAEliminar?.let { entrenamiento ->
            scope.launch {
                try {
                    isLoading = true

                    // 1. Dar de baja
                    val success = entrenamientosViewModel.darDeBajaEntrenamiento(entrenamiento._id)

                    // 2. SIEMPRE recargar los datos (sin importar si fue exitoso o no)
                    val filtros = mapOf(
                        "creador" to (usuario?._id ?: ""),
                        "baja" to "false"
                    )
                    entrenamientosViewModel.getFilter(filtros)

                } catch (e: Exception) {
                    Log.e("MisEntrenamientos", "Error: ${e.message}")
                } finally {
                    isLoading = false
                    showDeleteDialog = false
                    entrenamientoAEliminar = null
                }
            }
        }
        Unit
    }

    // ✅ Estado simplificado
    val screenState = MisEntrenamientosScreenState(
        isLoading = isLoading,
        entrenamientos = misEntrenamientos
    )

    MisEntrenamientosContent(
        state = screenState,
        onBackClick = remember { { navController.popBackStack() } },
        onCreateTrainingClick = remember { { navController.navigate("crearEntrenamiento") } },
        onEntrenamientoClick = remember { { id -> navController.navigate("crearEntrenamiento?id=$id&publicar=true") } },
        onDeleteEntrenamiento = onDeleteEntrenamiento
    )

    // Diálogo de confirmación
    if (showDeleteDialog && entrenamientoAEliminar != null) {
        DarDeBajaConfirmationDialog(
            entrenamiento = entrenamientoAEliminar!!,
            onConfirm = onConfirmDelete,
            onDismiss = {
                showDeleteDialog = false
                entrenamientoAEliminar = null
            }
        )
    }
}

// Modelo de estado para la UI
data class MisEntrenamientosScreenState(
    val isLoading: Boolean,
    val entrenamientos: List<Entrenamientos>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEntrenamientosContent(
    state: MisEntrenamientosScreenState,
    onBackClick: () -> Unit,
    onCreateTrainingClick: () -> Unit,
    onEntrenamientoClick: (String) -> Unit,
    onDeleteEntrenamiento: (Entrenamientos) -> Unit
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
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Entrenamientos",
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
                ),
                actions = {
                    IconButton(onClick = onCreateTrainingClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear entrenamiento",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> LoadingContent()
                state.entrenamientos.isEmpty() -> EmptyContent(onCreateClick = onCreateTrainingClick)
                else -> EntrenamientosList(
                    entrenamientos = state.entrenamientos,
                    onEntrenamientoClick = onEntrenamientoClick,
                    onDeleteEntrenamiento = onDeleteEntrenamiento
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFAB47BC))
    }
}

@Composable
private fun EmptyContent(onCreateClick: () -> Unit) {
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
                contentDescription = "No hay entrenamientos",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes entrenamientos activos",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Toca el botón + para crear tu primer entrenamiento",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B1FA2)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Crear entrenamiento")
            }
        }
    }
}

@Composable
private fun EntrenamientosList(
    entrenamientos: List<Entrenamientos>,
    onEntrenamientoClick: (String) -> Unit,
    onDeleteEntrenamiento: (Entrenamientos) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = entrenamientos,
            key = { it._id }
        ) { entrenamiento ->
            SwipeToDeleteEntrenamientoCard(
                entrenamiento = entrenamiento,
                onClick = { onEntrenamientoClick(entrenamiento._id) },
                onDelete = { onDeleteEntrenamiento(entrenamiento) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteEntrenamientoCard(
    entrenamiento: Entrenamientos,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // No eliminar automáticamente, esperamos confirmación
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFFFF9800), // Color naranja para indicar "dar de baja"
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Archive, // Icono más apropiado para "dar de baja"
                    contentDescription = "Dar de baja",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        EntrenamientoCard(
            entrenamiento = entrenamiento,
            onClick = onClick
        )
    }
}


@Composable
private fun DarDeBajaConfirmationDialog(
    entrenamiento: Entrenamientos,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Dar de baja entrenamiento",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que quieres dar de baja \"${entrenamiento.nombre}\"? El entrenamiento será ocultado pero no se eliminará definitivamente. Los usuarios que ya lo hayan realizado no se verán afectados.",
                color = Color.Gray
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF9800) // Color naranja para indicar que es una acción de baja
                )
            ) {
                Text("Dar de baja")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFAB47BC)
                )
            ) {
                Text("Cancelar")
            }
        },
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.Gray
    )
}