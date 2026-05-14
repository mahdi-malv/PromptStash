package com.mahdimalv.prompstash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mahdimalv.prompstash.data.settings.ThemePreference

private val PromptStashLightColorScheme = lightColorScheme(
    primary = PaperAccent,
    onPrimary = PaperOnAccent,
    primaryContainer = PaperAccentContainer,
    onPrimaryContainer = PaperOnAccentContainer,
    secondary = PaperAccent,
    onSecondary = PaperOnAccent,
    secondaryContainer = PaperAccentContainer,
    onSecondaryContainer = PaperOnAccentContainer,
    tertiary = PaperAccent,
    onTertiary = PaperOnAccent,
    tertiaryContainer = PaperAccentContainer,
    onTertiaryContainer = PaperOnAccentContainer,
    error = PaperDanger,
    onError = PaperOnDanger,
    errorContainer = PaperDangerContainer,
    onErrorContainer = PaperOnDangerContainer,
    background = PaperBackground,
    onBackground = PaperOnSurface,
    surface = PaperBackground,
    onSurface = PaperOnSurface,
    onSurfaceVariant = PaperOnSurfaceMuted,
    surfaceVariant = PaperSurfaceVariant,
    surfaceContainerLowest = PaperBackground,
    surfaceContainerLow = PaperSurfaceContainer,
    surfaceContainer = PaperSurfaceContainer,
    surfaceContainerHigh = PaperSurfaceContainerHigh,
    surfaceContainerHighest = PaperSurfaceContainerHigh,
    outline = PaperOutline,
    outlineVariant = PaperOutlineMuted,
    surfaceTint = PaperAccent,
)

private val PromptStashDarkColorScheme = darkColorScheme(
    primary = InkAccent,
    onPrimary = InkOnAccent,
    primaryContainer = InkAccentContainer,
    onPrimaryContainer = InkOnAccentContainer,
    secondary = InkAccent,
    onSecondary = InkOnAccent,
    secondaryContainer = InkAccentContainer,
    onSecondaryContainer = InkOnAccentContainer,
    tertiary = InkAccent,
    onTertiary = InkOnAccent,
    tertiaryContainer = InkAccentContainer,
    onTertiaryContainer = InkOnAccentContainer,
    error = InkDanger,
    onError = InkOnDanger,
    errorContainer = InkDangerContainer,
    onErrorContainer = InkOnDangerContainer,
    background = InkBackground,
    onBackground = InkOnSurface,
    surface = InkBackground,
    onSurface = InkOnSurface,
    onSurfaceVariant = InkOnSurfaceMuted,
    surfaceVariant = InkSurfaceVariant,
    surfaceContainerLowest = InkBackground,
    surfaceContainerLow = InkSurfaceContainer,
    surfaceContainer = InkSurfaceContainer,
    surfaceContainerHigh = InkSurfaceContainerHigh,
    surfaceContainerHighest = InkSurfaceContainerHigh,
    outline = InkOutline,
    outlineVariant = InkOutlineMuted,
    surfaceTint = InkAccent,
)

@Composable
fun PrompStashTheme(
    themePreference: ThemePreference,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) PromptStashDarkColorScheme else PromptStashLightColorScheme,
        typography = AppTypography(),
        shapes = AppShapes,
        content = content,
    )
}
