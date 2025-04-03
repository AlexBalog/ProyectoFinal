package com.example.proyectofinalandroid.Remote

import android.util.Log
import com.google.firebase.appdistribution.gradle.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // Endpoints para Usuario (ya existentes)
    val instance: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }

  /*  // Endpoints para Reserva y Habitaciones
    val reservaApiService: ReservaService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservaService::class.java)
    }

    val apiService: HabitacionesService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HabitacionesService::class.java)
    }*/
}
