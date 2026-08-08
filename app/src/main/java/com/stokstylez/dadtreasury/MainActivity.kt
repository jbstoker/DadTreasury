package com.stokstylez.dadtreasury

import android.Manifest
import android.content.pm.PackageManager
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
import com.stokstylez.dadtreasury.ui.DadTreasuryApp
import com.stokstylez.dadtreasury.ui.screens.PinLockScreen
import com.stokstylez.dadtreasury.ui.theme.AppSettingsState
import com.stokstylez.dadtreasury.ui.theme.DadTreasuryTheme
import com.stokstylez.dadtreasury.ui.theme.LocalAppSettings

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
            val onboardingDone by settingsRepository.onboardingDone.collectAsState(initial = false)

            LaunchedEffect(role, theme, calmMode, reducedMotion, highContrast, textScale) {
                appSettings.role = role?.name
                appSettings.theme = theme
                appSettings.calmMode = calmMode
                appSettings.reducedMotion = reducedMotion
                appSettings.highContrast = highContrast
                appSettings.textScale = textScale
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