package com.example.proyectofinalandroid.di

import android.util.Log
import com.example.proyectofinalandroid.Remote.EjerciciosApi
import com.example.proyectofinalandroid.Remote.EntrenamientosApi
import com.example.proyectofinalandroid.Remote.EntrenamientoRealizadoApi
import com.example.proyectofinalandroid.Remote.EventosApi
import com.example.proyectofinalandroid.Remote.EventosUsuarioApi
import com.example.proyectofinalandroid.Remote.EjercicioRealizadoApi
import com.example.proyectofinalandroid.Remote.GuardadosApi
import com.example.proyectofinalandroid.Repository.GuardadosRepository
import com.example.proyectofinalandroid.Remote.LikesApi
import com.example.proyectofinalandroid.Remote.SerieRealizadaApi
import com.example.proyectofinalandroid.Remote.UsuariosApi
import com.example.proyectofinalandroid.Repository.EjerciciosRepository
import com.example.proyectofinalandroid.Repository.EntrenamientosRepository
import com.example.proyectofinalandroid.Repository.EntrenamientoRealizadoRepository
import com.example.proyectofinalandroid.Repository.EventosRepository
import com.example.proyectofinalandroid.Repository.EventosUsuarioRepository
import com.example.proyectofinalandroid.Repository.EjercicioRealizadoRepository
import com.example.proyectofinalandroid.Repository.LikesRepository
import com.example.proyectofinalandroid.Repository.SerieRealizadaRepository
import com.example.proyectofinalandroid.Repository.UsuariosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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
            .client(provideOkHttpClient())
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
    fun provideEntrenarApi(retrofit: Retrofit): EntrenamientoRealizadoApi {
        return retrofit.create(EntrenamientoRealizadoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEntrenarRepository(api: EntrenamientoRealizadoApi): EntrenamientoRealizadoRepository {
        return EntrenamientoRealizadoRepository(api)
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
    fun provideRealizarEjerApi(retrofit: Retrofit): EjercicioRealizadoApi {
        return retrofit.create(EjercicioRealizadoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRealizarEjerRepository(api: EjercicioRealizadoApi): EjercicioRealizadoRepository {
        return EjercicioRealizadoRepository(api)
    }

    @Provides
    @Singleton
    fun provideEventosApi(retrofit: Retrofit): EventosApi {
        return retrofit.create(EventosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEventosRepository(api: EventosApi): EventosRepository {
        return EventosRepository(api)
    }

    @Provides
    @Singleton
    fun provideEventosUsuarioApi(retrofit: Retrofit): EventosUsuarioApi {
        return retrofit.create(EventosUsuarioApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEventosUsuarioRepository(api: EventosUsuarioApi): EventosUsuarioRepository {
        return EventosUsuarioRepository(api)
    }

    @Provides
    @Singleton
    fun provideSerieRealizadaApi(retrofit: Retrofit): SerieRealizadaApi {
        return retrofit.create(SerieRealizadaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSerieRealizadaRepository(api: SerieRealizadaApi): SerieRealizadaRepository {
        return SerieRealizadaRepository(api)
    }

    @Provides
    @Singleton
    fun LikesApi(retrofit: Retrofit): LikesApi {
        return retrofit.create(LikesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLikesRepository(api: LikesApi): LikesRepository {
        return LikesRepository(api)
    }

    @Provides
    @Singleton
    fun GuardadosApi(retrofit: Retrofit): GuardadosApi {
        return retrofit.create(GuardadosApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGuardadosRepository(api: GuardadosApi): GuardadosRepository {
        return GuardadosRepository(api)
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("Retrofit", "Request URL: ${request.url}")
                chain.proceed(request)
            }
            .build()
    }
}