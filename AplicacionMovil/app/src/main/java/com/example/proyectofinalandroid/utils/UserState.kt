package com.example.proyectofinalandroid.utils

import com.example.proyectofinalandroid.Model.Usuarios

sealed class UserState {
    object Loading : UserState()
    data class Success(val data: Usuarios?) : UserState()
    data class Error(val message: String) : UserState()
}