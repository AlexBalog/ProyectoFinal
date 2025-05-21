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

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEntrenamientosScreen(
    navController: NavController
) {
    // 1. Usa remember para estabilizar las referencias de ViewModels
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val mainEntry = remember(navController) {
        navController.getBackStackEntry("main")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel(mainEntry)

    val usuario by usuariosViewModel.usuario.collectAsState()
    // 2. Estabiliza el scope con remember
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Esto garantiza que siempre se carguen los datos al entrar a la pantalla
        scope.launch {
            usuario?.let { currentUser ->
                val filtros = mapOf("creador" to (currentUser._id ?: ""))
                entrenamientosViewModel.getFilter(filtros)
            }
        }
    }
    // 3. Define un estado derivado para la UI
    val screenState = produceState(
        initialValue = MisEntrenamientosScreenState(isLoading = true, entrenamientos = emptyList())
    ) {
        val usuarioFlow = usuariosViewModel.usuario
        val entrenamientosFlow = entrenamientosViewModel.entrenamientos

        // Usa collectLatest para evitar recolecciones innecesarias
        usuarioFlow.collect { usuario ->
            if (usuario != null) {
                entrenamientosViewModel.setUsuario(usuario)
                val filtros = mapOf("creador" to (usuario._id ?: ""))
                entrenamientosViewModel.getFilter(filtros)

                // Actualiza el estado solo cuando los datos cambian realmente
                entrenamientosFlow.collect { entrenamientosResult ->
                    value = MisEntrenamientosScreenState(
                        isLoading = false,
                        entrenamientos = entrenamientosResult ?: emptyList()
                    )
                }
            }
        }
    }

    // 4. Extrae la UI a componentes estables
    MisEntrenamientosContent(
        state = screenState.value,
        onBackClick = remember { { navController.popBackStack() } },
        onCreateTrainingClick = remember { { navController.navigate("crearEntrenamiento") } },
        onEntrenamientoClick = remember { { id -> navController.navigate("crearEntrenamiento?id=$id&publicar=true") } }
    )
}

// 5. Define un modelo de estado para la UI
data class MisEntrenamientosScreenState(
    val isLoading: Boolean,
    val entrenamientos: List<Entrenamientos>
)

// 6. Extrae la UI a un componente separado que solo recibe datos inmutables y callbacks estables
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEntrenamientosContent(
    state: MisEntrenamientosScreenState,
    onBackClick: () -> Unit,
    onCreateTrainingClick: () -> Unit,
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
            // TopAppBar estable
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

            // Contenido principal basado en el estado
            when {
                state.isLoading -> LoadingContent()
                state.entrenamientos.isEmpty() -> EmptyContent(onCreateClick = onCreateTrainingClick)
                else -> EntrenamientosList(
                    entrenamientos = state.entrenamientos,
                    onEntrenamientoClick = onEntrenamientoClick
                )
            }
        }
    }
}

// 7. Componentes de UI individuales para cada estado
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
                text = "No has creado entrenamientos",
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
    onEntrenamientoClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = entrenamientos,
            // 8. Usa una key estable para los items de la lista
            key = { it._id }
        ) { entrenamiento ->
            // 9. Memoriza la función onClick para cada item
            val onClick = remember(entrenamiento._id) {
                { onEntrenamientoClick(entrenamiento._id) }
            }

            EntrenamientoCard(
                entrenamiento = entrenamiento,
                onClick = onClick
            )
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}