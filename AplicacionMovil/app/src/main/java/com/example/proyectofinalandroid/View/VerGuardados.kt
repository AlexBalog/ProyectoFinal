package com.example.proyectofinalandroid.View

import android.annotation.SuppressLint
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
import com.example.proyectofinalandroid.ViewModel.GuardadosViewModel
import android.util.Log

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientosGuardadosScreen(
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
    val guardadosViewModel: GuardadosViewModel = hiltViewModel()

    val usuario by usuariosViewModel.usuario.collectAsState()
    val guardados by guardadosViewModel.guardados.collectAsState()
    val scope = rememberCoroutineScope()

    // Estados para manejar los entrenamientos guardados
    var entrenamientosGuardados by remember { mutableStateOf<List<Entrenamientos>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Cargar guardados cuando el usuario esté disponible
    LaunchedEffect(usuario) {
        usuario?.let { currentUser ->
            isLoading = true
            try {
                // Establecer usuario en el ViewModel y obtener sus guardados
                guardadosViewModel.setUsuario(currentUser)
                guardadosViewModel.getGuardadosByUsuario(currentUser._id)
            } finally {
                isLoading = false
            }
        }
    }

    // Obtener los entrenamientos completos cuando cambien los guardados
    LaunchedEffect(guardados, usuario) {
        val guardadosList = guardados
        val currentUser = usuario

        if (guardadosList != null && currentUser != null && guardadosList.isNotEmpty()) {
            isLoading = true
            scope.launch {
                try {
                    val entrenamientosCompletos = mutableListOf<Entrenamientos>()

                    // Para cada guardado, obtener el entrenamiento completo
                    for (guardado in guardadosList) {
                        try {
                            val entrenamiento = entrenamientosViewModel.getEntrenamientoById(guardado.entrenamiento)
                            entrenamiento?.let {
                                // Solo agregar entrenamientos que no estén dados de baja
                                if (!it.baja) {
                                    entrenamientosCompletos.add(it)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("EntrenamientosGuardados", "Error al obtener entrenamiento ${guardado.entrenamiento}: ${e.message}")
                        }
                    }

                    entrenamientosGuardados = entrenamientosCompletos
                } catch (e: Exception) {
                    Log.e("EntrenamientosGuardados", "Error al cargar entrenamientos: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        } else if (guardadosList != null && guardadosList.isEmpty()) {
            // Si la lista está vacía, actualizar el estado
            entrenamientosGuardados = emptyList()
            isLoading = false
        }
    }

    // Estado simplificado para la UI
    val screenState = EntrenamientosGuardadosScreenState(
        isLoading = isLoading,
        entrenamientos = entrenamientosGuardados
    )

    EntrenamientosGuardadosContent(
        state = screenState,
        onBackClick = remember { { navController.popBackStack() } },
        onEntrenamientoClick = remember { { id -> navController.navigate("detalleEntrenamiento/$id") } }
    )

}

// Modelo de estado para la UI
data class EntrenamientosGuardadosScreenState(
    val isLoading: Boolean,
    val entrenamientos: List<Entrenamientos>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientosGuardadosContent(
    state: EntrenamientosGuardadosScreenState,
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
            TopAppBar(
                title = {
                    Text(
                        text = "Entrenamientos Guardados",
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

            when {
                state.isLoading -> LoadingContent()
                state.entrenamientos.isEmpty() -> EmptyGuardadosContent()
                else -> EntrenamientosGuardadosList(
                    entrenamientos = state.entrenamientos,
                    onEntrenamientoClick = onEntrenamientoClick
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
private fun EmptyGuardadosContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "No hay entrenamientos guardados",
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes entrenamientos guardados",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explora entrenamientos y guarda tus favoritos",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EntrenamientosGuardadosList(
    entrenamientos: List<Entrenamientos>,
    onEntrenamientoClick: (String) -> Unit
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
            EntrenamientoCard(
                entrenamiento = entrenamiento,
                onClick = { onEntrenamientoClick(entrenamiento._id) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EntrenamientoGuardadoCard(
    entrenamiento: Entrenamientos,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entrenamiento.nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Músculo principal: ${entrenamiento.musculoPrincipal}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Categoría: ${entrenamiento.categoria}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Entrenamiento guardado",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Duración",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entrenamiento.duracion} min",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Likes",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entrenamiento.likes}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}