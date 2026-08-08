package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

/**
 * A chat target: either the child (default parent-child thread) or a connected parent app.
 */
data class ChatTarget(
    val id: String,
    val threadId: String,
    val displayName: String,
    val type: ChatTargetType,
)

enum class ChatTargetType {
    CHILD,
    PARENT_APP,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    repository: DadTreasuryRepository,
    role: String?,
) {
    val tokens = LocalSemanticTokens.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    val isParent = role == "PARENT"
    val currentRole = if (isParent) Role.PARENT else Role.CHILD

    // Connected parent apps (for parent-to-parent chat)
    val connections by repository.observeAppConnections().collectAsState(initial = emptyList())

    // Build list of available chat targets
    val targets = remember(connections) {
        buildList {
            // Always the child
            add(
                ChatTarget(
                    id = "child",
                    threadId = "parent-child",
                    displayName = "👶 Child",
                    type = ChatTargetType.CHILD,
                )
            )
            // Connected parent apps
            connections.filter { it.isTrusted && !it.isRevoked }.forEach { conn ->
                add(
                    ChatTarget(
                        id = conn.id,
                        threadId = "parent-${conn.id}",
                        displayName = "👨 ${conn.displayName}",
                        type = ChatTargetType.PARENT_APP,
                    )
                )
            }
        }
    }

    var selectedTarget by remember { mutableStateOf(targets.first()) }

    // If a parent connects a new app, update selection if current is invalid
    LaunchedEffect(targets) {
        if (targets.none { it.id == selectedTarget.id }) {
            selectedTarget = targets.first()
        }
    }

    val messages by repository.observeMessages(selectedTarget.threadId).collectAsState(initial = emptyList())

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat", color = tokens.textPrimary)
                        if (isParent && targets.size > 1) {
                            Text(
                                "→ ${selectedTarget.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.accentPrimary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
            )
        },
        bottomBar = {
            Surface(color = tokens.surface) {
                Column {
                    // Thread selector for parents: switch between child and connected parent apps
                    if (isParent && targets.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            targets.forEach { target ->
                                FilterChip(
                                    selected = selectedTarget.id == target.id,
                                    onClick = { selectedTarget = target },
                                    label = { Text(target.displayName) },
                                )
                            }
                        }
                        HorizontalDivider(color = tokens.border)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Type a message...", color = tokens.textSecondary) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (input.isNotBlank()) {
                                    scope.launch {
                                        if (selectedTarget.type == ChatTargetType.PARENT_APP) {
                                            // Send as parent-to-parent via the connection
                                            repository.sendParentMessage(
                                                connectionId = selectedTarget.id,
                                                text = input.trim(),
                                            )
                                        } else {
                                            repository.sendMessage(selectedTarget.threadId, currentRole, input.trim())
                                        }
                                        input = ""
                                    }
                                }
                            },
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = tokens.accentPrimary)
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Messages with you and ${selectedTarget.displayName} appear here. 👋",
                            modifier = Modifier.padding(16.dp),
                            color = tokens.textSecondary,
                        )
                    }
                }
            }
            items(messages, key = { it.id }) { message ->
                val isMine = message.senderRole == currentRole
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                ) {
                    Surface(
                        color = if (isMine) tokens.accentPrimary.copy(alpha = 0.2f) else tokens.card,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            // Render location pin if the message contains a 📍 with coordinates
                            val locationMatch = Regex("""📍 (-?\d+\.\d+), (-?\d+\.\d+)""").find(message.text)
                            if (locationMatch != null) {
                                val lat = locationMatch.groupValues[1].toDoubleOrNull()
                                val lng = locationMatch.groupValues[2].toDoubleOrNull()
                                val textWithoutLocation = message.text
                                    .replace(locationMatch.value, "")
                                    .trim('\n', ' ')
                                Text(textWithoutLocation, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                Spacer(Modifier.height(4.dp))
                                // Location pin card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = tokens.error.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = "Location pin",
                                            tint = tokens.error,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "📍 Location",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = tokens.error,
                                            )
                                            if (lat != null && lng != null) {
                                                Text(
                                                    "${"%.5f".format(lat)}, ${"%.5f".format(lng)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tokens.textSecondary,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(message.text, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                            }
                            Text(
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
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