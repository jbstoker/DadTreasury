package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.TaskStatus
import com.stokstylez.dadtreasury.ui.Routes
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
fun HomeScreen(
    repository: DadTreasuryRepository,
    role: String?,
    onNavigate: (String) -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    val tasks by repository.observeTasks().collectAsState(initial = emptyList())

    val isParent = role == "PARENT"

    val today = remember { System.currentTimeMillis() }
    val todayTasks = tasks.filter {
        it.dueTimestamp != null && it.dueTimestamp!! >= today - 86400000L && it.dueTimestamp!! <= today + 86400000L
    }
    val openTasks = tasks.count { it.status == TaskStatus.OPEN }
    val pendingApprovals = tasks.count { it.status == TaskStatus.COMPLETED }
    val completedToday = tasks.count { it.status == TaskStatus.APPROVED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = if (isParent) "📋 Parent Dashboard" else "⭐ Today",
            style = MaterialTheme.typography.headlineMedium,
            color = tokens.textPrimary,
        )

        Spacer(Modifier.height(16.dp))

        // Summary cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                label = "Open Tasks",
                value = openTasks.toString(),
                color = tokens.accentPrimary,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                label = if (isParent) "To Approve" else "Completed",
                value = if (isParent) pendingApprovals.toString() else completedToday.toString(),
                color = tokens.warning,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Quick navigation grid
        if (isParent) {
            NavigationGrid(
                items = listOf(
                    HomeNavItem(Routes.TASKS, "Tasks", Icons.Filled.Checklist),
                    HomeNavItem(Routes.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet),
                    HomeNavItem(Routes.CALENDAR, "Calendar", Icons.Filled.DateRange),
                    HomeNavItem(Routes.CHAT, "Chat", Icons.Filled.Forum),
                    HomeNavItem(Routes.LIBRARY, "Library", Icons.AutoMirrored.Filled.MenuBook),
                    HomeNavItem(Routes.LOCATION, "Locations", Icons.Filled.LocationOn),
                    HomeNavItem(Routes.PAIRING, "Pairing", Icons.Filled.Link),
                    HomeNavItem(Routes.CONNECT_PARENTS, "Connect", Icons.Filled.Group),
                    HomeNavItem(Routes.DIAGNOSTICS, "Diagnostics", Icons.Filled.Build),
                ),
                onNavigate = onNavigate,
            )
        } else {
            NavigationGrid(
                items = listOf(
                    HomeNavItem(Routes.TASKS, "My Tasks", Icons.Filled.Checklist),
                    HomeNavItem(Routes.WALLET, "My Wallet", Icons.Filled.AccountBalanceWallet),
                    HomeNavItem(Routes.CALENDAR, "Calendar", Icons.Filled.DateRange),
                    HomeNavItem(Routes.CHAT, "Chat", Icons.Filled.Forum),
                    HomeNavItem(Routes.LIBRARY, "Library", Icons.AutoMirrored.Filled.MenuBook),
                ),
                onNavigate = onNavigate,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Today's tasks
        Text("Today's Tasks", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
        Spacer(Modifier.height(8.dp))

        if (todayTasks.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "No tasks scheduled for today. Enjoy the calm! 🌿",
                    modifier = Modifier.padding(16.dp),
                    color = tokens.textSecondary,
                )
            }
        } else {
            todayTasks.take(5).forEach { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onNavigate(Routes.taskDetail(task.id)) },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.RadioButtonChecked,
                            contentDescription = null,
                            tint = when (task.status) {
                                TaskStatus.APPROVED -> tokens.success
                                TaskStatus.COMPLETED -> tokens.warning
                                else -> tokens.accentPrimary
                            },
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                            Text(
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(task.dueTimestamp ?: 0)),
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalSemanticTokens.current
    Card(
        colors = CardDefaults.cardColors(containerColor = tokens.card),
        modifier = modifier,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
        }
    }
}

@Composable
fun NavigationGrid(
    items: List<HomeNavItem>,
    onNavigate: (String) -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        onClick = { onNavigate(item.route) },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(item.icon, contentDescription = item.label, tint = tokens.accentPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(item.label, style = MaterialTheme.typography.labelMedium, color = tokens.textPrimary)
                        }
                    }
                }
            }
        }
    }
}