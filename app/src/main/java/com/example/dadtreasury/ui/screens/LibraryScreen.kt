package com.example.dadtreasury.ui.screens

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
import com.example.dadtreasury.data.DadTreasuryRepository
import com.example.dadtreasury.domain.model.LibraryPage
import com.example.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val categories by repository.observeCategories().collectAsState(initial = emptyList())
    val pages by repository.observePages().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedPage by remember { mutableStateOf<LibraryPage?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showAddPage by remember { mutableStateOf(false) }

    val filteredPages = if (selectedCategoryId == null) pages else pages.filter { it.categoryId == selectedCategoryId }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Library", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    IconButton(onClick = { showAddCategory = true }) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "Add category", tint = tokens.accentPrimary)
                    }
                    IconButton(onClick = { showAddPage = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add page", tint = tokens.accentPrimary)
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
            // Category chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null; selectedPage = null },
                    label = { Text("All") },
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id; selectedPage = null },
                        label = { Text(category.name) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (filteredPages.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No pages yet. Create a category and add pages!",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                filteredPages.forEach { page ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { selectedPage = page },
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = tokens.accentPrimary)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(page.title, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                                if (page.tags.isNotEmpty()) {
                                    Text(
                                        page.tags.joinToString(" · "),
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

    // Page detail
    selectedPage?.let { page ->
        AlertDialog(
            onDismissRequest = { selectedPage = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(page.title, modifier = Modifier.weight(1f))
                    Text("rev ${page.revision}", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(page.body, color = tokens.textPrimary)
                    Text(
                        "Tags: ${if (page.tags.isEmpty()) "none" else page.tags.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPage = null }) { Text("Close") }
            },
        )
    }

    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onSave = { name ->
                scope.launch { repository.addCategory(name) }
                showAddCategory = false
            },
        )
    }

    if (showAddPage) {
        AddPageDialog(
            categories = categories,
            onDismiss = { showAddPage = false },
            onSave = { categoryId, title, body, tags ->
                scope.launch { repository.addPage(categoryId, title, body, tags) }
                showAddPage = false
            },
        )
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category name") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun AddPageDialog(
    categories: List<com.example.dadtreasury.domain.model.LibraryCategory>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, List<String>) -> Unit,
) {
    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Page") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (categories.isNotEmpty()) {
                    Text("Category")
                    categories.forEach { category ->
                        FilterChip(
                            selected = categoryId == category.id,
                            onClick = { categoryId = category.id },
                            label = { Text(category.name) },
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && categoryId.isNotBlank()) {
                        onSave(
                            categoryId,
                            title.trim(),
                            body,
                            tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        )
                    }
                },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}