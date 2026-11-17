package com.example.memodeck.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")
val THEME_KEY = booleanPreferencesKey("is_dark_theme")

class ThemeManager(private val context: Context) {
    val isDarkTheme = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: false // По умолчанию светлая
    }

    suspend fun toggleTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }
}