package com.stokstylez.dadtreasury.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.LibraryCategory
import com.stokstylez.dadtreasury.domain.model.LibraryPage
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(repository: DadTreasuryRepository, role: String? = null) {
    val tokens = LocalSemanticTokens.current
    val categories by repository.observeCategories().collectAsState(initial = emptyList())
    val pages by repository.observePages().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val isParent = role == "PARENT"

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedPage by remember { mutableStateOf<LibraryPage?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showAddPage by remember { mutableStateOf(false) }
    var showImportExport by remember { mutableStateOf(false) }

    val filteredPages = if (selectedCategoryId == null) pages else pages.filter { it.categoryId == selectedCategoryId }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Library", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    if (isParent) {
                        IconButton(onClick = { showImportExport = true }) {
                            Icon(Icons.Filled.SwapVert, contentDescription = "Import/Export", tint = tokens.accentSecondary)
                        }
                        IconButton(onClick = { showAddCategory = true }) {
                            Icon(Icons.Filled.CreateNewFolder, contentDescription = "Add category", tint = tokens.accentPrimary)
                        }
                        IconButton(onClick = { showAddPage = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add page", tint = tokens.accentPrimary)
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

    // Page detail with rich rendering
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RichTextBody(page.body, tokens)
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

    if (showImportExport) {
        ImportExportDialog(
            repository = repository,
            pages = pages,
            categories = categories,
            onDismiss = { showImportExport = false },
        )
    }
}

/** Lightweight rich-text rendering: # headings, **bold**, *italic*, - lists, ![alt](uri). */
@Composable
fun RichTextBody(body: String, tokens: com.stokstylez.dadtreasury.ui.theme.SemanticTokens, modifier: Modifier = Modifier) {
    val lines = body.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                // Image: ![alt](uri)
                line.trim().startsWith("![") -> {
                    val uriStr = line.substringAfter("](").substringBefore(")")
                    if (uriStr.isNotBlank()) {
                        AsyncImage(
                            model = uriStr,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                // Heading 3
                line.trim().startsWith("### ") -> {
                    Text(line.trim().removePrefix("### "), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                }
                // Heading 2
                line.trim().startsWith("## ") -> {
                    Text(line.trim().removePrefix("## "), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = tokens.textPrimary)
                }
                // Heading 1
                line.trim().startsWith("# ") -> {
                    Text(line.trim().removePrefix("# "), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = tokens.accentPrimary)
                }
                // Bullet
                line.trim().startsWith("- ") -> {
                    Text(
                        "•  ${richInlineText(line.trim().removePrefix("- "))}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = tokens.textPrimary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                // Numbered
                line.trim().matches(Regex("\\d+\\.\\s+.*")) -> {
                    Text(
                        richInlineText(line.trim()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = tokens.textPrimary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                // Horizontal rule
                line.trim() == "---" -> HorizontalDivider(color = tokens.border, modifier = Modifier.padding(vertical = 4.dp))
                // Blank
                line.isBlank() -> {}
                // Normal paragraph
                else -> {
                    Text(
                        richInlineText(line),
                        style = MaterialTheme.typography.bodyLarge,
                        color = tokens.textPrimary,
                    )
                }
            }
        }
    }
}

/** Parse **bold** and *italic* markers into AnnotatedString. */
private fun richInlineText(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        // Process **bold** segments
        val boldPattern = Regex("\\*\\*(.+?)\\*\\*")
        val parts = boldPattern.split(text)
        val boldMatches = boldPattern.findAll(text).toList()

        parts.forEachIndexed { index, part ->
            if (part.isNotEmpty()) {
                append(part)
            }
            if (index < boldMatches.size) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldMatches[index].groupValues[1])
                }
            }
        }
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
    categories: List<com.stokstylez.dadtreasury.domain.model.LibraryCategory>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, List<String>) -> Unit,
) {
    val context = LocalContext.current
    val tokens = LocalSemanticTokens.current
    var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // Image picker - copies image to internal storage so the URI persists
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val copiedUri = copyImageToInternal(context, uri)
                if (copiedUri != null) {
                    if (body.isNotBlank() && !body.endsWith("\n")) body += "\n"
                    body += "![image]($copiedUri)\n"
                }
            }
        }
    )

    fun insertFormat(prefix: String, suffix: String = "") {
        if (body.isNotBlank() && !body.endsWith("\n")) body += "\n"
        body += "$prefix$suffix\n"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Page") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (categories.isNotEmpty()) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = categoryId == category.id,
                                onClick = { categoryId = category.id },
                                label = { Text(category.name) },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Formatting toolbar
                Text("Formatting", style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val btnMod = Modifier.size(width = 44.dp, height = 36.dp)
                    FilledTonalButton(onClick = { insertFormat("## ") }, modifier = btnMod) {
                        Text("H", fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(onClick = { insertFormat("**", "**") }, modifier = btnMod) {
                        Text("B", fontWeight = FontWeight.Bold)
                    }
                    FilledTonalButton(onClick = { insertFormat("*", "*") }, modifier = btnMod) {
                        Text("I", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                    FilledTonalButton(onClick = { insertFormat("- ") }, modifier = btnMod) {
                        Text("•")
                    }
                    FilledTonalButton(onClick = { insertFormat("---\n") }, modifier = btnMod) {
                        Text("—")
                    }
                    FilledTonalButton(
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = btnMod,
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = "Add image", tint = tokens.accentPrimary)
                    }
                }

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Content (## heading, **bold**, - list, image via 🖼️)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                )

                // Live preview
                if (body.isNotBlank()) {
                    Text("Preview", style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                    Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                        RichTextBody(body, tokens, modifier = Modifier.padding(12.dp))
                    }
                }

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

/** Copy a picked image into internal storage so the URI remains valid long-term. */
private fun copyImageToInternal(context: android.content.Context, uri: Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val dir = File(context.filesDir, "wiki_images")
        if (!dir.exists()) dir.mkdirs()
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val outFile = File(dir, fileName)
        input.use { ins ->
            outFile.outputStream().use { os -> ins.copyTo(os) }
        }
        outFile.toURI().toString()
    } catch (_: Exception) {
        null
    }
}

/** XML Import/Export dialog for library data. */
@Composable
fun ImportExportDialog(
    repository: DadTreasuryRepository,
    pages: List<LibraryPage>,
    categories: List<LibraryCategory>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val tokens = LocalSemanticTokens.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    // Export via SAF (Storage Access Framework)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/xml"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val xml = withContext(Dispatchers.IO) {
                        buildLibraryXml(categories, pages)
                    }
                    val ok = try {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(xml.toByteArray(Charsets.UTF_8))
                        } != null
                    } catch (_: Exception) {
                        false
                    }
                    message = if (ok) "✓ Exported ${pages.size} pages to XML" else "Export failed"
                }
            }
        }
    )

    // Import via SAF
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val xml = withContext(Dispatchers.IO) {
                        try {
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (xml != null) {
                        val result = parseLibraryXml(xml)
                        if (result.categories.isNotEmpty() || result.pages.isNotEmpty()) {
                            result.categories.forEach { cat ->
                                val existing = categories.find { it.name == cat.name }
                                if (existing == null) {
                                    repository.addCategory(cat.name)
                                }
                            }
                            result.pages.forEach { page ->
                                val cat = categories.find { it.name == page.categoryName } ?: categories.firstOrNull()
                                if (cat != null) {
                                    repository.addPage(cat.id, page.title, page.body, page.tags)
                                }
                            }
                            message = "✓ Imported ${result.pages.size} pages"
                        } else {
                            message = "No valid data found in XML"
                        }
                    } else {
                        message = "Import failed - could not read file"
                    }
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import / Export") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        exportLauncher.launch("dad_treasury_export.xml")
                    },
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Upload, contentDescription = null, tint = tokens.accentPrimary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Export to XML", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                            Text(
                                "Save all wiki categories and pages as an XML file",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                            )
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.card),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        importLauncher.launch(arrayOf("application/xml", "text/xml", "application/octet-stream"))
                    },
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Download, contentDescription = null, tint = tokens.success)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Import from XML", style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                            Text(
                                "Load categories and pages from an XML file",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.textSecondary,
                            )
                        }
                    }
                }
                message?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = tokens.success)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

/** Internal data holders for parsed XML import. */
private data class ImportedPage(
    val title: String,
    val body: String,
    val tags: List<String>,
    val categoryName: String,
)

private data class ImportedCategory(
    val name: String,
)

private data class LibraryImportResult(
    val categories: List<ImportedCategory>,
    val pages: List<ImportedPage>,
)

/** Build XML document from categories and pages. */
private fun buildLibraryXml(
    categories: List<LibraryCategory>,
    pages: List<LibraryPage>,
): String {
    val sb = StringBuilder()
    sb.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
    sb.appendLine("<dadTreasury version=\"1.0\">")
    sb.appendLine("  <library>")
    sb.appendLine("    <categories>")
    categories.forEach { cat ->
        sb.appendLine("      <category>")
        sb.appendLine("        <name>${xmlEscape(cat.name)}</name>")
        sb.appendLine("      </category>")
    }
    sb.appendLine("    </categories>")
    sb.appendLine("    <pages>")
    pages.forEach { page ->
        sb.appendLine("      <page>")
        sb.appendLine("        <title>${xmlEscape(page.title)}</title>")
        sb.appendLine("        <category>${xmlEscape(categories.find { it.id == page.categoryId }?.name ?: "")}</category>")
        sb.appendLine("        <body>${xmlEscape(page.body)}</body>")
        sb.appendLine("        <tags>${xmlEscape(page.tags.joinToString(","))}</tags>")
        sb.appendLine("      </page>")
    }
    sb.appendLine("    </pages>")
    sb.appendLine("  </library>")
    sb.appendLine("</dadTreasury>")
    return sb.toString()
}

private fun xmlEscape(s: String): String = s
    .replace("&", "&" + "amp;")
    .replace("<", "&" + "lt;")
    .replace(">", "&" + "gt;")
    .replace("\"", "&" + "quot;")
    .replace("'", "&" + "apos;")

/** Parse XML document from imported file. */
private fun parseLibraryXml(xml: String): LibraryImportResult {
    val categories = mutableListOf<ImportedCategory>()
    val pages = mutableListOf<ImportedPage>()
    try {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser: XmlPullParser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var currentTag = ""
        var currentCategory = ""
        var currentTitle = ""
        var currentBody = ""
        var currentTags = ""
        var inCategory = false
        var inPage = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (parser.name) {
                        "category" -> inCategory = true
                        "page" -> {
                            inPage = true
                            currentCategory = ""
                            currentTitle = ""
                            currentBody = ""
                            currentTags = ""
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    when {
                        inCategory && currentTag == "name" && text.isNotBlank() -> {
                            categories.add(ImportedCategory(text))
                        }
                        inPage -> when (currentTag) {
                            "title" -> currentTitle = text
                            "category" -> currentCategory = text
                            "body" -> currentBody += text
                            "tags" -> currentTags = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "category" -> inCategory = false
                        "page" -> {
                            if (currentTitle.isNotBlank()) {
                                pages.add(
                                    ImportedPage(
                                        title = currentTitle,
                                        body = currentBody,
                                        tags = currentTags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        categoryName = currentCategory,
                                    )
                                )
                            }
                            inPage = false
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
    } catch (_: Exception) {
        // Malformed XML - return whatever was parsed
    }
    return LibraryImportResult(categories.distinctBy { it.name }, pages)
}
