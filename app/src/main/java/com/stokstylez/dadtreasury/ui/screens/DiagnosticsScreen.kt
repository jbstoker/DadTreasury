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
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.SyncStatus
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())
    val syncQueue by repository.observeSyncQueue().collectAsState(initial = emptyList())
    val devices by repository.observeDevices().collectAsState(initial = emptyList())

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics", color = tokens.textPrimary) },
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
            Text("System Health", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))

            // Storage status
            DiagnosticCard(
                label = "Offline Storage",
                value = "Room database active · local-only",
                icon = Icons.Filled.Storage,
                color = tokens.success,
            )
            // Sync status
            DiagnosticCard(
                label = "Sync Queue",
                value = "${syncQueue.size} pending events",
                icon = Icons.Filled.Sync,
                color = tokens.accentPrimary,
            )
            // Task count
            DiagnosticCard(
                label = "Tasks",
                value = "${tasks.size} total",
                icon = Icons.Filled.Checklist,
                color = tokens.warning,
            )
            // Devices
            DiagnosticCard(
                label = "Devices",
                value = "${devices.size} paired",
                icon = Icons.Filled.Link,
                color = tokens.accentSecondary,
            )
            // Meshtastic
            DiagnosticCard(
                label = "Meshtastic",
                value = "Not connected (requires hardware)",
                icon = Icons.Filled.Radar,
                color = tokens.textSecondary,
            )
            // Google Family
            DiagnosticCard(
                label = "Google Family Sync",
                value = "Not configured (requires internet)",
                icon = Icons.Filled.CloudOff,
                color = tokens.textSecondary,
            )
        }
    }
}

@Composable
fun DiagnosticCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
) {
    val tokens = LocalSemanticTokens.current
    Card(
        colors = CardDefaults.cardColors(containerColor = tokens.card),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                Text(value, style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
            }
        }
    }
}