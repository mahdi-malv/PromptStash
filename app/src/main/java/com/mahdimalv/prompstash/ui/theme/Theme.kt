package com.mahdimalv.prompstash.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mahdimalv.prompstash.data.settings.ThemePreference

private val PromptStashLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceVariant = SurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    surfaceTint = SurfaceTint,
)

private val PromptStashDarkColorScheme = darkColorScheme(
    primary = ColorPrimaryDark,
    onPrimary = ColorOnPrimaryDark,
    primaryContainer = ColorPrimaryContainerDark,
    onPrimaryContainer = ColorOnPrimaryContainerDark,
    secondary = ColorSecondaryDark,
    onSecondary = ColorOnSecondaryDark,
    secondaryContainer = ColorSecondaryContainerDark,
    onSecondaryContainer = ColorOnSecondaryContainerDark,
    tertiary = ColorTertiaryDark,
    onTertiary = ColorOnTertiaryDark,
    tertiaryContainer = ColorTertiaryContainerDark,
    onTertiaryContainer = ColorOnTertiaryContainerDark,
    error = ColorErrorDark,
    onError = ColorOnErrorDark,
    errorContainer = ColorErrorContainerDark,
    onErrorContainer = ColorOnErrorContainerDark,
    background = ColorBackgroundDark,
    onBackground = ColorOnBackgroundDark,
    surface = ColorSurfaceDark,
    onSurface = ColorOnSurfaceDark,
    onSurfaceVariant = ColorOnSurfaceVariantDark,
    surfaceVariant = ColorSurfaceVariantDark,
    surfaceContainerLowest = ColorSurfaceContainerLowestDark,
    surfaceContainerLow = ColorSurfaceContainerLowDark,
    surfaceContainer = ColorSurfaceContainerDark,
    surfaceContainerHigh = ColorSurfaceContainerHighDark,
    surfaceContainerHighest = ColorSurfaceContainerHighestDark,
    outline = ColorOutlineDark,
    outlineVariant = ColorOutlineVariantDark,
    inverseSurface = ColorInverseSurfaceDark,
    inverseOnSurface = ColorInverseOnSurfaceDark,
    inversePrimary = ColorInversePrimaryDark,
    surfaceTint = ColorSurfaceTintDark,
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
