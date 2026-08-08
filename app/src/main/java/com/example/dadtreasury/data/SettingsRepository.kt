package com.example.dadtreasury.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.dadtreasury.domain.model.Role
import com.example.dadtreasury.ui.theme.ThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dad_treasury_settings")

/**
 * Persists app settings (role, theme, accessibility options) via DataStore.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val ROLE = stringPreferencesKey("role")
        val THEME = stringPreferencesKey("theme")
        val CALM_MODE = booleanPreferencesKey("calm_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val role: Flow<Role?> = context.dataStore.data.map { prefs ->
        prefs[Keys.ROLE]?.let { Role.valueOf(it) }
    }

    val theme: Flow<ThemeChoice> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { ThemeChoice.valueOf(it) } ?: ThemeChoice.RETRO_FUTURIST
    }

    val calmMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.CALM_MODE] ?: false
    }

    val reducedMotion: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.REDUCED_MOTION] ?: false
    }

    val highContrast: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIGH_CONTRAST] ?: false
    }

    val textScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.TEXT_SCALE] ?: 1.0f
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_DONE] ?: false
    }

    suspend fun setRole(role: Role) {
        context.dataStore.edit { it[Keys.ROLE] = role.name }
    }

    suspend fun setTheme(theme: ThemeChoice) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setCalmMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CALM_MODE] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun setTextScale(scale: Float) {
        context.dataStore.edit { it[Keys.TEXT_SCALE] = scale }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }
}