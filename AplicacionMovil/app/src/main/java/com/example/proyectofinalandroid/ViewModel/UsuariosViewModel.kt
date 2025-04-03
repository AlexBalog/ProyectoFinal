package com.example.proyectofinalandroid.ViewModel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectofinalandroid.Model.Usuarios
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel : ViewModel() {
    private val repository = UsuariosRepository()

    private val _usuario = MutableStateFlow<Usuarios?>(null)
    val usuario: StateFlow<Usuarios?> = _usuario

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            val loginResponse = repository.login(email, contrasena)
            if (loginResponse != null) {
                Log.d("falloVM2", "$loginResponse")
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

    fun registerUsuario(newUser: Usuarios) {
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
