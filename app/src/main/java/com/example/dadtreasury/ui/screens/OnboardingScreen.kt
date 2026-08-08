package com.example.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dadtreasury.data.SettingsRepository
import com.example.dadtreasury.domain.model.Role
import com.example.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    settingsRepository: SettingsRepository,
    onDone: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    val scope = rememberCoroutineScope()
    var roleChoice by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var step by rememberSaveable { mutableStateOf(0) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🌿 RetroNest",
            style = MaterialTheme.typography.headlineLarge,
            color = tokens.accentPrimary,
        )
        Text(
            text = "A calm, private family hub",
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.textSecondary,
        )

        Spacer(Modifier.height(32.dp))

        when (step) {
            0 -> {
                Text("Who will use this device?", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { roleChoice = Role.PARENT.name; step = 1 },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("👨 Parent", color = tokens.textPrimary)
                    }
                    OutlinedButton(
                        onClick = { roleChoice = Role.CHILD.name; step = 1 },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("🧒 Child", color = tokens.textPrimary)
                    }
                }
            }
            1 -> {
                Text("What should we call you?", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = tokens.error, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Please enter your name"
                        } else {
                            scope.launch {
                                settingsRepository.setRole(Role.valueOf(roleChoice ?: Role.PARENT.name))
                                settingsRepository.setOnboardingDone()
                                onDone()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Start using RetroNest")
                }
            }
        }
    }
}