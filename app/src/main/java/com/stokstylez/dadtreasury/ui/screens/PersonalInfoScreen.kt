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
import com.stokstylez.dadtreasury.data.SettingsRepository
import com.stokstylez.dadtreasury.domain.model.PersonalInfo
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    settingsRepository: SettingsRepository,
    role: String?,
) {
    val tokens = LocalSemanticTokens.current
    val isParent = role == "PARENT"
    val scope = rememberCoroutineScope()
    val info by settingsRepository.personalInfo.collectAsState(initial = PersonalInfo())

    // Local editable state (parent only)
    var dadName by remember { mutableStateOf(info.dadName) }
    var dadPhone by remember { mutableStateOf(info.dadPhone) }
    var dadEmail by remember { mutableStateOf(info.dadEmail) }
    var dadAddress by remember { mutableStateOf(info.dadAddress) }
    var momName by remember { mutableStateOf(info.momName) }
    var momPhone by remember { mutableStateOf(info.momPhone) }
    var momEmail by remember { mutableStateOf(info.momEmail) }
    var momAddress by remember { mutableStateOf(info.momAddress) }
    var childName by remember { mutableStateOf(info.childName) }
    var childEmail by remember { mutableStateOf(info.childEmail) }
    var childMobile by remember { mutableStateOf(info.childMobile) }
    var childBirth by remember { mutableStateOf(info.childBirthdate) }
    var shoe by remember { mutableStateOf(info.shoeSize) }
    var jeans by remember { mutableStateOf(info.jeansSize) }
    var shirt by remember { mutableStateOf(info.shirtSize) }
    var jacket by remember { mutableStateOf(info.jacketSize) }
    var hat by remember { mutableStateOf(info.hatSize) }
    var dress by remember { mutableStateOf(info.dressSize) }
    var saved by remember { mutableStateOf(false) }

    // Sync local state when the flow emits (e.g., on first load)
    LaunchedEffect(info) {
        dadName = info.dadName; dadPhone = info.dadPhone; dadEmail = info.dadEmail; dadAddress = info.dadAddress
        momName = info.momName; momPhone = info.momPhone; momEmail = info.momEmail; momAddress = info.momAddress
        childName = info.childName; childEmail = info.childEmail; childMobile = info.childMobile; childBirth = info.childBirthdate
        shoe = info.shoeSize; jeans = info.jeansSize; shirt = info.shirtSize; jacket = info.jacketSize; hat = info.hatSize; dress = info.dressSize
    }

    fun save() {
        scope.launch {
            settingsRepository.setPersonalInfo(
                PersonalInfo(
                    dadName = dadName, dadPhone = dadPhone, dadEmail = dadEmail, dadAddress = dadAddress,
                    momName = momName, momPhone = momPhone, momEmail = momEmail, momAddress = momAddress,
                    childName = childName, childEmail = childEmail, childMobile = childMobile, childBirthdate = childBirth,
                    shoeSize = shoe, jeansSize = jeans, shirtSize = shirt, jacketSize = jacket, hatSize = hat, dressSize = dress,
                )
            )
            saved = true
            kotlinx.coroutines.delay(2000)
            saved = false
        }
    }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Personal Info", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!isParent) {
                Text(
                    "This information is visible to you for reference.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary,
                )
            }

            // Dad card
            InfoCard(title = "👨 Dad", icon = Icons.Filled.Person, tokens = tokens) {
                InfoField("Name", dadName, isParent) { dadName = it }
                InfoField("Phone", dadPhone, isParent) { dadPhone = it }
                InfoField("Email", dadEmail, isParent) { dadEmail = it }
                InfoField("Address", dadAddress, isParent) { dadAddress = it }
            }

            // Mom card
            InfoCard(title = "👩 Mom", icon = Icons.Filled.Favorite, tokens = tokens) {
                InfoField("Name", momName, isParent) { momName = it }
                InfoField("Phone", momPhone, isParent) { momPhone = it }
                InfoField("Email", momEmail, isParent) { momEmail = it }
                InfoField("Address", momAddress, isParent) { momAddress = it }
            }

            // Child card
            InfoCard(title = "🧒 Me", icon = Icons.Filled.AccountCircle, tokens = tokens) {
                InfoField("Name", childName, isParent) { childName = it }
                InfoField("Email", childEmail, isParent) { childEmail = it }
                InfoField("Mobile", childMobile, isParent) { childMobile = it }
                InfoField("Birthdate", childBirth, isParent) { childBirth = it }
            }

            // Sizes card
            InfoCard(title = "📏 Sizes", icon = Icons.Filled.Home, tokens = tokens) {
                InfoField("Shoe size", shoe, isParent) { shoe = it }
                InfoField("Jeans size", jeans, isParent) { jeans = it }
                InfoField("Shirt size", shirt, isParent) { shirt = it }
                InfoField("Jacket size", jacket, isParent) { jacket = it }
                InfoField("Hat size", hat, isParent) { hat = it }
                InfoField("Dress size", dress, isParent) { dress = it }
            }

            if (isParent) {
                Button(onClick = { save() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (saved) "Saved ✓" else "Save")
                }
                Text(
                    "Fillable by parent. Read-only for the child.",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tokens: com.stokstylez.dadtreasury.ui.theme.SemanticTokens,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tokens.accentPrimary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
            }
            content()
        }
    }
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    editable: Boolean,
    onValue: (String) -> Unit,
) {
    if (editable) {
        OutlinedTextField(
            value = value,
            onValueChange = onValue,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = LocalSemanticTokens.current.textSecondary, modifier = Modifier.width(110.dp))
            Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = LocalSemanticTokens.current.textPrimary)
        }
    }
}