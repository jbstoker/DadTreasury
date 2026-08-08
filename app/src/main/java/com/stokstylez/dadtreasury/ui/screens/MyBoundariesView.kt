package com.stokstylez.dadtreasury.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens
import kotlinx.coroutines.launch

enum class EnergyLevel { FULL, LOW, EMPTY }
enum class Motivation { I_WANT_TO, TO_MAKE_FRIENDS_HAPPY, PRESSURED_FORCED }
enum class RoleComfort { ACTIVE_PLAYER, QUIET_HELPER_WATCHER }
enum class CleanupChoice { SPLIT, SOMEONE_ELSE }
enum class TimeLimit { FIFTEEN_MIN, SEE_HOW_IT_GOES }

private enum class WizardStep { STEP_ENERGY, STEP_MOTIVATION, STEP_ROLE, STEP_PRACTICAL, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBoundariesView(repository: com.stokstylez.dadtreasury.data.DadTreasuryRepository) {
    val tokens = LocalSemanticTokens.current
    val clipboard = LocalClipboardManager.current

    var step by rememberSaveable { mutableStateOf(WizardStep.STEP_ENERGY) }
    var energy by rememberSaveable { mutableStateOf<EnergyLevel?>(null) }
    var motivation by rememberSaveable { mutableStateOf<Motivation?>(null) }
    var role by rememberSaveable { mutableStateOf<RoleComfort?>(null) }
    var cleanup by rememberSaveable { mutableStateOf<CleanupChoice?>(null) }
    var timeLimit by rememberSaveable { mutableStateOf<TimeLimit?>(null) }
    var copiedPublic by remember { mutableStateOf(false) }
    var copiedHome by remember { mutableStateOf(false) }

    val (publicText, homeText) = buildOutputTexts(energy, motivation, role, cleanup, timeLimit)

    fun reset() {
        step = WizardStep.STEP_ENERGY
        energy = null; motivation = null; role = null; cleanup = null; timeLimit = null
        copiedPublic = false; copiedHome = false
    }

    val progress = when (step) {
        WizardStep.STEP_ENERGY -> 0f
        WizardStep.STEP_MOTIVATION -> 0.33f
        WizardStep.STEP_ROLE -> 0.66f
        WizardStep.STEP_PRACTICAL -> 1f
        WizardStep.END -> 1f
    }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            TopAppBar(
                title = { Text("My Boundaries & Choices", color = tokens.textPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface),
                actions = {
                    TextButton(onClick = { reset() }) { Text("Reset", color = tokens.accentPrimary) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (step != WizardStep.END) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = tokens.accentPrimary, trackColor = tokens.surface,
                )
                Spacer(Modifier.height(12.dp))
                Text(stepLabel(step), style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                Spacer(Modifier.height(16.dp))
            }
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "wizard",
                modifier = Modifier.weight(1f),
            ) { current ->
                when (current) {
                    WizardStep.STEP_ENERGY -> EnergyStep { level ->
                        energy = level
                        step = if (level == EnergyLevel.EMPTY) WizardStep.END else WizardStep.STEP_MOTIVATION
                    }
                    WizardStep.STEP_MOTIVATION -> MotivationStep { choice ->
                        motivation = choice
                        step = if (choice == Motivation.PRESSURED_FORCED) WizardStep.END else WizardStep.STEP_ROLE
                    }
                    WizardStep.STEP_ROLE -> RoleStep { r -> role = r; step = WizardStep.STEP_PRACTICAL }
                    WizardStep.STEP_PRACTICAL -> PracticalStep(
                        cleanup, timeLimit,
                        { cleanup = it }, { timeLimit = it },
                        { step = WizardStep.END },
                    )
                    WizardStep.END -> EndScreen(
                        publicText, homeText, energy, motivation,
                        copiedPublic, copiedHome,
                        {
                            clipboard.setText(AnnotatedString(publicText)); copiedPublic = true; copiedHome = false
                        },
                        {
                            clipboard.setText(AnnotatedString(homeText)); copiedHome = true; copiedPublic = false
                        },
                        { reset() }, repository,
                    )
                }
            }
        }
    }
}

private fun stepLabel(s: WizardStep): String = when (s) {
    WizardStep.STEP_ENERGY -> "Step 1 of 4 · Energy"
    WizardStep.STEP_MOTIVATION -> "Step 2 of 4 · Motivation"
    WizardStep.STEP_ROLE -> "Step 3 of 4 · Role"
    WizardStep.STEP_PRACTICAL -> "Step 4 of 4 · Boundaries"
    WizardStep.END -> "Done"
}

@Composable
private fun EnergyStep(onSelect: (EnergyLevel) -> Unit) {
    val tokens = LocalSemanticTokens.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("How is your battery right now?", style = MaterialTheme.typography.headlineSmall, color = tokens.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("One tap. No wrong answers.", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
        Spacer(Modifier.height(32.dp))
        BigChoiceButton("🟢", "Full", "I have energy", { onSelect(EnergyLevel.FULL) })
        Spacer(Modifier.height(12.dp))
        BigChoiceButton("🟡", "Low", "I'm running out", { onSelect(EnergyLevel.LOW) })
        Spacer(Modifier.height(12.dp))
        BigChoiceButton("🔴", "Empty", "I have nothing left", { onSelect(EnergyLevel.EMPTY) })
    }
}

@Composable
private fun MotivationStep(onSelect: (Motivation) -> Unit) {
    val tokens = LocalSemanticTokens.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Why are you joining right now?", style = MaterialTheme.typography.headlineSmall, color = tokens.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Be honest - it helps us figure out the best plan.", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
        Spacer(Modifier.height(32.dp))
        BigChoiceButton("💚", "I want to", "My own choice", { onSelect(Motivation.I_WANT_TO) })
        Spacer(Modifier.height(12.dp))
        BigChoiceButton("🤝", "To make friends happy", "I'm doing it for them", { onSelect(Motivation.TO_MAKE_FRIENDS_HAPPY) })
        Spacer(Modifier.height(12.dp))
        BigChoiceButton("😰", "I feel pressured / forced", "I don't really want to", { onSelect(Motivation.PRESSURED_FORCED) })
    }
}

@Composable
private fun RoleStep(onSelect: (RoleComfort) -> Unit) {
    val tokens = LocalSemanticTokens.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("How do you want to take part?", style = MaterialTheme.typography.headlineSmall, color = tokens.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Both options are 100% valid ways to be together.", style = MaterialTheme.typography.bodyMedium, color = tokens.textSecondary)
        Spacer(Modifier.height(32.dp))
        BigChoiceButton("🎮", "Active Player", "I'm in, let's play", { onSelect(RoleComfort.ACTIVE_PLAYER) })
        Spacer(Modifier.height(12.dp))
        BigChoiceButton("👀", "Quiet Helper / Watcher", "I'll help or just watch", { onSelect(RoleComfort.QUIET_HELPER_WATCHER) })
    }
}

@Composable
private fun PracticalStep(
    selectedCleanup: CleanupChoice?,
    selectedTime: TimeLimit?,
    onCleanup: (CleanupChoice) -> Unit,
    onTime: (TimeLimit) -> Unit,
    onComplete: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    val canFinish = selectedCleanup != null && selectedTime != null
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Two quick agreements, then you're done.", style = MaterialTheme.typography.headlineSmall, color = tokens.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text("Who cleans up?", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        MiniChoiceChips(listOf(CleanupChoice.SPLIT to "We split it", CleanupChoice.SOMEONE_ELSE to "Someone else cleans"), selectedCleanup, onCleanup)
        Spacer(Modifier.height(24.dp))
        Text("Time limit?", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        MiniChoiceChips(listOf(TimeLimit.FIFTEEN_MIN to "15 minutes", TimeLimit.SEE_HOW_IT_GOES to "See how it goes"), selectedTime, onTime)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onComplete, enabled = canFinish, modifier = Modifier.fillMaxWidth()) { Text("Finish & See My Plan") }
    }
}

@Composable
private fun <T> MiniChoiceChips(options: List<Pair<T, String>>, selected: T?, onSelect: (T) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (v, label) ->
            FilterChip(selected = selected == v, onClick = { onSelect(v) }, label = { Text(label) })
        }
    }
}

/** Root composables end here - step/end implementations added in follow-up block. */

@Composable
private fun EndScreen(
    publicText: String,
    homeText: String,
    energy: EnergyLevel?,
    motivation: Motivation?,
    copiedPublic: Boolean,
    copiedHome: Boolean,
    onCopyPublic: () -> Unit,
    onCopyHome: () -> Unit,
    onRestart: () -> Unit,
    repository: com.stokstylez.dadtreasury.data.DadTreasuryRepository,
) {
    val tokens = LocalSemanticTokens.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (energy == EnergyLevel.EMPTY) {
            Surface(
                color = tokens.warning.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Your battery is out. You have full permission to walk away and recharge. Let's find your safe space.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.textPrimary,
                )
            }
        }
        if (motivation == Motivation.PRESSURED_FORCED) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = tokens.accentPrimary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "You never have to force yourself to fit in at the cost of your comfort. Your true friends will understand.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.textPrimary,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Public Text (to friends / adults)", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(publicText, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCopyPublic, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (copiedPublic) "Copied ✓" else "Copy & Show")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Home Team Action (to Dad)", style = MaterialTheme.typography.titleMedium, color = tokens.textPrimary)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = tokens.card), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(homeText, style = MaterialTheme.typography.bodyLarge, color = tokens.textPrimary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onCopyHome, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (copiedHome) "Copied ✓" else "Copy")
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                repository.sendMessage(
                                    threadId = "parent-child",
                                    senderRole = com.stokstylez.dadtreasury.domain.model.Role.CHILD,
                                    text = homeText,
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send to Dad")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onRestart, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Start Over", color = tokens.accentPrimary)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BigChoiceButton(
    emoji: String,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val tokens = LocalSemanticTokens.current
    Card(
        colors = CardDefaults.cardColors(containerColor = tokens.card),
        modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = tokens.textPrimary)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = tokens.textSecondary)
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = tokens.accentPrimary)
        }
    }
}

/** Builds the public-facing text + home-team message from the selections. */
private fun buildOutputTexts(
    energy: EnergyLevel?,
    motivation: Motivation?,
    role: RoleComfort?,
    cleanup: CleanupChoice?,
    timeLimit: TimeLimit?,
): Pair<String, String> {
    if (energy == EnergyLevel.EMPTY) {
        return Pair(
            "I'm going to sit this one out today and recharge. I'll catch up with you later!",
            "Dad, my battery is empty. Can you help me make an excuse to leave politely and find a quiet spot?",
        )
    }
    if (motivation == Motivation.PRESSURED_FORCED) {
        return Pair(
            "I'm going to pass this time. I need a break and I'd rather not explain everything right now.",
            "Dad, I feel pressured/forced to join but I don't want to. Can you back me up with an excuse to leave?",
        )
    }

    val lines = mutableListOf<String>()
    when (energy) {
        EnergyLevel.FULL -> lines.add("I have energy and I'm ready to hang out.")
        EnergyLevel.LOW -> lines.add("I can join, but I'll need breaks. My battery is getting low.")
        EnergyLevel.EMPTY -> {}
        null -> {}
    }
    if (motivation == Motivation.TO_MAKE_FRIENDS_HAPPY) {
        lines.add("I'm here for you - I just might need a little more space than usual.")
    }
    when (role) {
        RoleComfort.ACTIVE_PLAYER -> lines.add("I'd like to be an active player.")
        RoleComfort.QUIET_HELPER_WATCHER -> lines.add("I'm going to sit this one out and watch/help from here!")
        null -> {}
    }
    when (cleanup) {
        CleanupChoice.SPLIT -> lines.add("Let's split the cleanup.")
        CleanupChoice.SOMEONE_ELSE -> lines.add("Someone else handles cleanup this time.")
        null -> {}
    }
    when (timeLimit) {
        TimeLimit.FIFTEEN_MIN -> lines.add("I can stay for 15 minutes, but then I have to go.")
        TimeLimit.SEE_HOW_IT_GOES -> lines.add("Let's see how it goes - I'll check in with myself.")
        null -> {}
    }
    if (lines.isEmpty()) lines.add("I'm here - and I'll let you know what I need.")

    val homeBits = mutableListOf<String>()
    if (role == RoleComfort.QUIET_HELPER_WATCHER) homeBits.add("I'd rather watch/help than play.")
    if (cleanup == CleanupChoice.SOMEONE_ELSE) homeBits.add("Can cleanup be someone else's turn?")
    if (timeLimit == TimeLimit.FIFTEEN_MIN) homeBits.add("Please help me stick to 15 minutes.")
    if (energy == EnergyLevel.LOW) homeBits.add("My battery is low - keep an eye on me.")
    val homeText = if (homeBits.isEmpty()) {
        "Dad, I've set my boundaries and I'm good to go. 😊"
    } else {
        "Dad, just so you know: ${homeBits.joinToString(" ")} Help me hold these boundaries, please."
    }

    return Pair(lines.joinToString("\n"), homeText)
}
