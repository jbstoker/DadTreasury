package com.example.dadtreasury.ui.screens

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
import com.example.dadtreasury.data.DadTreasuryRepository
import com.example.dadtreasury.domain.model.DeviceIdentity
import com.example.dadtreasury.domain.model.Role
import com.example.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    repository: DadTreasuryRepository,
    role: String?,
) {
    val tokens = LocalSemanticTokens.current
    val devices by repository.observeDevices().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val isParent = role == "PARENT"

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Device Pairing", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    if (isParent) {
                        IconButton(onClick = {
                            scope.launch {
                                repository.addDevice(
                                    DeviceIdentity(
                                        deviceId = UUID.randomUUID().toString(),
                                        displayName = "New child device",
                                        publicKey = "generated-key",
                                        role = Role.CHILD,
                                        isTrusted = false,
                                    )
                                )
                            }
                        }) {
                            Icon(Icons.Filled.Link, contentDescription = "Add device", tint = tokens.accentPrimary)
                        }
                    }
                },
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
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (isParent)
                        "Pair child devices via QR code or secure link. You can revoke access anytime. 🔒"
                    else
                        "This device is paired with your parent. Access can be revoked by your parent.",
                    modifier = Modifier.padding(16.dp),
                    color = tokens.textSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Connected Devices", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))

            if (devices.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No devices paired yet.",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                devices.forEach { device ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (device.role == Role.PARENT) Icons.Filled.Person else Icons.Filled.ChildCare,
                                contentDescription = null,
                                tint = if (device.isRevoked) tokens.error else tokens.accentPrimary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(device.displayName, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                Text(
                                    "${device.role.name} · ${if (device.isTrusted) "Trusted" else "Pending"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.textSecondary,
                                )
                            }
                            if (isParent && !device.isRevoked) {
                                IconButton(onClick = {
                                    scope.launch { repository.revokeDevice(device.deviceId) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Revoke", tint = tokens.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}