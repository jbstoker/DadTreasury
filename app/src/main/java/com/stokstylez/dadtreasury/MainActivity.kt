package com.stokstylez.dadtreasury

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.data.SettingsRepository
import com.stokstylez.dadtreasury.data.db.AppDatabase
import com.stokstylez.dadtreasury.security.PinLockManager
import com.stokstylez.dadtreasury.domain.model.Language
import com.stokstylez.dadtreasury.ui.DadTreasuryApp
import com.stokstylez.dadtreasury.ui.screens.PinLockScreen
import com.stokstylez.dadtreasury.ui.theme.AppSettingsState
import com.stokstylez.dadtreasury.ui.theme.DadTreasuryTheme
import com.stokstylez.dadtreasury.ui.theme.LocalAppSettings
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Block screenshots and screen recording when PIN lock is enabled
        // FLAG_SECURE prevents content from appearing in screenshots, screen capture,
        // recent apps preview, and other capture mechanisms
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            val context = LocalContext.current
            val settingsRepository = remember { SettingsRepository(context.applicationContext) }
            val db = remember { AppDatabase.getInstance(context.applicationContext) }
            val repository = remember { DadTreasuryRepository(db, context.applicationContext) }
            val appSettings = remember { AppSettingsState() }
            val pinLockManager = remember { PinLockManager(context.applicationContext) }
            var pinUnlocked by remember {
                mutableStateOf(!pinLockManager.shouldLockOnOpen())
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { /* handled elsewhere */ }
            )

            // Request location + notification permissions once when PIN is unlocked
            LaunchedEffect(Unit) {
                val perms = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                }
                val missing = perms.filter {
                    checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
                }
                if (missing.isNotEmpty()) {
                    permissionLauncher.launch(missing.toTypedArray())
                }
            }

            val role by settingsRepository.role.collectAsState(initial = null)
            LaunchedEffect(role) {
                repository.currentRole = role?.name
            }
            val theme by settingsRepository.theme.collectAsState(initial = com.stokstylez.dadtreasury.ui.theme.ThemeChoice.RETRO_FUTURIST)
            val calmMode by settingsRepository.calmMode.collectAsState(initial = false)
            val reducedMotion by settingsRepository.reducedMotion.collectAsState(initial = false)
            val highContrast by settingsRepository.highContrast.collectAsState(initial = false)
            val textScale by settingsRepository.textScale.collectAsState(initial = 1.0f)
            val language by settingsRepository.language.collectAsState(initial = Language.SYSTEM_DEFAULT)
            val onboardingDone by settingsRepository.onboardingDone.collectAsState(initial = false)

            LaunchedEffect(role, theme, calmMode, reducedMotion, highContrast, textScale, language) {
                appSettings.role = role?.name
                appSettings.theme = theme
                appSettings.calmMode = calmMode
                appSettings.reducedMotion = reducedMotion
                appSettings.highContrast = highContrast
                appSettings.textScale = textScale
                appSettings.language = language
            }

            // Apply locale to the activity context when the user changes language.
            // We call recreate() so Compose fully rebuilds with the new locale resources.
            var lastAppliedLocale by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(language, lastAppliedLocale) {
                if (lastAppliedLocale == language.name) return@LaunchedEffect
                lastAppliedLocale = language.name
                val locale = language.localeTag?.let { Locale(it) } ?: Locale.getDefault()
                Locale.setDefault(locale)
                val config = Configuration(resources.configuration)
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
                // Also update application context so future inflation uses the locale
                val appContext = context.applicationContext
                val appConfig = Configuration(appContext.resources.configuration)
                appConfig.setLocale(locale)
                @Suppress("DEPRECATION")
                appContext.resources.updateConfiguration(appConfig, appContext.resources.displayMetrics)
                // Force a full recreation so ALL views (nav bar, screens) rebuild with
                // the selected language's resource strings.
                kotlinx.coroutines.delay(200)
                recreate()
            }

            CompositionLocalProvider(LocalAppSettings provides appSettings) {
                DadTreasuryTheme {
                    if (pinUnlocked) {
                        DadTreasuryApp(
                            repository = repository,
                            settingsRepository = settingsRepository,
                            role = role?.name,
                            onboardingDone = onboardingDone,
                            pinLockManager = pinLockManager,
                        )
                    } else {
                        PinLockScreen(
                            pinLockManager = pinLockManager,
                            onUnlocked = { pinUnlocked = true },
                        )
                    }
                }
            }
        }
    }
}