package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stokstylez.dadtreasury.data.SettingsRepository
import com.stokstylez.dadtreasury.domain.model.Language
import com.stokstylez.dadtreasury.security.PinLockManager
import com.stokstylez.dadtreasury.ui.theme.LocalAppSettings
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import com.stokstylez.dadtreasury.ui.theme.ThemeChoice
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    pinLockManager: PinLockManager? = null,
    onNavigatePinSetup: (() -> Unit)? = null,
) {
    val tokens = LocalSemanticTokens.current
    val appSettings = LocalAppSettings.current
    val scope = rememberCoroutineScope()

    val calmMode by settingsRepository.calmMode.collectAsState(initial = false)
    val reducedMotion by settingsRepository.reducedMotion.collectAsState(initial = false)
    val highContrast by settingsRepository.highContrast.collectAsState(initial = false)
    val textScale by settingsRepository.textScale.collectAsState(initial = 1.0f)
    val language by settingsRepository.language.collectAsState(initial = Language.SYSTEM_DEFAULT)

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Theme selection
            Text("Theme", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))
            ThemeChoice.entries.forEach { theme ->
                val selected = appSettings.theme == theme
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) tokens.accentPrimary.copy(alpha = 0.2f) else tokens.card,
                        contentColor = if (selected) tokens.accentPrimary else tokens.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = {
                        scope.launch { settingsRepository.setTheme(theme) }
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            when (theme) {
                                ThemeChoice.RETRO_FUTURIST -> Icons.Filled.RocketLaunch
                                ThemeChoice.HIGH_CONTRAST -> Icons.Filled.Highlight
                                ThemeChoice.CALM -> Icons.Filled.Spa
                                ThemeChoice.NATURE -> Icons.Filled.Park
                            },
                            contentDescription = null,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(theme.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Accessibility
            Text("Accessibility", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))

            // Calm mode
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Spa, contentDescription = null, tint = tokens.accentPrimary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Calm Mode", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                        Text("Reduce visual stimulation", style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                    }
                    Switch(
                        checked = calmMode,
                        onCheckedChange = {
                            scope.launch { settingsRepository.setCalmMode(it) }
                        },
                    )
                }
            }

            // Reduced motion
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, tint = tokens.accentPrimary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reduced Motion", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                        Text("Minimize animations", style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                    }
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = {
                            scope.launch { settingsRepository.setReducedMotion(it) }
                        },
                    )
                }
            }

            // High contrast
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Highlight, contentDescription = null, tint = tokens.accentPrimary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("High Contrast", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                        Text("Stronger color differences", style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                    }
                    Switch(
                        checked = highContrast,
                        onCheckedChange = {
                            scope.launch { settingsRepository.setHighContrast(it) }
                        },
                    )
                }
            }

            // Text scale
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Text Size", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                    Text(
                        "${(textScale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                    )
                    Slider(
                        value = textScale,
                        onValueChange = {
                            scope.launch { settingsRepository.setTextScale(it) }
                        },
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("80%", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                        Text("150%", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Language selection
            Text("Language", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("App Language", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                    Text("Choose app language", style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    Language.entries.forEach { lang ->
                        val selected = language == lang
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) tokens.accentPrimary.copy(alpha = 0.2f) else tokens.card,
                                contentColor = if (selected) tokens.accentPrimary else tokens.textPrimary,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            onClick = {
                                scope.launch { settingsRepository.setLanguage(lang) }
                            },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = if (selected) tokens.accentPrimary else tokens.textSecondary,
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    when (lang) {
                                        Language.SYSTEM_DEFAULT -> "🌐 System Default"
                                        Language.ENGLISH -> "English"
                                        Language.NEDERLANDS -> "Nederlands"
                                        Language.DEUTSCH -> "Deutsch"
                                        Language.ESPANOL -> "Español"
                                        Language.FRANCAIS -> "Français"
                                        Language.CHINESE -> "中文"
                                        Language.FRYSLAN -> "Frysk"
                                    },
                                    color = tokens.textPrimary,
                                )
                                Spacer(Modifier.weight(1f))
                                if (selected) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = tokens.success)
                                }
                            }
                        }
                    }
                }
            }

            // PIN Lock
            if (pinLockManager != null && onNavigatePinSetup != null) {
                Spacer(Modifier.height(24.dp))
                Text("Security", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                Spacer(Modifier.height(8.dp))

                val pinEnabled = remember { pinLockManager.isPinEnabled }

                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigatePinSetup() },
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (pinEnabled) tokens.success else tokens.textSecondary,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (pinEnabled) "PIN Lock Enabled" else "Set Up PIN Lock",
                                style = MaterialTheme.typography.bodyLarge,
                                color = tokens.textPrimary,
                            )
                            Text(
                                "Require PIN to access the app",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                            )
                        }
                        if (pinEnabled) {
                            Icon(Icons.Filled.Check, contentDescription = "Enabled", tint = tokens.success)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // About
            Text("About", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("🌿 Dad's Treasury", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                    Text(
                        "Offline-first family coordination app.\n" +
                            "Version 0.1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary,
                    )
                }
            }
        }
    }
}