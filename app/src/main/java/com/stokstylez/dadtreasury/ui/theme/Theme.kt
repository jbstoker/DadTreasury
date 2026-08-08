package com.stokstylez.dadtreasury.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.sp
import com.stokstylez.dadtreasury.domain.model.Language

/**
 * App settings - held at composition root so screens can react.
 */
class AppSettingsState {
    var role by mutableStateOf<String?>(null)
    var calmMode by mutableStateOf(false)
    var reducedMotion by mutableStateOf(false)
    var highContrast by mutableStateOf(false)
    var textScale by mutableStateOf(1.0f)
    var theme by mutableStateOf(ThemeChoice.RETRO_FUTURIST)
    var language by mutableStateOf(Language.SYSTEM_DEFAULT)
}

enum class ThemeChoice {
    RETRO_FUTURIST,
    HIGH_CONTRAST,
    CALM,
    NATURE
}

/**
 * Semantic tokens - screens must never interact with raw colors directly.
 */
data class SemanticTokens(
    val background: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val card: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val accentPrimary: androidx.compose.ui.graphics.Color,
    val accentSecondary: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val error: androidx.compose.ui.graphics.Color,
    val naturalCalendar: androidx.compose.ui.graphics.Color,
)

val RetroFuturistTokens = SemanticTokens(
    background = GraphiteDark,
    surface = Graphite,
    card = SurfaceLight,
    border = BorderLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    accentPrimary = NeonCyan,
    accentSecondary = NeonPink,
    success = NeonGreen,
    warning = NeonAmber,
    error = NeonRed,
    naturalCalendar = NeonGreen,
)

val HighContrastTokens = SemanticTokens(
    background = androidx.compose.ui.graphics.Color(0xFF000000),
    surface = androidx.compose.ui.graphics.Color(0xFF0A0A0A),
    card = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
    border = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    textPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    textSecondary = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    accentPrimary = androidx.compose.ui.graphics.Color(0xFFFFFF00),
    accentSecondary = androidx.compose.ui.graphics.Color(0xFF00FFFF),
    success = androidx.compose.ui.graphics.Color(0xFF00FF00),
    warning = androidx.compose.ui.graphics.Color(0xFFFFFF00),
    error = androidx.compose.ui.graphics.Color(0xFFFF0000),
    naturalCalendar = androidx.compose.ui.graphics.Color(0xFF00FF00),
)

val CalmTokens = SemanticTokens(
    background = androidx.compose.ui.graphics.Color(0xFFF4F1EA),
    surface = androidx.compose.ui.graphics.Color(0xFFEDE8DF),
    card = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    border = androidx.compose.ui.graphics.Color(0xFFC8C4BA),
    textPrimary = androidx.compose.ui.graphics.Color(0xFF2C2C2C),
    textSecondary = androidx.compose.ui.graphics.Color(0xFF6B6B6B),
    accentPrimary = androidx.compose.ui.graphics.Color(0xFF7A9B76),
    accentSecondary = androidx.compose.ui.graphics.Color(0xFF8B7D9B),
    success = androidx.compose.ui.graphics.Color(0xFF5C8A5C),
    warning = androidx.compose.ui.graphics.Color(0xFFB58A3E),
    error = androidx.compose.ui.graphics.Color(0xFFB34A4A),
    naturalCalendar = androidx.compose.ui.graphics.Color(0xFF5C8A5C),
)

val NatureTokens = SemanticTokens(
    background = androidx.compose.ui.graphics.Color(0xFF10241A),
    surface = androidx.compose.ui.graphics.Color(0xFF1A3326),
    card = androidx.compose.ui.graphics.Color(0xFF24402F),
    border = androidx.compose.ui.graphics.Color(0xFF3D6B4F),
    textPrimary = androidx.compose.ui.graphics.Color(0xFFE8F0E8),
    textSecondary = androidx.compose.ui.graphics.Color(0xFFA8C8B0),
    accentPrimary = androidx.compose.ui.graphics.Color(0xFF66CC88),
    accentSecondary = androidx.compose.ui.graphics.Color(0xFF88AACC),
    success = androidx.compose.ui.graphics.Color(0xFF88DD66),
    warning = androidx.compose.ui.graphics.Color(0xFFEECC66),
    error = androidx.compose.ui.graphics.Color(0xFFEE6666),
    naturalCalendar = androidx.compose.ui.graphics.Color(0xFF66CC88),
)

fun tokensFor(theme: ThemeChoice): SemanticTokens = when (theme) {
    ThemeChoice.RETRO_FUTURIST -> RetroFuturistTokens
    ThemeChoice.HIGH_CONTRAST -> HighContrastTokens
    ThemeChoice.CALM -> CalmTokens
    ThemeChoice.NATURE -> NatureTokens
}

val LocalSemanticTokens = staticCompositionLocalOf { RetroFuturistTokens }
val LocalAppSettings = staticCompositionLocalOf { AppSettingsState() }

@Composable
fun DadTreasuryTheme(
    content: @Composable () -> Unit,
) {
    val settings = LocalAppSettings.current
    val tokens = tokensFor(settings.theme)

    // The app's visual identity is dark retro-futurist. The Calm theme is light.
    // We build the Material3 color scheme from our semantic tokens so text fields,
    // cards, and dialogs always use readable colors — regardless of system dark mode.
    val colorScheme = if (settings.theme == ThemeChoice.CALM) {
        lightColorScheme(
            primary = tokens.accentPrimary,
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = tokens.accentSecondary,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.card,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.border,
            error = tokens.error,
            onError = androidx.compose.ui.graphics.Color.White,
        )
    } else {
        darkColorScheme(
            primary = tokens.accentPrimary,
            onPrimary = tokens.background,
            secondary = tokens.accentSecondary,
            onSecondary = tokens.background,
            background = tokens.background,
            onBackground = tokens.textPrimary,
            surface = tokens.surface,
            onSurface = tokens.textPrimary,
            surfaceVariant = tokens.card,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.border,
            error = tokens.error,
            onError = tokens.background,
        )
    }

    CompositionLocalProvider(LocalSemanticTokens provides tokens) {
        // Apply user-selected text scale to the default typography.
        val scale = settings.textScale.coerceIn(0.8f, 1.5f)
        val base = MaterialTheme.typography
        val scaledTypography = base.copy(
            displayLarge = base.displayLarge.copy(fontSize = (base.displayLarge.fontSize.value * scale).sp),
            displayMedium = base.displayMedium.copy(fontSize = (base.displayMedium.fontSize.value * scale).sp),
            displaySmall = base.displaySmall.copy(fontSize = (base.displaySmall.fontSize.value * scale).sp),
            headlineLarge = base.headlineLarge.copy(fontSize = (base.headlineLarge.fontSize.value * scale).sp),
            headlineMedium = base.headlineMedium.copy(fontSize = (base.headlineMedium.fontSize.value * scale).sp),
            headlineSmall = base.headlineSmall.copy(fontSize = (base.headlineSmall.fontSize.value * scale).sp),
            titleLarge = base.titleLarge.copy(fontSize = (base.titleLarge.fontSize.value * scale).sp),
            titleMedium = base.titleMedium.copy(fontSize = (base.titleMedium.fontSize.value * scale).sp),
            titleSmall = base.titleSmall.copy(fontSize = (base.titleSmall.fontSize.value * scale).sp),
            bodyLarge = base.bodyLarge.copy(fontSize = (base.bodyLarge.fontSize.value * scale).sp),
            bodyMedium = base.bodyMedium.copy(fontSize = (base.bodyMedium.fontSize.value * scale).sp),
            bodySmall = base.bodySmall.copy(fontSize = (base.bodySmall.fontSize.value * scale).sp),
            labelLarge = base.labelLarge.copy(fontSize = (base.labelLarge.fontSize.value * scale).sp),
            labelMedium = base.labelMedium.copy(fontSize = (base.labelMedium.fontSize.value * scale).sp),
            labelSmall = base.labelSmall.copy(fontSize = (base.labelSmall.fontSize.value * scale).sp),
        )
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}
