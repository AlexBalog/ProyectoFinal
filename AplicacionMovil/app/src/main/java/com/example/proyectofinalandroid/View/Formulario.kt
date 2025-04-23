package com.example.proyectofinalandroid.View


import android.content.Context
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import android.util.Base64
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.withContext
import java.time.Period
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.proyectofinalandroid.utils.uriToBase64
import kotlin.math.pow


data class ActivityLevel(
    val name: String,
    val icon: ImageVector,
    val description: String
)

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(navController: NavController) {

    val parentEntry = remember(navController) {
        navController.getBackStackEntry("root")
    }
    val usuariosViewModel: UsuariosViewModel = hiltViewModel(parentEntry)

    // Estados para los campos del formulario
    var fotoPerfil by remember { mutableStateOf<Uri?>(null) }
    var fechaNacimiento by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var objetivoPeso by remember { mutableStateOf("") }
    var objetivoTiempo by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var nivelActividad by remember { mutableStateOf("") }
    var objetivoCalorias by remember { mutableStateOf("") }

    val activityOptions = listOf(
        ActivityLevel(
            "Sedentario",
            Icons.Default.SelfImprovement,
            "Poco o ningún ejercicio"
        ),
        ActivityLevel(
            "Ligero",
            Icons.Default.DirectionsWalk,
            "Ejercicio ligero 1-3 días/semana"
        ),
        ActivityLevel(
            "Moderado",
            Icons.Default.DirectionsRun,
            "Ejercicio moderado 3-5 días/semana"
        ),
        ActivityLevel(
            "Intenso",
            Icons.Default.FitnessCenter,
            "Ejercicio intenso 6-7 días/semana"
        ),
        ActivityLevel(
            "Muy intenso",
            Icons.Default.SportsMartialArts,
            "Ejercicio muy intenso diario"
        )
    )

    // Estados de error para validación
    var alturaError by remember { mutableStateOf<String?>(null) }
    var pesoError by remember { mutableStateOf<String?>(null) }
    var objetivoPesoError by remember { mutableStateOf<String?>(null) }
    var objetivoTiempoError by remember { mutableStateOf<String?>(null) }

    // Estados para la UI
    var isLoading by remember { mutableStateOf(false) }
    var isAnimatedIn by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerState = rememberDatePickerState()

    // Contexto y scroll
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Lanzador para seleccionar imagen
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoPerfil = uri
    }

    // Configurar animación de entrada
    LaunchedEffect(Unit) {
        delay(100)
        isAnimatedIn = true
    }


    // Funciones de validación
    fun validateAltura(value: String): String? {
        return when {
            value.isEmpty() -> null
            value.toIntOrNull() == null -> "Ingrese un número válido"
            value.toInt() < 50 -> "La altura mínima es 50 cm"
            value.toInt() > 250 -> "La altura máxima es 250 cm"
            else -> null
        }
    }

    fun validatePeso(value: String): String? {
        return when {
            value.isEmpty() -> null
            value.toFloatOrNull() == null -> "Ingrese un número válido"
            value.toFloat() < 30 -> "El peso mínimo es 30 kg"
            value.toFloat() > 300 -> "El peso máximo es 300 kg"
            else -> null
        }
    }

    fun validateObjetivoPeso(value: String): String? {
        return when {
            value.isEmpty() -> null
            value.toFloatOrNull() == null -> "Ingrese un número válido"
            value.toFloat() < 30 -> "El peso mínimo es 30 kg"
            value.toFloat() > 300 -> "El peso máximo es 300 kg"
            else -> null
        }
    }

    fun validateObjetivoTiempo(value: String): String? {
        return when {
            value.isEmpty() -> null
            value.toIntOrNull() == null -> "Ingrese un número válido"
            value.toInt() < 1 -> "Mínimo 1 semana"
            value.toInt() > 52 -> "Máximo 52 semanas (1 año)"
            else -> null
        }
    }

    fun calcularEdad(fechaNacimiento: LocalDate): Int {
        val hoy = LocalDate.now()
        return Period.between(fechaNacimiento, hoy).years
    }

    // Función para guardar el formulario
    suspend fun guardarFormulario() {
        // Validar todos los campos antes de guardar
        val validAltura = validateAltura(altura)
        val validPeso = validatePeso(peso)
        val validObjetivoPeso = validateObjetivoPeso(objetivoPeso)
        val validObjetivoTiempo = validateObjetivoTiempo(objetivoTiempo)

        alturaError = validAltura
        pesoError = validPeso
        objetivoPesoError = validObjetivoPeso
        objetivoTiempoError = validObjetivoTiempo

        // Solo proceder si no hay errores
        if (validAltura == null && validPeso == null &&
            validObjetivoPeso == null && validObjetivoTiempo == null) {

            isLoading = true

            try {
                val fotoBase64 = fotoPerfil?.let { uriToBase64(context, it) } ?: ""
                val edad = try {
                    calcularEdad(stringToLocalDate(fechaNacimiento))
                } catch (e: Exception) {
                    Toast.makeText(context, "Fecha de nacimiento inválida", Toast.LENGTH_LONG).show()
                    isLoading = false
                    return
                }

                // Calcular TMB
                val TMB = when (sexo) {
                    "Masculino" -> 10f * peso.toFloat() + 6.25f * altura.toFloat() - 5f * edad + 5f
                    "Femenino" -> 10f * peso.toFloat() + 6.25f * altura.toFloat() - 5f * edad - 161f
                    else -> 0f
                }

                val caloriasMantenimiento = when (nivelActividad) {
                    "Sedentario" -> TMB * 1.2f
                    "Ligero" -> TMB * 1.375f
                    "Moderado" -> TMB * 1.55f
                    "Intenso" -> TMB * 1.725f
                    "Muy intenso" -> TMB * 1.9f
                    else -> TMB
                }

                val calcularCalorias = (7700f * (objetivoPeso.toFloat() - peso.toFloat())) / (objetivoTiempo.toFloat() * 7f)

                val datos = mutableMapOf<String, String>().apply {
                    put("foto", fotoBase64)
                    put("fechaNacimiento", fechaNacimiento)
                    put("sexo", sexo)
                    put("altura", altura)
                    put("peso", peso)
                    put("nivelActividad", nivelActividad)
                    put("objetivoPeso", objetivoPeso)
                    put("objetivoTiempo", objetivoTiempo)
                    put("caloriasMantenimiento", caloriasMantenimiento.toString())
                    put("objetivoCalorias", (caloriasMantenimiento + calcularCalorias).toString())
                    put("IMC", ((peso.toFloat() / ((altura.toFloat() / 100).pow(2))).toString()))
                    put("formulario", true.toString())
                }

                val exito = withContext(Dispatchers.Main) {
                    usuariosViewModel.update(datos)
                }

                if (exito) {
                    Toast.makeText(context, "Gracias por tu paciencia!!", Toast.LENGTH_SHORT).show()
                    navController.navigate("principal") {
                        popUpTo("profileForm") { inclusive = true }
                    }
                } else {
                    Toast.makeText(context, "No se pudo guardar, inténtalo más tarde.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        }
    }


    // Función para omitir formulario
    fun omitirFormulario() {
        navController.navigate("principal") {
            popUpTo("profileForm") { inclusive = true }
        }
    }

    // UI principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Header con logo pequeño y nombre de la empresa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp) // Increased vertical padding
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo más grande
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo FitSphere",
                modifier = Modifier
                    .size(70.dp) // Increased size from 50dp to 70dp
                    .padding(4.dp, 8.dp, 0.dp, 0.dp)
            )

            // Nombre de la empresa (centrado verticalmente)
            Text(
                text = "FitSphere",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp, // Increased font size to match larger logo
                    fontFamily = FontFamily.Default,
                    shadow = Shadow(
                        color = Color(0xFF7B1FA2),
                        blurRadius = 12f,
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f)
                    ),
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFAB47BC), // Morado más claro
                            Color(0xFF7B1FA2)  // Morado más oscuro
                        )
                    ),
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Espacio para equilibrar el layout
            Spacer(modifier = Modifier.width(50.dp)) // Match logo width
        }

        // Contenido principal animado
        AnimatedVisibility(
            visible = isAnimatedIn,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp) // Increased space for the header
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título del formulario
                Text(
                    text = "Completa tu perfil",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Formulario en una Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF7B1FA2),
                            spotColor = Color(0xFF7B1FA2)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Foto de perfil
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A2A2A))
                                .border(2.dp, Color(0xFFAB47BC), CircleShape)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoPerfil != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(fotoPerfil),
                                    contentDescription = "Foto de Perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Añadir Foto",
                                    tint = Color(0xFFAB47BC),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Text(
                            text = "Toca para añadir foto",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )

                        // DatePicker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = fechaNacimiento,
                                onValueChange = { /* No permitir escritura directa */ },
                                label = { Text("Fecha de nacimiento", color = Color.Gray) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Fecha",
                                        tint = Color(0xFFAB47BC)
                                    )
                                },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFFAB47BC),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFFAB47BC),
                                    selectionColors = TextSelectionColors(
                                        handleColor = Color(0xFFAB47BC),
                                        backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                    )
                                )
                            )

                            // Overlay transparente que captura los clics en todo el campo
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // Sin indicación visual de clic
                                    ) {
                                        showDatePicker = true
                                    }
                            )
                        }

                        // Selección de sexo
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Sexo",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Opción Masculino
                                OutlinedButton(
                                    onClick = { sexo = "Masculino" },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (sexo == "Masculino") Color(0xFF7B1FA2) else Color.Transparent,
                                        contentColor = if (sexo == "Masculino") Color.White else Color(0xFFAB47BC)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFAB47BC))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Male,
                                        contentDescription = "Masculino",
                                        tint = if (sexo == "Masculino") Color.White else Color(0xFFAB47BC)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Masculino")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Opción Femenino
                                OutlinedButton(
                                    onClick = { sexo = "Femenino" },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (sexo == "Femenino") Color(0xFF7B1FA2) else Color.Transparent,
                                        contentColor = if (sexo == "Femenino") Color.White else Color(0xFFAB47BC)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFAB47BC))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Female,
                                        contentDescription = "Femenino",
                                        tint = if (sexo == "Femenino") Color.White else Color(0xFFAB47BC)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Femenino")
                                }
                            }
                        }

                        // Altura con validación
                        OutlinedTextField(
                            value = altura,
                            onValueChange = {
                                // Solo permitir hasta 3 dígitos
                                if (it.length <= 3 && it.all { char -> char.isDigit() }) {
                                    altura = it
                                    alturaError = validateAltura(it)
                                }
                            },
                            label = { Text("Altura (cm)", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Height,
                                    contentDescription = "Altura",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (alturaError == null) Color(0xFFAB47BC) else Color.Red,
                                unfocusedBorderColor = if (alturaError == null) Color.Gray else Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFAB47BC),
                                selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFFAB47BC),
                                    backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                )
                            ),
                            textStyle = TextStyle(color = Color.White),
                            isError = alturaError != null,
                            supportingText = {
                                if (alturaError != null) {
                                    Text(
                                        text = alturaError!!,
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Peso con validación
                        OutlinedTextField(
                            value = peso,
                            onValueChange = {
                                // Permitir números con un punto decimal
                                val regex = Regex("^\\d{0,3}(\\.\\d{0,1})?\$")
                                if (it.isEmpty() || regex.matches(it)) {
                                    peso = it
                                    pesoError = validatePeso(it)
                                }
                            },
                            label = { Text("Peso (kg)", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = "Peso",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (pesoError == null) Color(0xFFAB47BC) else Color.Red,
                                unfocusedBorderColor = if (pesoError == null) Color.Gray else Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFAB47BC),
                                selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFFAB47BC),
                                    backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                )
                            ),
                            textStyle = TextStyle(color = Color.White),
                            isError = pesoError != null,
                            supportingText = {
                                if (pesoError != null) {
                                    Text(
                                        text = pesoError!!,
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Objetivo de peso con validación
                        OutlinedTextField(
                            value = objetivoPeso,
                            onValueChange = {
                                // Permitir números con un punto decimal
                                val regex = Regex("^\\d{0,3}(\\.\\d{0,1})?\$")
                                if (it.isEmpty() || regex.matches(it)) {
                                    objetivoPeso = it
                                    objetivoPesoError = validateObjetivoPeso(it)
                                }
                            },
                            label = { Text("Objetivo de peso (kg)", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = "Objetivo de peso",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (objetivoPesoError == null) Color(0xFFAB47BC) else Color.Red,
                                unfocusedBorderColor = if (objetivoPesoError == null) Color.Gray else Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFAB47BC),
                                selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFFAB47BC),
                                    backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                )
                            ),
                            textStyle = TextStyle(color = Color.White),
                            isError = objetivoPesoError != null,
                            supportingText = {
                                if (objetivoPesoError != null) {
                                    Text(
                                        text = objetivoPesoError!!,
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        var selectedIcon by remember { mutableStateOf<ImageVector?>(null) }
                        var selectedDescription by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Nivel de actividad",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                // Campo de texto que actúa como el trigger del dropdown
                                OutlinedTextField(
                                    value = nivelActividad,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = {
                                        if (nivelActividad.isEmpty()) {
                                            Text("Tu actividad física", color = Color.Gray)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = selectedIcon ?: Icons.Default.FitnessCenter, // Icono placeholder
                                            contentDescription = "Nivel de actividad",
                                            tint = Color(0xFFAB47BC)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expandir",
                                            tint = Color(0xFFAB47BC)
                                        )
                                    },
                                    supportingText = {
                                        if (selectedDescription.isNotEmpty()) {
                                            Text(
                                                text = selectedDescription,
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null // Sin indicación visual de clic
                                        ) {
                                            expandedDropdown = !expandedDropdown
                                        },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFFAB47BC),
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color(0xFFAB47BC),
                                        selectionColors = TextSelectionColors(
                                            handleColor = Color(0xFFAB47BC),
                                            backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                        )
                                    )
                                )

                                // Overlay transparente que captura los clics en todo el campo
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null // Sin indicación visual de clic
                                        ) {
                                            expandedDropdown = !expandedDropdown
                                        }
                                )

                                // Menú desplegable con iconos
                                DropdownMenu(
                                    expanded = expandedDropdown,
                                    onDismissRequest = { expandedDropdown = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.95f)
                                        .background(Color(0xFF2A2A2A))
                                ) {
                                    activityOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = option.name,
                                                    color = if (nivelActividad == option.name) Color(0xFFAB47BC) else Color.White
                                                )
                                            },
                                            onClick = {
                                                nivelActividad = option.name
                                                selectedIcon = option.icon
                                                selectedDescription = option.description
                                                expandedDropdown = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = option.icon,
                                                    contentDescription = option.name,
                                                    tint = if (nivelActividad == option.name) Color(0xFFAB47BC) else Color.White
                                                )
                                            },
                                            modifier = Modifier
                                                .background(
                                                    if (nivelActividad == option.name)
                                                        Color(0xFF7B1FA2).copy(alpha = 0.2f)
                                                    else
                                                        Color.Transparent
                                                )
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // Objetivo de tiempo (semanas) con validación
                        OutlinedTextField(
                            value = objetivoTiempo,
                            onValueChange = {
                                // Permitir hasta 2 dígitos (máximo 52 semanas)
                                if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                    objetivoTiempo = it
                                    objetivoTiempoError = validateObjetivoTiempo(it)
                                }
                            },
                            label = { Text("Objetivo de tiempo (semanas)", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Objetivo de tiempo",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = if (objetivoTiempoError == null) Color(0xFFAB47BC) else Color.Red,
                                unfocusedBorderColor = if (objetivoTiempoError == null) Color.Gray else Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFFAB47BC),
                                selectionColors = TextSelectionColors(
                                    handleColor = Color(0xFFAB47BC),
                                    backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                                )
                            ),
                            textStyle = TextStyle(color = Color.White),
                            isError = objetivoTiempoError != null,
                            supportingText = {
                                if (objetivoTiempoError != null) {
                                    Text(
                                        text = objetivoTiempoError!!,
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )

                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón Rellenar más tarde
                            OutlinedButton(
                                onClick = { omitirFormulario() },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFAB47BC)
                                ),
                                contentPadding = PaddingValues(horizontal = 0.dp),
                                border = BorderStroke(0.dp, Color.Transparent)
                            ) {
                                Text(
                                    text = "Rellenar más tarde",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Botón Guardar
                            Button(
                                onClick = { CoroutineScope(Dispatchers.Main).launch {
                                    guardarFormulario()
                                } },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7B1FA2),
                                    disabledContainerColor = Color(0xFF7B1FA2).copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        "Guardar",
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Texto de pie de página
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "© 2025 FitSphere",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f)
                )
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Formatear fecha seleccionada
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        fechaNacimiento = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Color(0xFFAB47BC))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Color(0xFFAB47BC))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.Gray,
                subheadContentColor = Color.LightGray,
                yearContentColor = Color.White,
                currentYearContentColor = Color(0xFFAB47BC),
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = Color(0xFF7B1FA2),
                dayContentColor = Color.White,
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF7B1FA2),
                todayContentColor = Color(0xFFAB47BC),
                todayDateBorderColor = Color(0xFFAB47BC)
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.Gray,
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF7B1FA2),
                    todayContentColor = Color(0xFFAB47BC),
                    todayDateBorderColor = Color(0xFFAB47BC),
                    dateTextFieldColors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            errorTextColor = Color.Red,
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                            disabledPlaceholderColor = Color.White.copy(alpha = 0.4f),
                            errorPlaceholderColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            cursorColor = Color(0xFFAB47BC),
                            selectionColors = TextSelectionColors(
                                handleColor = Color(0xFFAB47BC),
                                backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.4f)
                            )
                        )
                )
            )
        }
    }
}

fun stringToLocalDate(fecha: String): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return LocalDate.parse(fecha, formatter)
}