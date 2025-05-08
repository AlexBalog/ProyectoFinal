package com.example.proyectofinalandroid.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val TOKEN = stringPreferencesKey("auth_token")
    }

    suspend fun saveUser(id: String, token: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[TOKEN] = token
        }
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data.map { it[USER_ID] }.first()
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN] }.first()
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.clear() }
    }
}
