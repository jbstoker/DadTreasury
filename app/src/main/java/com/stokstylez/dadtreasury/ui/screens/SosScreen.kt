package com.stokstylez.dadtreasury.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SOS / quick-alert options the child can send to the parent.
 */
data class SosOption(
    val id: String,
    val label: String,
    val emoji: String,
    val message: String,
)

private val sosOptions = listOf(
    SosOption("help", "Help", "🆘", "🆘 Help! I need assistance."),
    SosOption("call", "Call Me", "📞", "📞 Please call me!"),
    SosOption("tired", "I'm tired", "😴", "😴 I'm tired."),
    SosOption("hungry", "I'm hungry", "🍽️", "🍽️ I'm hungry."),
    SosOption("late", "Late for dinner", "⏰", "⏰ I'm late for dinner."),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf(sosOptions.first()) }
    var sending by remember { mutableStateOf(false) }
    var sentMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("SOS Button", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Need something? Send a quick alert to your parent, along with your location.",
                style = MaterialTheme.typography.bodyLarge,
                color = tokens.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "What's going on?",
                style = MaterialTheme.typography.titleMedium,
                color = tokens.textPrimary,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            // Option list (radio-style select)
            sosOptions.forEach { option ->
                val selected = selectedOption.id == option.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) tokens.accentPrimary.copy(alpha = 0.2f) else tokens.card,
                        contentColor = tokens.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { selectedOption = option },
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(option.emoji, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = tokens.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selected) tokens.success else tokens.textSecondary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Big red panic button
            Button(
                onClick = {
                    scope.launch {
                        sending = true
                        sentMessage = null
                        val location = withContext(Dispatchers.IO) {
                            getLastKnownLocation(context)
                        }
                        val locationText = if (location != null) {
                            "📍 ${"%.5f".format(location.first)}, ${"%.5f".format(location.second)}"
                        } else {
                            "📍 Location unavailable"
                        }
                        val fullMessage = "${selectedOption.message}\n$locationText"
                        repository.sendMessage("parent-child", Role.CHILD, fullMessage)
                        sentMessage = fullMessage
                        sending = false
                    }
                },
                enabled = !sending,
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "SEND",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (sending) {
                CircularProgressIndicator(color = tokens.accentPrimary)
            }

            sentMessage?.let { _ ->
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Sent ✓ Your parent has been notified.",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.success,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Your current location is included with the message so your parent can find you.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Get the last known device location (no Google Play Services required).
 * Returns (lat, lng) or null if unavailable / permission not granted.
 */
private fun getLastKnownLocation(context: Context): Pair<Double, Double>? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val hasPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasPermission) return null
    listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
        val loc = try {
            lm.getLastKnownLocation(provider)
        } catch (_: Exception) {
            null
        }
        if (loc != null) return loc.latitude to loc.longitude
    }
    return null
}