package com.stokstylez.dadtreasury.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.domain.model.RewardType
import com.stokstylez.dadtreasury.domain.model.TaskStatus
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    repository: DadTreasuryRepository,
    taskId: String,
    role: String?,
    onBack: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val task by produceState<com.stokstylez.dadtreasury.domain.model.Task?>(initialValue = null, taskId) {
        value = repository.getTask(taskId)
    }
    val isParent = role == "PARENT"

    // Camera capture state
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoUri?.let { uri ->
                    scope.launch {
                        repository.completeTask(taskId, "child-1", completionPhotoUri = uri.toString())
                        onBack()
                    }
                }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                launchCamera(context, photoUri) { uri ->
                    photoUri = uri
                    takePictureLauncher.launch(uri)
                }
            }
        }
    )

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Task Detail", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.textPrimary)
                    }
                },
            )
        },
    ) { padding ->
        val currentTask = task
        if (currentTask == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Task not found", color = tokens.textSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(currentTask.title, style = MaterialTheme.typography.headlineMedium, color = tokens.textPrimary)
                Spacer(Modifier.height(8.dp))
                TaskStatusBadge(currentTask.status, tokens)

                currentTask.description.let {
                    if (it.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(it, style = MaterialTheme.typography.bodyLarge, color = tokens.textSecondary)
                    }
                }

                currentTask.dueTimestamp?.let { ts ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Due: ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(ts))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary,
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Expected: ${currentTask.expectedDurationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary,
                )

                if (currentTask.rewardType != RewardType.FREE) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        when (currentTask.rewardType) {
                            RewardType.PAID -> "💰 Reward: €${currentTask.rewardAmount / 100.0}"
                            RewardType.TIME -> "⏰ Reward: ${currentTask.rewardAmount} min"
                            RewardType.FREE -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.success,
                    )
                }

                // Completion photo display
                currentTask.completionPhotoUri?.let { uriString ->
                    Spacer(Modifier.height(16.dp))
                    Text("📸 Completion Photo", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = Uri.parse(uriString),
                        contentDescription = "Task completion photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop,
                    )
                }

                if (currentTask.checklist.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Checklist", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
                    currentTask.checklist.forEach { item ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircleOutline, contentDescription = null, tint = tokens.accentPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text(item, color = tokens.textSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Role-specific actions
                when {
                    isParent && currentTask.status == TaskStatus.COMPLETED -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.approveTask(currentTask.id, "child-1")
                                        onBack()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("✓ Approve")
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.rejectTask(currentTask.id)
                                        onBack()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("✗ Reject")
                            }
                        }
                    }
                    !isParent && currentTask.status == TaskStatus.OPEN -> {
                        Button(
                            onClick = {
                                if (hasCameraPermission) {
                                    launchCamera(context, photoUri) { uri ->
                                        photoUri = uri
                                        takePictureLauncher.launch(uri)
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("📷 Take photo & finish")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates a camera output file and returns its content URI.
 */
private fun launchCamera(
    context: android.content.Context,
    existingUri: Uri?,
    onReady: (Uri) -> Unit,
) {
    val photoFile = createImageFile(context)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile,
    )
    onReady(uri)
}

private fun createImageFile(context: android.content.Context): File {
    val storageDir = File(context.cacheDir, "task_photos")
    if (!storageDir.exists()) storageDir.mkdirs()
    return File(
        storageDir,
        "task_${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg",
    )
}