package com.gokcank.optidoc.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage {
    SYSTEM, TR, EN
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val THEME_KEY = stringPreferencesKey("theme_mode")
    private val LANGUAGE_KEY = stringPreferencesKey("language_mode")

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeStr = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        val langStr = preferences[LANGUAGE_KEY] ?: AppLanguage.SYSTEM.name
        try {
            AppLanguage.valueOf(langStr)
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }
}
