package com.example.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.dadtreasury.security.PinLockManager
import com.example.dadtreasury.ui.theme.LocalSemanticTokens
import com.example.dadtreasury.ui.theme.SemanticTokens

@Composable
fun PinLockScreen(
    pinLockManager: PinLockManager,
    onUnlocked: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf(0) }
    var isLockedOut by remember { mutableStateOf(false) }

    fun handleUnlock() {
        if (pin.length < 4) {
            error = "Enter your PIN"
            return
        }
        if (pinLockManager.verifyPin(pin)) {
            pinLockManager.recordLockTimestamp()
            onUnlocked()
        } else {
            attempts++
            error = if (attempts >= 5) {
                isLockedOut = true
                "Too many attempts. App locked. Restart to retry."
            } else {
                "Incorrect PIN (${5 - attempts} attempts left)"
            }
            pin = ""
        }
    }

    if (isLockedOut) {
        LockedOutScreen(tokens = tokens)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🔒", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Dad Treasury",
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.textPrimary,
        )
        Text(
            "Enter your PIN to unlock",
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.textSecondary,
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { input ->
                // Allow only digits, max 8
                pin = input.filter { it.isDigit() }.take(8)
                error = null
            },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = tokens.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { handleUnlock() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Unlock", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LockedOutScreen(tokens: SemanticTokens) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🚫", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "Too many failed attempts",
            style = MaterialTheme.typography.headlineSmall,
            color = tokens.error,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Please restart the app to try again",
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.textSecondary,
        )
    }
}