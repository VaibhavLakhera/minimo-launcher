package com.minimo.launcher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

internal data class ThemePreset(
    val name: String,
    val background: Color,
    val foreground: Color,
    val accent: Color,
    val dark: Boolean
) {
    val colorScheme: ColorScheme = presetColorScheme(background, foreground, accent, dark)
}

private fun presetColorScheme(
    background: Color,
    foreground: Color,
    accent: Color,
    dark: Boolean
): ColorScheme {
    fun surface(tint: Float) = foreground.copy(alpha = tint).compositeOver(background)

    // Keep elevation tints subtle so saturated surfaces retain strong text contrast.
    val secondaryText = surface(0.85f).takeIf {
        contrastRatio(it, surface(0.06f)) >= 4.5f
    } ?: foreground
    val accentContainer = accent.copy(alpha = 0.12f).compositeOver(background)
    // Accent brightness varies independently of whether the overall preset is dark.
    val onAccent = if (contrastRatio(Color.Black, accent) >= contrastRatio(Color.White, accent)) {
        Color.Black
    } else {
        Color.White
    }
    val lightTone = if (dark) foreground else background
    val darkTone = if (dark) background else foreground
    val fixedDim = darkTone.copy(alpha = 0.06f).compositeOver(lightTone)
    val base = if (dark) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentContainer,
        onPrimaryContainer = foreground,
        inversePrimary = background,
        secondary = secondaryText,
        onSecondary = background,
        secondaryContainer = surface(0.08f),
        onSecondaryContainer = foreground,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = accentContainer,
        onTertiaryContainer = foreground,
        background = background,
        onBackground = foreground,
        surface = background,
        onSurface = foreground,
        surfaceVariant = surface(0.06f),
        onSurfaceVariant = secondaryText,
        surfaceTint = accent,
        inverseSurface = foreground,
        inverseOnSurface = background,
        error = if (dark) Color(0xFFFFC5C5) else Color(0xFF8C1537),
        onError = if (dark) Color(0xFF4B0018) else Color.White,
        errorContainer = if (dark) Color(0xFF651A2D) else Color(0xFFFFD9DF),
        onErrorContainer = if (dark) Color(0xFFFFD9DF) else Color(0xFF51051C),
        outline = surface(0.60f),
        outlineVariant = surface(0.25f),
        surfaceBright = if (dark) surface(0.06f) else background,
        surfaceDim = if (dark) background else surface(0.06f),
        surfaceContainerLowest = background,
        surfaceContainerLow = surface(0.015f),
        surfaceContainer = surface(0.03f),
        surfaceContainerHigh = surface(0.045f),
        surfaceContainerHighest = surface(0.06f),
        primaryFixed = lightTone,
        primaryFixedDim = fixedDim,
        onPrimaryFixed = darkTone,
        onPrimaryFixedVariant = darkTone,
        secondaryFixed = lightTone,
        secondaryFixedDim = fixedDim,
        onSecondaryFixed = darkTone,
        onSecondaryFixedVariant = darkTone,
        tertiaryFixed = lightTone,
        tertiaryFixedDim = fixedDim,
        onTertiaryFixed = darkTone,
        onTertiaryFixedVariant = darkTone
    )
}

private fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    return (maxOf(firstLuminance, secondLuminance) + 0.05f) /
            (minOf(firstLuminance, secondLuminance) + 0.05f)
}
