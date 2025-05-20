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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.proyectofinalandroid.Model.Entrenamientos
import com.example.proyectofinalandroid.Model.Ejercicios
import com.example.proyectofinalandroid.ViewModel.EjerciciosViewModel
import com.example.proyectofinalandroid.ViewModel.EntrenamientosViewModel
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.ImageBitmap
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.utils.base64ToImageBitmap


@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscadorScreen(
    navController: NavController,
    tipoBusqueda: String? = null,
    categoria: String? = null,
    musculo: String? = null
) {
    // Obtener ViewModels
    val userEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(userEntry)
    val entrenamientosViewModel: EntrenamientosViewModel = hiltViewModel()
    val ejerciciosViewModel: EjerciciosViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // Obtener usuario actual
    val usuario by usuariosViewModel.usuario.collectAsState()

    // Inicializar los ViewModels con el usuario actual si aún no están inicializados
    LaunchedEffect(usuario) {
        usuario?.let {
            entrenamientosViewModel.setUsuario(it)
            ejerciciosViewModel.setUsuario(it)
        }
    }

    // Estados para los filtros
    var searchQuery by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf(tipoBusqueda ?: "entrenamientos") }
    var selectedCategory by remember { mutableStateOf(categoria) }
    var selectedMuscle by remember { mutableStateOf(musculo) }
    var durationRange by remember { mutableStateOf(0f..180f) }
    var sortBy by remember { mutableStateOf("nombre") }
    var sortDirection by remember { mutableStateOf("asc") }

    // Estados para UI
    var showFilters by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados observables de los ViewModels
    val entrenamientos by entrenamientosViewModel.entrenamientos.collectAsState()
    val ejercicios by ejerciciosViewModel.ejercicios.collectAsState()

    // Lista de categorías y músculos (esto debería venir de algún lugar en tu app)
    val categorias = listOf("Cardio", "Fuerza", "Resistencia", "Flexibilidad", "Hipertrofia", "Fullbody")
    val musculos = listOf("Pecho", "Espalda", "Hombros", "Biceps", "Triceps", "Piernas", "Abdominales", "Glúteos", "Core")

    // Función para aplicar filtros
    fun applyFilters() {
        val filters = mutableMapOf<String, String>()

        if (searchQuery.isNotEmpty()) {
            filters["nombre"] = searchQuery
        }

        selectedCategory?.let {
            filters["categoria"] = it
        }

        selectedMuscle?.let {
            filters["musculo"] = it
        }

        if (searchType == "entrenamientos" && (durationRange.start > 0f || durationRange.endInclusive < 180f)) {
            filters["duracionMin"] = durationRange.start.toInt().toString()
            filters["duracionMax"] = durationRange.endInclusive.toInt().toString()
        }

        filters["sortBy"] = sortBy
        filters["sortDirection"] = sortDirection

        if (searchType == "entrenamientos") {
            scope.launch {
                Log.d("FalloEntFilter", "${filters}")
                entrenamientosViewModel.getFilter(filters)
            }
        } else {
            scope.launch {
                ejerciciosViewModel.getFilter(filters)
            }
        }
    }

    // Aplicar filtros iniciales si existen
    LaunchedEffect(tipoBusqueda, categoria, musculo) {
        delay(300) // Dar tiempo a que el ViewModel cargue los datos
        if (tipoBusqueda != null || categoria != null || musculo != null) {
            tipoBusqueda?.let { searchType = it }
            categoria?.let { selectedCategory = it }
            musculo?.let { selectedMuscle = it }
            applyFilters()
        }
        // Aplicar filtros iniciales
        isLoading = false
    }

    // UI Principal
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
            // Barra de búsqueda con icono de filtro
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF7B1FA2),
                        spotColor = Color(0xFF7B1FA2)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    TextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            scope.launch {
                                applyFilters()
                            }
                        },
                        placeholder = { Text("Buscar ${if (searchType == "entrenamientos") "entrenamientos" else "ejercicios"}") },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            cursorColor = Color(0xFFAB47BC),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    FilledIconButton(
                        onClick = { showFilters = !showFilters },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF7B1FA2)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = Color.White
                        )
                    }
                }
            }

            // Selector de tipo de búsqueda (Entrenamientos/Ejercicios)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchTypeButton(
                    text = "Entrenamientos",
                    selected = searchType == "entrenamientos",
                    onClick = {
                        searchType = "entrenamientos"
                        scope.launch {
                            applyFilters()
                        }
                    }
                )

                SearchTypeButton(
                    text = "Ejercicios",
                    selected = searchType == "ejercicios",
                    onClick = {
                        searchType = "ejercicios"
                        scope.launch {
                            applyFilters()
                        }
                    }
                )
            }

            // Panel de filtros expansible
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FilterPanel(
                    searchType = searchType,
                    categories = categorias,
                    muscles = musculos,
                    selectedCategory = selectedCategory,
                    selectedMuscle = selectedMuscle,
                    durationRange = durationRange,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    onCategorySelected = { category ->
                        selectedCategory = category
                        scope.launch {
                            applyFilters()
                        }
                    },
                    onMuscleSelected = { muscle ->
                        selectedMuscle = muscle
                        scope.launch {
                            applyFilters()
                        }
                    },
                    onDurationChanged = { range ->
                        durationRange = range
                        scope.launch {
                            applyFilters()
                        }
                    },
                    onSortChanged = { sort, direction ->
                        sortBy = sort
                        sortDirection = direction
                        scope.launch {
                            applyFilters()
                        }
                    }
                )
            }

            // Indicador de filtros activos
            ActiveFilters(
                searchType = searchType,
                selectedCategory = selectedCategory,
                selectedMuscle = selectedMuscle,
                durationRange = durationRange,
                onRemoveCategory = {
                    selectedCategory = null
                    scope.launch {
                        applyFilters()
                    }
                },
                onRemoveMuscle = {
                    selectedMuscle = null
                    scope.launch {
                        applyFilters()
                    }
                },
                onRemoveDuration = {
                    durationRange = 0f..180f
                    scope.launch {
                        applyFilters()
                    }
                }
            )

            // Contenido principal (resultados)
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFAB47BC))
                }
            } else if ((searchType == "entrenamientos" && entrenamientos.isNullOrEmpty()) ||
                (searchType == "ejercicios" && ejercicios.isNullOrEmpty())) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No hay resultados",
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron resultados",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intenta con otros filtros",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                if (searchType == "entrenamientos") {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        entrenamientos?.let { listaEntrenamientos ->
                            items(listaEntrenamientos) { entrenamiento ->
                                EntrenamientoCard(
                                    entrenamiento = entrenamiento,
                                    onClick = {
                                        navController.navigate("detalleEntrenamiento/${entrenamiento._id}")
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ejercicios?.let { listaEjercicios ->
                            items(listaEjercicios) { ejercicio ->
                                EjercicioCard(
                                    ejercicio = ejercicio,
                                    onClick = {
                                        navController.navigate("detalleEjercicio/${ejercicio._id}")
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FooterNavigation(
                navController = navController,
                currentRoute = "search",
                usuario = usuario
            )
        }
    }
}

@Composable
fun SearchTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF7B1FA2) else Color(0xFF333333),
            contentColor = if (selected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
       // modifier = Modifier.weight(1f)
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    searchType: String,
    categories: List<String>,
    muscles: List<String>,
    selectedCategory: String?,
    selectedMuscle: String?,
    durationRange: ClosedFloatingPointRange<Float>,
    sortBy: String,
    sortDirection: String,
    onCategorySelected: (String?) -> Unit,
    onMuscleSelected: (String?) -> Unit,
    onDurationChanged: (ClosedFloatingPointRange<Float>) -> Unit,
    onSortChanged: (String, String) -> Unit
) {
    val categoryExpanded = remember { mutableStateOf(false) }
    val muscleExpanded = remember { mutableStateOf(false) }
    val sortByExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Filtro por categoría (solo para entrenamientos)
            if (searchType == "entrenamientos") {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded.value,
                    onExpandedChange = { categoryExpanded.value = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "Todas las categorías",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded.value)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFAB47BC),
                            unfocusedBorderColor = Color.Gray
                        ),
                        textStyle = TextStyle(color = Color.White),
                        label = { Text("Categoría") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded.value,
                        onDismissRequest = { categoryExpanded.value = false },
                        modifier = Modifier.background(Color(0xFF2A2A2A))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las categorías", color = Color.White) },
                            onClick = {
                                onCategorySelected(null)
                                categoryExpanded.value = false
                            }
                        )

                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White) },
                                onClick = {
                                    onCategorySelected(category)
                                    categoryExpanded.value = false
                                }
                            )
                        }
                    }
                }
            }

            // Filtro por músculo (para ambos)
            ExposedDropdownMenuBox(
                expanded = muscleExpanded.value,
                onExpandedChange = { muscleExpanded.value = it }
            ) {
                OutlinedTextField(
                    value = selectedMuscle ?: "Todos los músculos",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleExpanded.value)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFAB47BC),
                        unfocusedBorderColor = Color.Gray
                    ),
                    label = { Text("Músculo") },
                    textStyle = TextStyle(color = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = muscleExpanded.value,
                    onDismissRequest = { muscleExpanded.value = false },
                    modifier = Modifier.background(Color(0xFF2A2A2A))
                ) {
                    DropdownMenuItem(
                        text = { Text("Todos los músculos", color = Color.White) },
                        onClick = {
                            onMuscleSelected(null)
                            muscleExpanded.value = false
                        }
                    )

                    muscles.forEach { muscle ->
                        DropdownMenuItem(
                            text = { Text(muscle, color = Color.White) },
                            onClick = {
                                onMuscleSelected(muscle)
                                muscleExpanded.value = false
                            }
                        )
                    }
                }
            }

            // Duración (slider para entrenamientos)
            if (searchType == "entrenamientos") {
                Column {
                    Text(
                        text = "Duración: ${durationRange.start.toInt()} - ${durationRange.endInclusive.toInt()} min",
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RangeSlider(
                        value = durationRange,
                        onValueChange = { onDurationChanged(it) },
                        valueRange = 0f..180f,
                        steps = 35,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFAB47BC),
                            activeTrackColor = Color(0xFFAB47BC),
                            inactiveTrackColor = Color.Gray
                        )
                    )
                }
            }

            // Ordenar por (para ambos tipos de búsqueda)
            if (searchType == "entrenamientos") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ordenar por:",
                        color = Color.White,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = sortByExpanded.value,
                        onExpandedChange = { sortByExpanded.value = it }
                    ) {
                        OutlinedTextField(
                            value = when (sortBy) {
                                "nombre" -> "Nombre"
                                "likes" -> "Likes"
                                else -> "Nombre"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortByExpanded.value)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFAB47BC),
                                unfocusedBorderColor = Color.Gray
                            ),
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = sortByExpanded.value,
                            onDismissRequest = { sortByExpanded.value = false },
                            modifier = Modifier.background(Color(0xFF2A2A2A))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nombre", color = Color.White) },
                                onClick = {
                                    onSortChanged("nombre", sortDirection)
                                    sortByExpanded.value = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Likes", color = Color.White) },
                                onClick = {
                                    onSortChanged("likes", sortDirection)
                                    sortByExpanded.value = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val newDirection = if (sortDirection == "asc") "desc" else "asc"
                            onSortChanged(sortBy, newDirection)
                        }
                    ) {
                        Icon(
                            imageVector = if (sortDirection == "asc") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Dirección de ordenamiento",
                            tint = Color(0xFFAB47BC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFilters(
    searchType: String,
    selectedCategory: String?,
    selectedMuscle: String?,
    durationRange: ClosedFloatingPointRange<Float>,
    onRemoveCategory: () -> Unit,
    onRemoveMuscle: () -> Unit,
    onRemoveDuration: () -> Unit
) {
    val hasActiveFilters = selectedCategory != null || selectedMuscle != null ||
            (searchType == "entrenamientos" &&
                    (durationRange.start > 0f || durationRange.endInclusive < 180f))

    if (hasActiveFilters) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filtro activo de categoría
            selectedCategory?.let {
                FilterChip(
                    label = it,
                    icon = Icons.Default.Category,
                    onRemove = onRemoveCategory
                )
            }

            // Filtro activo de músculo
            selectedMuscle?.let {
                FilterChip(
                    label = it,
                    icon = Icons.Default.FitnessCenter,
                    onRemove = onRemoveMuscle
                )
            }

            // Filtro activo de duración
            if (searchType == "entrenamientos" &&
                (durationRange.start > 0f || durationRange.endInclusive < 180f)) {
                FilterChip(
                    label = "${durationRange.start.toInt()}-${durationRange.endInclusive.toInt()} min",
                    icon = Icons.Default.Timer,
                    onRemove = onRemoveDuration
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    icon: ImageVector,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF333333),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFAB47BC),
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitar filtro",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun EntrenamientoCard(
    entrenamiento: Entrenamientos,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.height(160.dp).fillMaxWidth()
        ) {
            // Imagen de fondo
            Image(
                bitmap = base64ToImageBitmap(entrenamiento.foto as String)!!,
                contentDescription = "Imagen de entrenamiento",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )

            // Gradiente oscuro encima de la imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xE6000000)
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 0.dp)
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                // Título
                Text(
                    text = entrenamiento.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = entrenamiento.categoria,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Detalles adicionales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Duración
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entrenamiento.duracion} min",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Likes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${entrenamiento.likes}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Músculos (muestra solo el primer músculo si hay varios)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entrenamiento.musculoPrincipal,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EjercicioCard(
    ejercicio: Ejercicios,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF7B1FA2),
                spotColor = Color(0xFF7B1FA2)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Imagen del ejercicio
            Box(
                modifier = Modifier.height(160.dp).fillMaxWidth()
            ) {
                // Imagen de fondo
                Image(
                    bitmap = base64ToImageBitmap(ejercicio.foto as String)!!,
                    contentDescription = "Imagen de ejercicio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                )

                // Gradiente oscuro encima de la imagen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xE6000000)
                                ),
                                startY = 0f,
                                endY = 300f
                            )
                        )
                )


            // Información del ejercicio
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row {
                    Text(
                        text = ejercicio.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ejercicio.musculo,//.joinToString(", "),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Nombre del ejercicio




                // Fila inferior con likes y botón de detalles
                /*Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Likes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFAB47BC),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${ejercicio.likes}",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Botón ver detalles
                    FilledTonalButton(
                        onClick = onClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF7B1FA2).copy(alpha = 0.3f),
                            contentColor = Color(0xFFAB47BC)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Ver detalles",
                            fontSize = 10.sp
                        )
                    }
                }*/
            }
        }
    }
}


// Componente para mostrar recomendaciones cuando no hay filtros aplicados
@Composable
fun RecommendedSection(
    title: String,
    items: List<Any>,
    showTitle: Boolean = true,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (showTitle) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                items.firstOrNull() is Entrenamientos -> {
                    items(items as List<Entrenamientos>) { entrenamiento ->
                        EntrenamientoCard(
                            entrenamiento = entrenamiento,
                            onClick = { onItemClick(entrenamiento._id) }
                        )
                    }
                }
                items.firstOrNull() is Ejercicios -> {
                    items(items as List<Ejercicios>) { ejercicio ->
                        EjercicioCard(
                            ejercicio = ejercicio,
                            onClick = { onItemClick(ejercicio._id) }
                        )
                    }
                }
            }
        }
    }
}

// Animación de entrada para tarjetas
@Composable
fun AnimatedCardEntry(
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + androidx.compose.animation.slideInVertically(
            initialOffsetY = { it / 2 }
        )
    ) {
        content()
    }
}


// Botón de filtro flotante para móviles pequeños
@Composable
fun FloatingFilterButton(
    onClick: () -> Unit,
    filtersActive: Boolean
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = if (filtersActive) Color(0xFFAB47BC) else Color(0xFF7B1FA2),
        contentColor = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filtros"
        )
    }
}
}
