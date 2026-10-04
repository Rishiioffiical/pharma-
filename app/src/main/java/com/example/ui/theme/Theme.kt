package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

val LocalGlowColor = staticCompositionLocalOf { Color(0x3300E5A3) }
val LocalSurfaceElevated = staticCompositionLocalOf { Color(0xFF172640) }
val LocalCurrentThemePreset = staticCompositionLocalOf { ThemePreset.MIDNIGHT }

@Composable
fun PharmaHubTheme(
    themeConfig: PharmaThemeConfig = PharmaThemeConfig(),
    content: @Composable () -> Unit
) {
    val preset = themeConfig.preset
    val isDark = themeConfig.isDark
    val primaryColor = themeConfig.customAccentColor ?: preset.primary

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = preset.secondary,
            onSecondary = Color.Black,
            tertiary = preset.accent,
            onTertiary = Color.Black,
            background = preset.darkBackground,
            onBackground = Color(0xFFF1F5F9),
            surface = preset.darkSurface,
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = preset.darkSurfaceElevated,
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = preset.darkSurfaceElevated.copy(alpha = 0.8f)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = preset.secondary,
            onSecondary = Color.White,
            tertiary = preset.accent,
            onTertiary = Color.White,
            background = preset.lightBackground,
            onBackground = Color(0xFF0F172A),
            surface = preset.lightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = preset.lightSurfaceElevated,
            onSurfaceVariant = Color(0xFF64748B),
            outline = Color(0xFFCBD5E1)
        )
    }

    val surfaceElevated = if (isDark) preset.darkSurfaceElevated else preset.lightSurfaceElevated
    val glowColor = primaryColor.copy(alpha = 0.25f)

    CompositionLocalProvider(
        LocalGlowColor provides glowColor,
        LocalSurfaceElevated provides surfaceElevated,
        LocalCurrentThemePreset provides preset
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
