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
import com.example.dadtreasury.domain.model.RewardType
import com.example.dadtreasury.domain.model.TaskStatus
import com.example.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    repository: DadTreasuryRepository,
    role: String?,
    onTaskClick: (String) -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val isParent = role == "PARENT"

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isParent) "All Tasks" else "My Tasks", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    if (isParent) {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Create task", tint = tokens.accentPrimary)
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
            if (tasks.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (isParent) "No tasks yet. Tap + to create one." else "No tasks. Check back later!",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                tasks.forEach { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        onClick = { onTaskClick(task.id) },
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.textPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                TaskStatusBadge(task.status, tokens)
                            }
                            task.description.let {
                                if (it.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(it, style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
                                }
                            }
                            task.dueTimestamp?.let { ts ->
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Due: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(ts))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.textSecondary,
                                )
                            }
                            if (task.rewardType != RewardType.FREE) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    when (task.rewardType) {
                                        RewardType.PAID -> "💰 +€${task.rewardAmount / 100.0}"
                                        RewardType.TIME -> "⏰ +${task.rewardAmount} min"
                                        RewardType.FREE -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.success,
                                )
                            }
                            if (task.completionPhotoUri != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "📸 Has completion photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.accentPrimary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, desc, minutes, rewardType, rewardAmount ->
                scope.launch {
                    repository.createTask(
                        title = title,
                        description = desc,
                        expectedDurationMinutes = minutes,
                        rewardType = rewardType,
                        rewardAmount = rewardAmount,
                    )
                }
                showCreateDialog = false
            },
        )
    }
}

@Composable
fun TaskStatusBadge(status: TaskStatus, tokens: com.example.dadtreasury.ui.theme.SemanticTokens) {
    val (label, color) = when (status) {
        TaskStatus.OPEN -> "Open" to tokens.accentPrimary
        TaskStatus.COMPLETED -> "Awaiting approval" to tokens.warning
        TaskStatus.APPROVED -> "Approved ✓" to tokens.success
        TaskStatus.REJECTED -> "Rejected" to tokens.error
        TaskStatus.CANCELLED -> "Cancelled" to tokens.textSecondary
    }
    Surface(color = color.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, RewardType, Long) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("") }
    var rewardType by remember { mutableStateOf(RewardType.FREE) }
    var rewardAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Expected duration (min)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Reward type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RewardType.entries.forEach { type ->
                        FilterChip(
                            selected = rewardType == type,
                            onClick = { rewardType = type },
                            label = { Text(type.name) },
                        )
                    }
                }
                if (rewardType != RewardType.FREE) {
                    OutlinedTextField(
                        value = rewardAmount,
                        onValueChange = { rewardAmount = it },
                        label = { Text(if (rewardType == RewardType.PAID) "Amount (cents)" else "Minutes") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            description.trim(),
                            durationMinutes.toIntOrNull() ?: 0,
                            rewardType,
                            rewardAmount.toLongOrNull() ?: 0,
                        )
                    }
                },
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}