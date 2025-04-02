package com.example.proyectofinalandroid.View

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyectofinalandroid.R
import com.example.proyectofinalandroid.ViewModel.UsuariosViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    usuariosViewModel: UsuariosViewModel = viewModel(navController.getBackStackEntry("main"))
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val usuario by usuariosViewModel.usuario.collectAsState()
    val errorMessage by usuariosViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    fun autenticarUsuario() {
        usuariosViewModel.login(email, password)
    }

    // Observa cambios en usuario para navegar o mostrar mensaje de bienvenida
    LaunchedEffect(usuario) {
        if (usuario != null) {
            Toast.makeText(context, "Bienvenido ${usuario!!.nombre}", Toast.LENGTH_LONG).show()

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("usuario", usuario)

            // Navegar a la pantalla "vistaBuscador" en lugar de "DatosUser"
            navController.navigate("vistaBuscador")
        }
    }

    // Observa cambios en errorMessage y muestra el mensaje correspondiente
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Login",
            modifier = Modifier.height(200.dp)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = "Bienvenido de vuelta", fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { autenticarUsuario() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Iniciar sesión")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("RegisterUser") }) {
            Text("Registrarse")
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = { /* Aquí lógica para recuperar contraseña */ }) {
            Text("¿Olvidaste tu contraseña?")
        }
    }
}
