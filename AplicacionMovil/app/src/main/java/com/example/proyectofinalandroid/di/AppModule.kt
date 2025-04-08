package com.example.proyectofinalandroid.di

import com.example.proyectofinalandroid.Remote.EjerciciosApi
import com.example.proyectofinalandroid.Remote.EntrenamientosApi
import com.example.proyectofinalandroid.Remote.EntrenarApi
import com.example.proyectofinalandroid.Remote.RealizarEjerApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import com.example.proyectofinalandroid.Repository.EntrenamientosRepository
import com.example.proyectofinalandroid.Repository.EntrenarRepository
import com.example.proyectofinalandroid.Repository.RealizarEjerRepository
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
//    private const val BASE_URL = "http://192.168.1.142:3000/"
//    private const val BASE_URL = "http://10.0.2.2:3000/"
    private const val BASE_URL = "http://172.20.0.22:3000/" // La IP del trabajo

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUsuariosApi(retrofit: Retrofit): UsuariosApi {
        return retrofit.create(UsuariosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUsuariosRepository(api: UsuariosApi): UsuariosRepository {
        return UsuariosRepository(api)
    }

    @Provides
    @Singleton
    fun provideEntrenamientosApi(retrofit: Retrofit): EntrenamientosApi {
        return retrofit.create(EntrenamientosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEntrenamientosRepository(api: EntrenamientosApi): EntrenamientosRepository {
        return EntrenamientosRepository(api)
    }

    @Provides
    @Singleton
    fun provideEntrenarApi(retrofit: Retrofit): EntrenarApi {
        return retrofit.create(EntrenarApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEntrenarRepository(api: EntrenarApi): EntrenarRepository {
        return EntrenarRepository(api)
    }

    @Provides
    @Singleton
    fun provideEjerciciosApi(retrofit: Retrofit): EjerciciosApi {
        return retrofit.create(EjerciciosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEjerciciosRepository(api: EjerciciosApi): EjerciciosRepository {
        return EjerciciosRepository(api)
    }

    @Provides
    @Singleton
    fun provideRealizarEjerApi(retrofit: Retrofit): RealizarEjerApi {
        return retrofit.create(RealizarEjerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRealizarEjerRepository(api: RealizarEjerApi): RealizarEjerRepository {
        return RealizarEjerRepository(api)
    }
}