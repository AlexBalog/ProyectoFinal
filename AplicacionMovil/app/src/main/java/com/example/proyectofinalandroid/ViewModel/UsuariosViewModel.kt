package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State
import com.example.proyectofinalandroid.utils.UserState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.proyectofinalandroid.utils.UserPreferences
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import androidx.work.WorkManager
import androidx.work.WorkInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Locale



@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val repository: UsuariosRepository,
    @ApplicationContext private val context: Context
    ) : ViewModel() {


    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState

    val _passwordResetSuccess = MutableStateFlow(false)
    val passwordResetSuccess: StateFlow<Boolean> get() = _passwordResetSuccess

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val loginResponse = repository.login(email, contrasena)
                if (loginResponse != null) {
                    val usuarioLogueado = repository.getOneByEmail(email, contrasena, loginResponse.token)
                    if (usuarioLogueado != null) {
                        usuarioLogueado.token = loginResponse.token // El token ya se guarda en el modelo
                        _usuario.value = usuarioLogueado
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Error al obtener datos del usuario"
                    }
                } else {
                    _errorMessage.value = "No existe un usuario con ese correo electrónico"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error en el login: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun update(updatedData: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            val currentUser = _usuario.value

            // ✅ AÑADIDO: Verificación detallada
            if (currentUser == null) {
                Log.e("UsuariosViewModel", "❌ ERROR: currentUser es null")
                _errorMessage.value = "Error: Usuario no disponible"
                return@withContext false
            }

            val _id = currentUser._id
            val token = currentUser.token

            // ✅ AÑADIDO: Log de debug para verificar valores
            Log.d("UsuariosViewModel", "🔍 DEBUG Update:")
            Log.d("UsuariosViewModel", "  - ID: $_id")
            Log.d("UsuariosViewModel", "  - Token: ${token?.take(10)}...")
            Log.d("UsuariosViewModel", "  - Datos a actualizar: $updatedData")

            if (token == null) {
                Log.e("UsuariosViewModel", "❌ ERROR: token es null")
                _errorMessage.value = "Error: Token no disponible"
                return@withContext false
            }

            if (_id.isEmpty()) {
                Log.e("UsuariosViewModel", "❌ ERROR: _id está vacío")
                _errorMessage.value = "Error: ID de usuario no válido"
                return@withContext false
            }

            val success = repository.update(_id, updatedData, token)

            Log.d("UsuariosViewModel", "📥 Resultado repository.update: $success")

            if (!success) {
                Log.e("UsuariosViewModel", "❌ ERROR: repository.update devolvió false")
                _errorMessage.value = "Error al actualizar el usuario en el servidor"
                return@withContext false
            }

            // Crear un formato de fecha para parsear el String a Date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Crear una copia con conversiones de tipo adecuadas
            val updatedUser = currentUser.copy(
                nombre = updatedData["nombre"] ?: currentUser.nombre,
                apellido = updatedData["apellido"] ?: currentUser.apellido,
                foto = updatedData["foto"] ?: currentUser.foto,
                sexo = updatedData["sexo"] ?: currentUser.sexo,
                // Convertir string a Date para fechaNacimiento
                fechaNacimiento = if (updatedData.containsKey("fechaNacimiento")) {
                    try {
                        val fechaString = updatedData["fechaNacimiento"]!!
                        val parsedDate = dateFormat.parse(fechaString)
                        parsedDate
                    } catch (e: Exception) {
                        Log.e("UsuariosViewModel", "Error parseando fecha: ${e.message}")
                        currentUser.fechaNacimiento // En caso de error, mantener el valor actual
                    }
                } else {
                    currentUser.fechaNacimiento
                },
                // Convertir strings a Float para los valores numéricos usando toFloatOrNull()
                altura = updatedData["altura"]?.toFloatOrNull() ?: currentUser.altura,
                peso = updatedData["peso"]?.toFloatOrNull() ?: currentUser.peso,
                objetivoPeso = updatedData["objetivoPeso"]?.toFloatOrNull() ?: currentUser.objetivoPeso,
                objetivoTiempo = updatedData["objetivoTiempo"]?.toFloatOrNull() ?: currentUser.objetivoTiempo,
                IMC = updatedData["IMC"]?.toFloatOrNull() ?: currentUser.IMC,
                nivelActividad = updatedData["nivelActividad"] ?: currentUser.nivelActividad,
                objetivoCalorias = updatedData["objetivoCalorias"]?.toFloatOrNull() ?: currentUser.objetivoCalorias,
                caloriasMantenimiento = updatedData["caloriasMantenimiento"]?.toFloatOrNull() ?: currentUser.caloriasMantenimiento,
            )

            // Actualizar el estado con el nuevo usuario
            _usuario.value = updatedUser
            Log.d("UsuariosViewModel", "✅ SUCCESS: Usuario actualizado localmente")

            return@withContext success
        }
    }


    suspend fun updateForm(updatedData: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            _usuario.value?.let { currentUser ->
                val _id = currentUser._id
                val token = currentUser.token ?: return@withContext false
                val success = repository.update(_id, updatedData, token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val updatedUser = currentUser.copy(
                    nombre = updatedData["nombre"] ?: currentUser.nombre,
                    apellido = updatedData["apellido"] ?: currentUser.apellido,
                    foto = updatedData["foto"] ?: currentUser.foto,
                    sexo = updatedData["sexo"] ?: currentUser.sexo,
                    // Convertir string a Date para fechaNacimiento
                    fechaNacimiento = if (updatedData.containsKey("fechaNacimiento")) {
                        try {
                            val fechaString = updatedData["fechaNacimiento"]!!
                            val parsedDate = dateFormat.parse(fechaString)
                            parsedDate
                        } catch (e: Exception) {
                            Log.e("UsuariosViewModel", "Error parseando fecha: ${e.message}")
                            currentUser.fechaNacimiento // En caso de error, mantener el valor actual
                        }
                    } else {
                        currentUser.fechaNacimiento
                    },
                    // Convertir strings a Float para los valores numéricos usando toFloatOrNull()
                    altura = updatedData["altura"]?.toFloatOrNull() ?: currentUser.altura,
                    peso = updatedData["peso"]?.toFloatOrNull() ?: currentUser.peso,
                    objetivoPeso = updatedData["objetivoPeso"]?.toFloatOrNull() ?: currentUser.objetivoPeso,
                    objetivoTiempo = updatedData["objetivoTiempo"]?.toFloatOrNull() ?: currentUser.objetivoTiempo,
                    IMC = updatedData["IMC"]?.toFloatOrNull() ?: currentUser.IMC,
                    nivelActividad = updatedData["nivelActividad"] ?: currentUser.nivelActividad,
                    objetivoCalorias = updatedData["objetivoCalorias"]?.toFloatOrNull() ?: currentUser.objetivoCalorias,
                    caloriasMantenimiento = updatedData["caloriasMantenimiento"]?.toFloatOrNull() ?: currentUser.caloriasMantenimiento,
                    formulario = true
                )

                _usuario.value = updatedUser
                return@withContext success
            } ?: false
        }
    }


    fun registrar(newUser: Usuarios) {
        viewModelScope.launch {
            try {
                val creado = repository.registerWithoutToken(newUser)
                if (creado != null) {
                    _usuario.value = creado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al crear el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }


    suspend fun getOne(id: String): Usuarios? {
        return withContext(Dispatchers.IO) {
            try {
                repository.getOne(id)
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar evento: ${e.message}"
                null
            }
        }
    }

    fun loadUser(userId: String, userToken: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val user = repository.getOne(userId)
                _userState.value = if (user != null) {
                    UserState.Success(user)
                } else {
                    UserState.Error("Usuario no encontrado")
                }
                user?.token = userToken
                _usuario.value = user
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val userPrefs = UserPreferences(context)
                userPrefs.clearUser()
                cancelScheduledJobs()
                _usuario.value = null
                _userState.value = UserState.Loading
            } catch (e: Exception) {
                Log.e("FalloUsuariosViewModel", "Error en logout: ${e.message}")
            }
        }
    }

    private fun cancelScheduledJobs() {
        try {
            val workManager = WorkManager.getInstance(context)
            val usuarioId = _usuario.value?._id ?: return
            workManager.cancelAllWorkByTag("entrenamiento_scheduler_worker")
            workManager.cancelAllWorkByTag("entrenamiento_reminder_worker")

            // 3. Verificar que los trabajos se hayan cancelado correctamente (opcional)
            viewModelScope.launch {
                try {
                    val schedulerInfos = getWorkInfosByTag(workManager, "entrenamiento_scheduler_worker")
                    val reminderInfos = getWorkInfosByTag(workManager, "entrenamiento_reminder_worker")
                    for (workInfo in schedulerInfos) {
                        if (workInfo.state != WorkInfo.State.CANCELLED) {
                            Log.w("UsuariosViewModel", "El trabajo ${workInfo.id} no se canceló correctamente")
                        }
                    }
                    for (workInfo in reminderInfos) {
                        if (workInfo.state != WorkInfo.State.CANCELLED) {
                            Log.w("UsuariosViewModel", "El trabajo ${workInfo.id} no se canceló correctamente")
                        }
                    }

                    Log.d("UsuariosViewModel", "Todos los trabajos cancelados correctamente")
                } catch (e: Exception) {
                    Log.e("UsuariosViewModel", "Error al verificar estados de trabajos: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("UsuariosViewModel", "Error al cancelar trabajos programados: ${e.message}")
        }
    }

    private suspend fun getWorkInfosByTag(workManager: WorkManager, tag: String): List<WorkInfo> {
        return suspendCancellableCoroutine { continuation ->
            val listenableFuture = workManager.getWorkInfosByTag(tag)

            listenableFuture.addListener(
                {
                    try {
                        val result = listenableFuture.get()
                        continuation.resume(result)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                },
                { command -> command.run() }  // Ejecutor
            )

            continuation.invokeOnCancellation {
                if (!listenableFuture.isDone) {
                    listenableFuture.cancel(true)
                }
            }
        }
    }


    suspend fun isTokenValid(token: String?): Boolean {
        if (token.isNullOrEmpty()) return false

        return try {
            repository.verifyToken(token)
        } catch (e: Exception) {
            Log.e("TokenValidation", "Error al verificar token: ${e.message}")
            false
        }
    }


    fun verifyTokenAndLoadUser(userId: String?, token: String?) {
        viewModelScope.launch {
            _userState.value = UserState.Loading

            if (userId.isNullOrEmpty() || token.isNullOrEmpty()) {
                _userState.value = UserState.Error("Credenciales no disponibles")
                return@launch
            }

            // Verificar token con el servidor
            val isValid = isTokenValid(token)

            if (!isValid) {
                _userState.value = UserState.Error("Token expirado o inválido")
                return@launch
            }

            // El token es válido, cargar el usuario
            try {
                val user = repository.getOne(userId)
                if (user != null) {
                    user.token = token
                    _usuario.value = user
                    _userState.value = UserState.Success(user)
                } else {
                    _userState.value = UserState.Error("Usuario no encontrado")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    // Función para enviar código de verificación
    fun sendVerificationCode(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.sendVerificationCode(email)
                if (success) {
                    _errorMessage.value = null
                    onResult(true)
                } else {
                    _errorMessage.value = "No se pudo enviar el código. Verifica que el email sea correcto."
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar código: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función para verificar código
    fun verifyCode(email: String, code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.verifyCode(email, code)
                if (success) {
                    _errorMessage.value = null
                    onResult(true)
                } else {
                    _errorMessage.value = "Código incorrecto. Inténtalo de nuevo."
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al verificar código: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función para cambiar contraseña
    fun changePassword(email: String, newPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val success = repository.changePassword(email, newPassword)
                if (success) {
                    _errorMessage.value = null
                    _passwordResetSuccess.value = true
                    onResult(true)
                } else {
                    _errorMessage.value = "No se pudo cambiar la contraseña. Inténtalo de nuevo."
                    onResult(false)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cambiar contraseña: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}