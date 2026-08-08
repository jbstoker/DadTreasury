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
import com.stokstylez.dadtreasury.domain.naturecalendar.NatureCalendar
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val events by repository.observeAllEvents().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val now = System.currentTimeMillis()
    val todayNatural = remember(now) { NatureCalendar.fromTimestamp(now) }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Calendar", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add event", tint = tokens.accentPrimary)
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
            // Dual date display - per spec §12: Gregorian in default, natural in green
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        SimpleDateFormat("EEEE, d MMM yyyy  HH:mm", Locale.getDefault()).format(Date(now)),
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.textPrimary,
                    )
                    Text(
                        todayNatural.fullLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.naturalCalendar,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Upcoming Events", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)

            Spacer(Modifier.height(8.dp))

            val upcoming = events.filter { it.endTimestamp >= now }.sortedBy { it.startTimestamp }
            if (upcoming.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No upcoming events. Add one with + !",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                upcoming.forEach { event ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(event.title, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                Text(
                                    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(event.startTimestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.textSecondary,
                                )
                                val naturalStart = NatureCalendar.fromTimestamp(event.startTimestamp)
                                Text(
                                    naturalStart.fullLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.naturalCalendar,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, description, startTs, endTs, reminderMinutes ->
                scope.launch {
                    repository.addCalendarEvent(
                        title = title,
                        description = description,
                        startTimestamp = startTs,
                        endTimestamp = endTs,
                        reminderMinutes = reminderMinutes,
                    )
                }
                showAddDialog = false
            },
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Long, Int?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("1") }
    var reminderValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Event") },
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
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Date (dd-MM-yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = durationHours,
                    onValueChange = { durationHours = it },
                    label = { Text("Duration (hours)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = reminderValue,
                    onValueChange = { reminderValue = it.filter { ch -> ch.isDigit() }.ifEmpty { "" } },
                    label = { Text("Reminder (minutes before, optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        try {
                            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                            val start = sdf.parse("$startDate $startTime")?.time
                            val durationMs = (durationHours.toLongOrNull() ?: 1L) * 3600000L
                            if (start != null) {
                                val reminder = reminderValue.toIntOrNull()
                                onSave(title.trim(), description.trim(), start, start + durationMs, reminder)
                            }
                        } catch (e: Exception) {
                            // Invalid date - just ignore
                        }
                    }
                },
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}