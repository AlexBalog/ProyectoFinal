package com.example.proyectofinalandroid.View

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
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
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    navController: NavController,
    usuariosViewModel: UsuariosViewModel = hiltViewModel()
) {
    // Estados para los campos del formulario
    var fotoPerfil by remember { mutableStateOf<Uri?>(null) }
    var fechaNacimiento by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var imc by remember { mutableStateOf("") }
    var objetivoPeso by remember { mutableStateOf("") }
    var objetivoTiempo by remember { mutableStateOf("") }
    var objetivoCalorias by remember { mutableStateOf("") }

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

    // Función para convertir imagen a Base64
    fun convertImageToBase64(uri: Uri): String {
        val bitmap = ImageRequest.Builder(context)
            .data(uri)
            .build()
            .context
            .assets
            .open(uri.path ?: "")
            .use { it.readBytes() }
            .let {
                android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
            }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Función para calcular IMC
    fun calcularIMC() {
        if (altura.isNotEmpty() && peso.isNotEmpty()) {
            val alturaMetros = altura.toFloat() / 100
            val pesoKg = peso.toFloat()
            val imcCalculado = pesoKg / (alturaMetros * alturaMetros)
            imc = ((imcCalculado * 10).roundToInt() / 10.0f).toString()
        }
    }

    // Función para convertir fecha de nacimiento a edad
    fun fechaNacimientoAEdad(fecha: String): Int {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fechaNac = LocalDate.parse(fecha, formatter)
        return LocalDate.now().year - fechaNac.year
    }

    // Función para calcular calorías objetivo diarias
    fun calcularCalorias() {
        if (peso.isNotEmpty() && altura.isNotEmpty() && fechaNacimiento.isNotEmpty() && sexo.isNotEmpty()) {
            val pesoKg = peso.toFloat()
            val alturaMetros = altura.toFloat()
            val edad = fechaNacimientoAEdad(fechaNacimiento)

            // Fórmula de Harris-Benedict
            val tmb = if (sexo == "Masculino") {
                (88.362 + (13.397 * pesoKg) + (4.799 * alturaMetros) - (5.677 * edad)).roundToInt()
            } else {
                (447.593 + (9.247 * pesoKg) + (3.098 * alturaMetros) - (4.330 * edad)).roundToInt()
            }

            // Ajuste según objetivo (si quiere perder o ganar peso)
            val caloriasObjetivo = when {
                objetivoPeso.isNotEmpty() && objetivoPeso.toFloat() < peso.toFloat() -> (tmb * 0.8).roundToInt() // Déficit calórico
                objetivoPeso.isNotEmpty() && objetivoPeso.toFloat() > peso.toFloat() -> (tmb * 1.2).roundToInt() // Superávit calórico
                else -> tmb // Mantenimiento
            }

            objetivoCalorias = caloriasObjetivo.toString()
        }
    }

    // Función para guardar el formulario
    suspend fun guardarFormulario() {
        isLoading = true

        // Aquí se guardarían los datos en la base de datos o ViewModel
        val fotoBase64 = fotoPerfil?.let { convertImageToBase64(it) } ?: ""

        // Simular guardado
        try {
            // Aquí llamaríamos a la función correspondiente del ViewModel
            // usuariosViewModel.guardarPerfil(...)

            delay(1000) // Simular tiempo de guardado
            navController.navigate("vistaBuscador") {
                popUpTo("profileForm") { inclusive = true }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    // Función para omitir formulario
    fun omitirFormulario() {
        // Marcar el formulario como false en la base de datos
        // usuariosViewModel.setFormularioCompletado(false)

        navController.navigate("vistaBuscador") {
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo pequeño
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo FitSphere",
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp)
            )

            // Nombre de la empresa
            Text(
                text = "FitSphere",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Default,
                    shadow = Shadow(
                        color = Color(0xFF7B1FA2),
                        blurRadius = 4f,
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f)
                    ),
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFAB47BC), // Morado más claro
                            Color(0xFF7B1FA2)  // Morado más oscuro
                        )
                    ),
                ),
                modifier = Modifier.padding(start = 8.dp)
            )

            // Espacio para equilibrar el layout
            Spacer(modifier = Modifier.width(50.dp))
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
                .padding(top = 70.dp) // Dejar espacio para el header
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

                        // Fecha de nacimiento
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = { },
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
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(bottom = 12.dp),
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

                        // Altura
                        OutlinedTextField(
                            value = altura,
                            onValueChange = {
                                altura = it.filter { char -> char.isDigit() }
                                calcularIMC()
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

                        // Peso
                        OutlinedTextField(
                            value = peso,
                            onValueChange = {
                                peso = it.filter { char -> char.isDigit() || char == '.' }
                                calcularIMC()
                                calcularCalorias()
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

                        // IMC (calculado)
                        OutlinedTextField(
                            value = imc,
                            onValueChange = { },
                            label = { Text("IMC", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SettingsAccessibility,
                                    contentDescription = "IMC",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFAB47BC),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledBorderColor = Color.Gray,
                                disabledTextColor = Color.LightGray
                            )
                        )

                        // Objetivo de peso
                        OutlinedTextField(
                            value = objetivoPeso,
                            onValueChange = {
                                objetivoPeso = it.filter { char -> char.isDigit() || char == '.' }
                                calcularCalorias()
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

                        // Objetivo de tiempo (semanas)
                        OutlinedTextField(
                            value = objetivoTiempo,
                            onValueChange = { objetivoTiempo = it.filter { char -> char.isDigit() } },
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
                                .padding(bottom = 12.dp),
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

                        // Objetivo de calorías (calculado)
                        OutlinedTextField(
                            value = objetivoCalorias,
                            onValueChange = { },
                            label = { Text("Objetivo de calorías (diarias)", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Calorías",
                                    tint = Color(0xFFAB47BC)
                                )
                            },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFAB47BC),
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledBorderColor = Color.Gray,
                                disabledTextColor = Color.LightGray
                            )
                        )

                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón Rellenar más tarde
                            OutlinedButton(
                                onClick = { omitirFormulario() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFAB47BC)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFAB47BC))
                            ) {
                                Text("Rellenar más tarde")
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
                                shape = RoundedCornerShape(8.dp),
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
                        calcularCalorias()
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
            )
        }
    }
}