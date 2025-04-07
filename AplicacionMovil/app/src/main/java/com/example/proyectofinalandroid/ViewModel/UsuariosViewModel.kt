package com.example.proyectofinalandroid.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UsuariosViewModel @Inject constructor(private val repository: UsuariosRepository) : ViewModel() {
    // Estados con StateFlow
    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> get() = _usuario

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

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

    suspend fun updateUsuario(updatedData: Map<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            _usuario.value?.let { currentUser ->
                val _id = currentUser._id
                val token = currentUser.token ?: return@withContext false
                val success = repository.updateUsuario(_id, updatedData, token)
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
                return@withContext success
            } ?: false
        }
    }

    fun registrarUsuario(newUser: Usuarios) {
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
}