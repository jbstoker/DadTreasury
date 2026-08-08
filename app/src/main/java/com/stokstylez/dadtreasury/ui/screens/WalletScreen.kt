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
import com.stokstylez.dadtreasury.domain.model.LedgerEntryType
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    // Using "child-1" as the default child for this prototype
    val walletTx by repository.observeWallet("child-1").collectAsState(initial = emptyList())
    val timeTx by repository.observeTimeBank("child-1").collectAsState(initial = emptyList())
    val walletBalanceCents by repository.observeWalletBalance("child-1").collectAsState(initial = 0L)
    val timeBalanceMinutes by repository.observeTimeBankBalance("child-1").collectAsState(initial = 0L)
    val scope = rememberCoroutineScope()

    var showWalletDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Wallet & Time Bank", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    IconButton(onClick = { showWalletDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add wallet tx", tint = tokens.accentPrimary)
                    }
                    IconButton(onClick = { showTimeDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add time tx", tint = tokens.success)
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
            // Balance cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("💰 Wallet", style = MaterialTheme.typography.titleSmall, color = tokens.textSecondary)
                        Text(
                            "€${walletBalanceCents / 100.0}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = tokens.accentPrimary,
                        )
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("⏰ Time Bank", style = MaterialTheme.typography.titleSmall, color = tokens.textSecondary)
                        Text(
                            "${timeBalanceMinutes} min",
                            style = MaterialTheme.typography.headlineLarge,
                            color = tokens.success,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            Spacer(Modifier.height(8.dp))

            if (walletTx.isEmpty() && timeTx.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No transactions yet. Complete tasks to earn rewards!",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            }

            walletTx.take(10).forEach { tx ->
                TransactionRow(
                    icon = "💰",
                    label = tx.note,
                    amount = if (tx.type == LedgerEntryType.CREDIT) "+€${tx.amountCents / 100.0}" else "-€${tx.amountCents / 100.0}",
                    isCredit = tx.type == LedgerEntryType.CREDIT,
                    timestamp = tx.timestamp,
                    tokens = tokens,
                )
            }

            timeTx.take(10).forEach { tx ->
                TransactionRow(
                    icon = "⏰",
                    label = tx.note,
                    amount = if (tx.type == LedgerEntryType.CREDIT) "+${tx.amountMinutes}m" else "-${tx.amountMinutes}m",
                    isCredit = tx.type == LedgerEntryType.CREDIT,
                    timestamp = tx.timestamp,
                    tokens = tokens,
                )
            }
        }
    }

    if (showWalletDialog) {
        LedgerEntryDialog(
            title = "Wallet Transaction",
            onDismiss = { showWalletDialog = false },
            onSave = { type, amount ->
                scope.launch {
                    repository.addWalletTransaction("child-1", type, amount, "Manual entry")
                }
                showWalletDialog = false
            },
        )
    }

    if (showTimeDialog) {
        LedgerEntryDialog(
            title = "Time Bank Transaction",
            onDismiss = { showTimeDialog = false },
            onSave = { type, amount ->
                scope.launch {
                    repository.addTimeTransaction("child-1", type, amount, "Manual entry")
                }
                showTimeDialog = false
            },
        )
    }
}

@Composable
fun TransactionRow(
    icon: String,
    label: String,
    amount: String,
    isCredit: Boolean,
    timestamp: Long,
    tokens: com.stokstylez.dadtreasury.ui.theme.SemanticTokens,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = tokens.card),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
                Text(
                    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                )
            }
            Text(
                amount,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCredit) tokens.success else tokens.error,
            )
        }
    }
}

@Composable
fun LedgerEntryDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (LedgerEntryType, Long) -> Unit,
) {
    var type by remember { mutableStateOf(LedgerEntryType.CREDIT) }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LedgerEntryType.CREDIT, LedgerEntryType.DEBIT, LedgerEntryType.CORRECTION).forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.name) },
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (positive number)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amount.toLongOrNull()
                    if (value != null && value > 0) {
                        onSave(type, value)
                    }
                },
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}