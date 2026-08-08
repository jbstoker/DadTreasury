package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.Role
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    repository: DadTreasuryRepository,
    role: String?,
) {
    val tokens = LocalSemanticTokens.current
    val threadId = "parent-child"
    val messages by repository.observeMessages(threadId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    val currentRole = if (role == "PARENT") Role.PARENT else Role.CHILD

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Chat", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
            )
        },
        bottomBar = {
            Surface(color = tokens.surface) {
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
                                    repository.sendMessage(threadId, currentRole, input.trim())
                                    input = ""
                                }
                            }
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = tokens.accentPrimary)
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
                            "Messages with you and your parent/child appear here. 👋",
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
                            Text(message.text, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
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