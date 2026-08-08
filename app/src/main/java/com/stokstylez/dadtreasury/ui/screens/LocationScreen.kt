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
import androidx.compose.ui.viewinterop.AndroidView
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

/**
 * osmdroid map composable - no Google Play Services needed.
 */
@Composable
fun rememberOsmdroidMap(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        // Use OpenStreetMap standard tiles (no API key required)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 3.0
            maxZoomLevel = 19.0
            controller.setZoom(12.0)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
    return mapView
}

/**
 * Displays a full interactive map and returns the tapped coordinates via [onLocationPicked].
 */
@Composable
fun LocationPickerMap(
    onLocationPicked: (Double, Double, String) -> Unit,
    initialLat: Double? = null,
    initialLng: Double? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = rememberOsmdroidMap()

    // Track a draggable marker for the selected position
    var marker by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        val startLat = initialLat ?: 52.3676
        val startLng = initialLng ?: 4.9041
        val startPoint = GeoPoint(startLat, startLng)
        mapView.controller.setCenter(startPoint)
        mapView.controller.setZoom(13.0)

        val m = Marker(mapView).apply {
            position = startPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Selected location"
            isDraggable = true
            mapView.overlays.add(this)
        }
        marker = m

        // My-location overlay (requires location permission)
        val myLocation = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
        myLocation.enableMyLocation()
        mapView.overlays.add(myLocation)

        // Tap on map moves the marker
        mapView.overlays.add(
            object : org.osmdroid.views.overlay.Overlay() {
                override fun onTouchEvent(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                    if (e?.action == android.view.MotionEvent.ACTION_UP && mapView != null) {
                        val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt())
                        m.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                        onLocationPicked(geoPoint.latitude, geoPoint.longitude, "")
                    }
                    return super.onTouchEvent(e, mapView)
                }
            }
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(repository: DadTreasuryRepository, role: String? = null) {
    val tokens = LocalSemanticTokens.current
    val rules by repository.observeGeoRules().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val isParent = role == "PARENT"
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Location Rules", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    if (isParent) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add location rule", tint = tokens.accentPrimary)
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
            onSave = { title, message, lat, lng, radius, startHour, endHour, targetRole ->
                scope.launch {
                    repository.addGeoRule(
                        title = title,
                        message = message,
                        latitude = lat,
                        longitude = lng,
                        radiusMeters = radius,
                        activeStartHour = startHour,
                        activeEndHour = endHour,
                        targetRole = targetRole,
                    )
                }
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGeoRuleDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Int, Int, Int, String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    // Selected coordinates (start centered on Netherlands)
    var pickedLat by remember { mutableStateOf(52.3676) }
    var pickedLng by remember { mutableStateOf(4.9041) }
    var radius by remember { mutableStateOf("150") }
    var startHour by remember { mutableStateOf("0") }
    var endHour by remember { mutableStateOf("24") }
    var searchStatus by remember { mutableStateOf<String?>(null) }
    var showMapSheet by remember { mutableStateOf(false) }
    var resolvedPlaceName by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("CHILD") }

    fun resolveAddress(query: String) {
        if (query.isNotBlank()) {
            scope.launch {
                searchStatus = "Searching..."
                val results = withContext(Dispatchers.IO) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val list = geocoder.getFromLocationName(query, 1) ?: emptyList()
                        list.map { Triple(it.latitude, it.longitude, it.getAddressLine(0) ?: "") }
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                if (results.isNotEmpty()) {
                    val (lat, lng, name) = results.first()
                    pickedLat = lat
                    pickedLng = lng
                    resolvedPlaceName = name
                    searchStatus = "✓ Found: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}"
                } else {
                    searchStatus = "Place not found - use the map to pick a location"
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
                            resolveAddress(address)
                        }
                    }
                }
            }
        }
    )


    val radiusOptions = listOf(50, 100, 150, 250, 500, 1000, 2000, 5000)
    var radiusChoice by remember { mutableStateOf("150") }

    // If the map sheet closes with picked coordinates, those are already set
    if (showMapSheet) {
        AlertDialog(
            onDismissRequest = { showMapSheet = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Resolve a human-readable place name for the picked coordinates
                        scope.launch {
                            val name = withContext(Dispatchers.IO) {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val addr = geocoder.getFromLocation(pickedLat, pickedLng, 1)?.firstOrNull()
                                    addr?.getAddressLine(0) ?: "Picked location"
                                } catch (_: Exception) {
                                    "Picked location"
                                }
                            }
                            resolvedPlaceName = name
                        }
                        showMapSheet = false
                    },
                ) { Text("Use this location") }
            },
            dismissButton = { TextButton(onClick = { showMapSheet = false }) { Text("Cancel") } },
            title = { Text("Pick location on map") },
            text = {
                Column {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Search address") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { resolveAddress(placeName) }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = LocalSemanticTokens.current.accentPrimary)
                            }
                        },
                    )
                    searchStatus?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = LocalSemanticTokens.current.success)
                    }
                    Spacer(Modifier.height(8.dp))
                    LocationPickerMap(
                        initialLat = pickedLat,
                        initialLng = pickedLng,
                        onLocationPicked = { lat, lng, _ ->
                            pickedLat = lat
                            pickedLng = lng
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "📍 ${"%.5f".format(pickedLat)}, ${"%.5f".format(pickedLng)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalSemanticTokens.current.textPrimary,
                    )
                }
            },
        )
    }

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

                // Who should receive this notification?
                Text("Notify", style = MaterialTheme.typography.labelMedium, color = LocalSemanticTokens.current.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetRole == "CHILD",
                        onClick = { targetRole = "CHILD" },
                        label = { Text("🧒 Child") },
                    )
                    FilterChip(
                        selected = targetRole == "PARENT",
                        onClick = { targetRole = "PARENT" },
                        label = { Text("👨 Parent (personal)") },
                    )
                }

                // Map picker button - hides the coordinate fields
                Card(
                    colors = CardDefaults.cardColors(containerColor = LocalSemanticTokens.current.card),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showMapSheet = true },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Map,
                            contentDescription = null,
                            tint = LocalSemanticTokens.current.accentPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Select Location on Map", style = MaterialTheme.typography.bodyLarge, color = LocalSemanticTokens.current.textPrimary)
                            Text(
                                resolvedPlaceName.ifBlank { "Tap to open map with address search" },
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalSemanticTokens.current.textSecondary,
                            )
                        }
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = LocalSemanticTokens.current.textSecondary)
                    }
                }

                // Address search + contact picker (quick search without opening full map)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Place name (e.g. Home, School)") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { resolveAddress(placeName) }) {
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

                if (resolvedPlaceName.isNotBlank()) {
                    Text(
                        "📍 ${"%.5f".format(pickedLat)}, ${"%.5f".format(pickedLng)} · $resolvedPlaceName",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalSemanticTokens.current.textSecondary,
                    )
                }

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
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onSave(
                            title.trim(),
                            message.trim(),
                            pickedLat,
                            pickedLng,
                            radius.toIntOrNull() ?: 150,
                            startHour.toIntOrNull() ?: 0,
                            endHour.toIntOrNull() ?: 24,
                            targetRole,
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