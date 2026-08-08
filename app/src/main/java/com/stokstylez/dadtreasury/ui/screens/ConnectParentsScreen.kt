package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.AppConnection
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

/**
 * Screen for connecting two parent apps so they can:
 *  - share the wiki/library with each other
 *  - message each other
 *
 * Uses a 6-character pairing code for secure connection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectParentsScreen(repository: DadTreasuryRepository, role: String? = null) {
    val tokens = LocalSemanticTokens.current
    val scope = rememberCoroutineScope()
    val connections by repository.observeAppConnections().collectAsState(initial = emptyList())
    val pages by repository.observePages().collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var selectedConnection by remember { mutableStateOf<AppConnection?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Connect Parents", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Link, contentDescription = "Create pairing code", tint = tokens.accentPrimary)
                    }
                    IconButton(onClick = { showAcceptDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Input, contentDescription = "Enter pairing code", tint = tokens.accentPrimary)
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
                    "Connect two parent apps to share the family wiki and message each other securely. 🔗",
                    modifier = Modifier.padding(16.dp),
                    color = tokens.textSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Connected Parent Apps", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))

            if (connections.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No parent connections yet. Create a pairing code or enter one from another parent.",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                connections.filter { !it.isRevoked }.forEach { conn ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { selectedConnection = conn },
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (conn.isTrusted) Icons.Filled.Person else Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = if (conn.isTrusted) tokens.success else tokens.warning,
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(conn.displayName, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                    Text(
                                        if (conn.isTrusted) "Trusted · Code ${conn.pairingCode}" else "Pending · Code ${conn.pairingCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Connection detail dialog (share page / send message / disconnect)
    selectedConnection?.let { conn ->
        AlertDialog(
            onDismissRequest = { selectedConnection = null },
            title = { Text("${conn.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Pairing code: ${conn.pairingCode}\n" +
                            if (conn.isTrusted) "Status: Trusted ✓" else "Status: Pending",
                        color = tokens.textSecondary,
                    )

                    if (conn.isTrusted) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            label = { Text("Send message") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                if (conn.isTrusted) {
                    TextButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                scope.launch {
                                    repository.sendParentMessage(conn.id, messageText.trim())
                                    messageText = ""
                                }
                            }
                        },
                    ) { Text("Send") }
                }
                TextButton(onClick = { showShareDialog = true }) { Text("Share Wiki") }
                TextButton(
                    onClick = {
                        scope.launch { repository.disconnectAppConnection(conn.id) }
                        selectedConnection = null
                    },
                ) { Text("Disconnect") }
            },
            dismissButton = { TextButton(onClick = { selectedConnection = null }) { Text("Close") } },
        )
    }

    // Share page dialog
    if (showShareDialog && selectedConnection != null) {
        val conn = selectedConnection!!
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Wiki Page") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (pages.isEmpty()) {
                        Text("No wiki pages to share yet.", color = tokens.textSecondary)
                    } else {
                        pages.forEach { page ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = tokens.card),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        repository.shareLibraryPage(conn.id, page.id)
                                        showShareDialog = false
                                        selectedConnection = null
                                    }
                                },
                            ) {
                                Text(
                                    page.title,
                                    modifier = Modifier.padding(12.dp),
                                    color = tokens.textPrimary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showShareDialog = false }) { Text("Cancel") } },
        )
    }

    // Create pairing code dialog
    var createName by remember { mutableStateOf("") }
    var createdCode by remember { mutableStateOf<String?>(null) }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; createdCode = null },
            title = { Text(if (createdCode == null) "Create Parent Connection" else "Your Pairing Code") },
            text = {
                if (createdCode == null) {
                    OutlinedTextField(
                        value = createName,
                        onValueChange = { createName = it },
                        label = { Text("Your display name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            createdCode!!,
                            style = MaterialTheme.typography.headlineMedium,
                            color = tokens.accentPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Share this 6-character code with the other parent app.\nThey can enter it to connect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary,
                        )
                    }
                }
            },
            confirmButton = {
                if (createdCode == null) {
                    TextButton(
                        onClick = {
                            if (createName.isNotBlank()) {
                                scope.launch {
                                    createdCode = repository.createAppConnection(createName.trim())
                                }
                            }
                        },
                    ) { Text("Create Code") }
                } else {
                    TextButton(onClick = { showCreateDialog = false; createdCode = null }) { Text("Done") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; createdCode = null }) { Text("Cancel") }
            },
        )
    }

    // Accept pairing code dialog
    var acceptCode by remember { mutableStateOf("") }

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Connect to Parent App") },
            text = {
                OutlinedTextField(
                    value = acceptCode,
                    onValueChange = { acceptCode = it.uppercase() },
                    label = { Text("Enter 6-character code") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (acceptCode.length == 6) {
                            scope.launch {
                                repository.acceptAppConnection(acceptCode.trim().uppercase(), "Parent")
                                acceptCode = ""
                                showAcceptDialog = false
                            }
                        }
                    },
                ) { Text("Connect") }
            },
            dismissButton = { TextButton(onClick = { showAcceptDialog = false }) { Text("Cancel") } },
        )
    }
}