package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dls_studio_prefs")

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    SPANISH("es", "Español"),
    PORTUGUESE("pt", "Português"),
    FRENCH("fr", "Français")
}

data class UserPreferences(
    val displayName: String = "Guest Designer",
    val clubTagline: String = "Dream League Creator",
    val themePreference: ThemePreference = ThemePreference.DARK,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val exportCount: Int = 0
)

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val CLUB_TAGLINE = stringPreferencesKey("club_tagline")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language_code")
        val EXPORT_COUNT = intPreferencesKey("export_count")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemePreference.DARK.name
        val theme = enumValues<ThemePreference>().firstOrNull { it.name == themeStr } ?: ThemePreference.DARK
        val langCode = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code
        val lang = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.ENGLISH
        UserPreferences(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Guest Designer",
            clubTagline = prefs[Keys.CLUB_TAGLINE] ?: "Dream League Creator",
            themePreference = theme,
            language = lang,
            exportCount = prefs[Keys.EXPORT_COUNT] ?: 0
        )
    }

    suspend fun updateProfile(name: String, tagline: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = name.ifBlank { "Guest Designer" }
            prefs[Keys.CLUB_TAGLINE] = tagline.ifBlank { "Dream League Creator" }
        }
    }

    suspend fun setThemePreference(themePreference: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themePreference.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun incrementExportCount() {
        context.dataStore.edit { prefs ->
            prefs[Keys.EXPORT_COUNT] = (prefs[Keys.EXPORT_COUNT] ?: 0) + 1
        }
    }
}
