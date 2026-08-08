package com.stokstylez.dadtreasury.ui.screens

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
import com.stokstylez.dadtreasury.security.PinLockManager
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

/**
 * Setup screen for PIN lock - either set a new PIN or change existing.
 */
@Composable
fun PinSetupScreen(
    pinLockManager: PinLockManager,
    mode: PinSetupMode,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showCurrent by remember {
        mutableStateOf(pinLockManager.isPinEnabled)
    }

    fun handleSave() {
        // Validate
        if (showCurrent && currentPin.isBlank()) {
            error = "Enter your current PIN"
            return
        }
        if (newPin.length < 4) {
            error = "PIN must be at least 4 digits"
            return
        }
        if (!newPin.all { it.isDigit() }) {
            error = "PIN must contain only digits"
            return
        }
        if (newPin != confirmPin) {
            error = "PINs do not match"
            return
        }

        val current = if (showCurrent) currentPin else null
        val success = pinLockManager.setPin(currentPin = current, newPin = newPin)
        if (!success) {
            error = "Current PIN is incorrect"
            return
        }
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            mode.title,
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.textPrimary,
        )
        Text(
            "Protect your family data with a PIN",
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.textSecondary,
        )

        Spacer(Modifier.height(32.dp))

        // Current PIN field - only if PIN already enabled
        if (showCurrent) {
            OutlinedTextField(
                value = currentPin,
                onValueChange = { currentPin = it.filter { c -> c.isDigit() }.take(8) },
                label = { Text("Current PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = error != null && currentPin.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = newPin,
            onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(8) },
            label = { Text("New PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPin,
            onValueChange = { confirmPin = it.filter { c -> c.isDigit() }.take(8) },
            label = { Text("Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = tokens.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { handleSave() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(mode.buttonText, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancel", color = tokens.textSecondary)
        }
    }
}

enum class PinSetupMode {
    SETUP,
    CHANGE;

    val title: String
        get() = when (this) {
            SETUP -> "Set Up PIN"
            CHANGE -> "Change PIN"
        }

    val buttonText: String
        get() = when (this) {
            SETUP -> "Enable PIN 🔒"
            CHANGE -> "Change PIN"
        }
}