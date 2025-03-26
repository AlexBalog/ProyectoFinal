package com.example.proyectofinalandroid.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel : ViewModel() {
    private val repository = UsuarioRepository()

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            val loginResponse = repository.login(email, contrasena)
            if (loginResponse != null) {
                val usuarioLogueado = repository.getOneByEmail(email, contrasena, loginResponse.token)
                if (usuarioLogueado != null) {
                    usuarioLogueado.token = loginResponse.token
                    _usuario.value = usuarioLogueado
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al obtener datos del usuario"
                }
            } else {
                _errorMessage.value = "No existe un usuario con ese correo electronico"
            }
        }
    }

    fun updateUsuario(dni: String, updatedData: Map<String, String>) {
        viewModelScope.launch {
            _usuario.value?.let { currentUser ->
                val success = repository.updateUsuario(dni, updatedData, currentUser.token ?: "")
                if (!success) {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
            }
        }
    }

    fun registerUsuario(newUser: Usuario) {
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
