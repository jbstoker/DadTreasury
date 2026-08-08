package com.stokstylez.dadtreasury.ui.screens

import android.content.Intent
import android.location.Geocoder
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(repository: DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val rules by repository.observeGeoRules().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Location Rules", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add location rule", tint = tokens.accentPrimary)
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
                    "Location rules trigger local reminders when the child enters an area. 📍",
                    modifier = Modifier.padding(16.dp),
                    color = tokens.textSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))

            if (rules.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No location rules yet. Add one with + !",
                        modifier = Modifier.padding(16.dp),
                        color = tokens.textSecondary,
                    )
                }
            } else {
                rules.forEach { rule ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = tokens.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Place, contentDescription = null, tint = if (rule.isEnabled) tokens.success else tokens.textSecondary)
                                Spacer(Modifier.width(8.dp))
                                Text(rule.title, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary, modifier = Modifier.weight(1f))
                                Switch(checked = rule.isEnabled, onCheckedChange = null)
                            }
                            Text(rule.message, style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
                            Text(
                                "📍 ${rule.latitude}, ${rule.longitude} · radius ${rule.radiusMeters}m · active ${rule.activeStartHour}-${rule.activeEndHour}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddGeoRuleDialog(
            onDismiss = { showDialog = false },
            onSave = { title, message, lat, lng, radius, startHour, endHour ->
                scope.launch {
                    repository.addGeoRule(
                        title = title,
                        message = message,
                        latitude = lat,
                        longitude = lng,
                        radiusMeters = radius,
                        activeStartHour = startHour,
                        activeEndHour = endHour,
                    )
                }
                showDialog = false
            },
        )
    }
}

@Composable
fun AddGeoRuleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Int, Int, Int) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("150") }
    var startHour by remember { mutableStateOf("0") }
    var endHour by remember { mutableStateOf("24") }
    var searchStatus by remember { mutableStateOf<String?>(null) }
    var maxResults by remember { mutableStateOf("5") }

    fun resolveCoordinates() {
        val lat = latitude.toDoubleOrNull()
        val lng = longitude.toDoubleOrNull()
        if (lat != null && lng != null) return

        if (placeName.isNotBlank()) {
            scope.launch {
                searchStatus = "Searching..."
                val results = withContext(Dispatchers.IO) {
                    searchPlacesSmart(context, placeName, maxResults.toIntOrNull() ?: 5)
                }
                if (results.isNotEmpty()) {
                    val first = results.first()
                    latitude = first.first.toString()
                    longitude = first.second.toString()
                    searchStatus = if (results.size > 1) {
                        "✓ ${results.size} locations found - using most relevant"
                    } else {
                        "✓ Found: ${"%.4f".format(first.first)}, ${"%.4f".format(first.second)}"
                    }
                } else {
                    searchStatus = "Place not found - enter coordinates manually"
                }
            }
        }
    }

    // Contact address picker (uses ContactsContract - no Play Services)
    val contactPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { res ->
            val uri = res.data?.data
            if (uri != null) {
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                    ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                    ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                    ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                    ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY
                )
                context.contentResolver.query(uri, projection, null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val formatted = c.getString(0)
                        val address = if (!formatted.isNullOrBlank()) {
                            formatted
                        } else {
                            listOfNotNull(
                                c.getString(1), c.getString(2), c.getString(3), c.getString(4)
                            ).filter { it.isNotBlank() }.joinToString(", ")
                        }
                        if (address.isNotBlank()) {
                            placeName = address
                            resolveCoordinates()
                        }
                    }
                }
            }
        }
    )

    val radiusOptions = listOf(50, 100, 150, 250, 500, 1000, 2000, 5000)
    var radiusChoice by remember { mutableStateOf("150") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Location Rule") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message *") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Place name (e.g. Home, School)") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { resolveCoordinates() }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search place", tint = LocalSemanticTokens.current.accentPrimary)
                            }
                        },
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI)
                        contactPicker.launch(intent)
                    }) {
                        Icon(Icons.Filled.Contacts, contentDescription = "Pick contact address", tint = LocalSemanticTokens.current.accentPrimary)
                    }
                }
                searchStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = LocalSemanticTokens.current.success)
                }
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Radius", style = MaterialTheme.typography.labelMedium, color = LocalSemanticTokens.current.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    radiusOptions.forEach { opt ->
                        FilterChip(
                            selected = radiusChoice == opt.toString(),
                            onClick = {
                                radiusChoice = opt.toString()
                                radius = opt.toString()
                            },
                            label = { Text("${opt}m") },
                        )
                    }
                }
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it.filter { ch -> ch.isDigit() }.ifEmpty { "" } },
                    label = { Text("Custom radius (meters)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it },
                        label = { Text("Active from (h)") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it },
                        label = { Text("Active until (h)") },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()
                    if (title.isNotBlank() && message.isNotBlank() && lat != null && lng != null) {
                        onSave(
                            title.trim(),
                            message.trim(),
                            lat,
                            lng,
                            radius.toIntOrNull() ?: 150,
                            startHour.toIntOrNull() ?: 0,
                            endHour.toIntOrNull() ?: 24,
                        )
                    }
                },
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Smart geocoding: split "Brand, City/Area" so it searches the brand within a
 * bounding box around the area (ported from ProximityNotes, no network needed).
 */
private fun searchPlacesSmart(
    context: android.content.Context,
    query: String,
    max: Int,
): List<Pair<Double, Double>> {
    if (query.isBlank()) return emptyList()

    fun splitBrandArea(q: String): Pair<String, String?> {
        val raw = q.trim()
        if (raw.isBlank()) return "" to null
        if (raw.contains(',')) {
            val parts = raw.split(',')
            return parts.first().trim() to parts.drop(1).joinToString(",").trim().ifBlank { null }
        }
        val tokens = raw.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (tokens.size <= 1) return raw to null
        return tokens.first() to tokens.drop(1).joinToString(" ").trim().ifBlank { null }
    }

    fun bboxAround(lat: Double, lng: Double, meters: Double): DoubleArray {
        val latDelta = meters / 111_000.0
        val cosLat = kotlin.math.cos(lat * Math.PI / 180.0).coerceAtLeast(0.0001)
        val lngDelta = meters / (111_000.0 * cosLat)
        return doubleArrayOf(lat - latDelta, lng - lngDelta, lat + latDelta, lng + lngDelta)
    }

    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val (brand, area) = splitBrandArea(query)
        val cappedMax = max.coerceIn(1, 20)

        if (!area.isNullOrBlank() && brand.isNotBlank()) {
            val areaRes = geocoder.getFromLocationName(area, 1)
            val areaFirst = areaRes?.firstOrNull()
            if (areaFirst != null) {
                val tokenCount = area.split(Regex("\\s+")).count { it.isNotBlank() }
                val meters = if (tokenCount >= 2) 3000.0 else 7000.0
                val box = bboxAround(areaFirst.latitude, areaFirst.longitude, meters)
                val scoped = geocoder.getFromLocationName(brand, cappedMax, box[0], box[1], box[2], box[3])
                    ?: emptyList()
                if (scoped.isNotEmpty()) return scoped.map { Pair(it.latitude, it.longitude) }
            }
        }

        val results = geocoder.getFromLocationName(query, cappedMax) ?: emptyList()
        results.map { Pair(it.latitude, it.longitude) }
    } catch (_: Exception) {
        emptyList()
    }
}