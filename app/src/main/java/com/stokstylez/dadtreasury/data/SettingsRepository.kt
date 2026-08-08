package com.stokstylez.dadtreasury.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stokstylez.dadtreasury.domain.model.Language
import com.stokstylez.dadtreasury.domain.model.PersonalInfo
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.ui.theme.ThemeChoice
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
        val LANGUAGE = stringPreferencesKey("language")
        val PERSONAL_DAD_NAME = stringPreferencesKey("personal_dad_name")
        val PERSONAL_DAD_PHONE = stringPreferencesKey("personal_dad_phone")
        val PERSONAL_DAD_EMAIL = stringPreferencesKey("personal_dad_email")
        val PERSONAL_DAD_ADDRESS = stringPreferencesKey("personal_dad_address")
        val PERSONAL_MOM_NAME = stringPreferencesKey("personal_mom_name")
        val PERSONAL_MOM_PHONE = stringPreferencesKey("personal_mom_phone")
        val PERSONAL_MOM_EMAIL = stringPreferencesKey("personal_mom_email")
        val PERSONAL_MOM_ADDRESS = stringPreferencesKey("personal_mom_address")
        val PERSONAL_CHILD_NAME = stringPreferencesKey("personal_child_name")
        val PERSONAL_CHILD_EMAIL = stringPreferencesKey("personal_child_email")
        val PERSONAL_CHILD_MOBILE = stringPreferencesKey("personal_child_mobile")
        val PERSONAL_CHILD_BIRTH = stringPreferencesKey("personal_child_birth")
        val PERSONAL_SHOE = stringPreferencesKey("personal_shoe")
        val PERSONAL_JEANS = stringPreferencesKey("personal_jeans")
        val PERSONAL_SHIRT = stringPreferencesKey("personal_shirt")
        val PERSONAL_JACKET = stringPreferencesKey("personal_jacket")
        val PERSONAL_HAT = stringPreferencesKey("personal_hat")
        val PERSONAL_DRESS = stringPreferencesKey("personal_dress")
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

    val language: Flow<Language> = context.dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE]?.let { runCatching { Language.valueOf(it) }.getOrNull() }
            ?: Language.SYSTEM_DEFAULT
    }

    val personalInfo: Flow<PersonalInfo> = context.dataStore.data.map { prefs ->
        PersonalInfo(
            dadName = prefs[Keys.PERSONAL_DAD_NAME] ?: "",
            dadPhone = prefs[Keys.PERSONAL_DAD_PHONE] ?: "",
            dadEmail = prefs[Keys.PERSONAL_DAD_EMAIL] ?: "",
            dadAddress = prefs[Keys.PERSONAL_DAD_ADDRESS] ?: "",
            momName = prefs[Keys.PERSONAL_MOM_NAME] ?: "",
            momPhone = prefs[Keys.PERSONAL_MOM_PHONE] ?: "",
            momEmail = prefs[Keys.PERSONAL_MOM_EMAIL] ?: "",
            momAddress = prefs[Keys.PERSONAL_MOM_ADDRESS] ?: "",
            childName = prefs[Keys.PERSONAL_CHILD_NAME] ?: "",
            childEmail = prefs[Keys.PERSONAL_CHILD_EMAIL] ?: "",
            childMobile = prefs[Keys.PERSONAL_CHILD_MOBILE] ?: "",
            childBirthdate = prefs[Keys.PERSONAL_CHILD_BIRTH] ?: "",
            shoeSize = prefs[Keys.PERSONAL_SHOE] ?: "",
            jeansSize = prefs[Keys.PERSONAL_JEANS] ?: "",
            shirtSize = prefs[Keys.PERSONAL_SHIRT] ?: "",
            jacketSize = prefs[Keys.PERSONAL_JACKET] ?: "",
            hatSize = prefs[Keys.PERSONAL_HAT] ?: "",
            dressSize = prefs[Keys.PERSONAL_DRESS] ?: "",
        )
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

    suspend fun setLanguage(language: Language) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setPersonalInfo(info: PersonalInfo) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PERSONAL_DAD_NAME] = info.dadName
            prefs[Keys.PERSONAL_DAD_PHONE] = info.dadPhone
            prefs[Keys.PERSONAL_DAD_EMAIL] = info.dadEmail
            prefs[Keys.PERSONAL_DAD_ADDRESS] = info.dadAddress
            prefs[Keys.PERSONAL_MOM_NAME] = info.momName
            prefs[Keys.PERSONAL_MOM_PHONE] = info.momPhone
            prefs[Keys.PERSONAL_MOM_EMAIL] = info.momEmail
            prefs[Keys.PERSONAL_MOM_ADDRESS] = info.momAddress
            prefs[Keys.PERSONAL_CHILD_NAME] = info.childName
            prefs[Keys.PERSONAL_CHILD_EMAIL] = info.childEmail
            prefs[Keys.PERSONAL_CHILD_MOBILE] = info.childMobile
            prefs[Keys.PERSONAL_CHILD_BIRTH] = info.childBirthdate
            prefs[Keys.PERSONAL_SHOE] = info.shoeSize
            prefs[Keys.PERSONAL_JEANS] = info.jeansSize
            prefs[Keys.PERSONAL_SHIRT] = info.shirtSize
            prefs[Keys.PERSONAL_JACKET] = info.jacketSize
            prefs[Keys.PERSONAL_HAT] = info.hatSize
            prefs[Keys.PERSONAL_DRESS] = info.dressSize
        }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }
}