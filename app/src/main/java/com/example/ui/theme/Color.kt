package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Neutral Tones
val Neutral900 = Color(0xFF070D18)
val Neutral850 = Color(0xFF0C1424)
val Neutral800 = Color(0xFF111C30)
val Neutral700 = Color(0xFF1B2A44)
val Neutral600 = Color(0xFF2C3E5E)
val Neutral400 = Color(0xFF7E92B2)
val Neutral200 = Color(0xFFCBD5E1)
val Neutral100 = Color(0xFFF1F5F9)
val Neutral50 = Color(0xFFF8FAFC)

enum class ThemePreset(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkSurfaceElevated: Color,
    val lightBackground: Color,
    val lightSurface: Color,
    val lightSurfaceElevated: Color,
    val glowColor: Color
) {
    MIDNIGHT(
        title = "Midnight",
        primary = Color(0xFF00E5A3),
        secondary = Color(0xFF00B4D8),
        accent = Color(0xFF38BDF8),
        darkBackground = Color(0xFF070E1A),
        darkSurface = Color(0xFF0F1A2E),
        darkSurfaceElevated = Color(0xFF172640),
        lightBackground = Color(0xFFF1F5F9),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFE2E8F0),
        glowColor = Color(0x3300E5A3)
    ),
    OCEAN(
        title = "Ocean",
        primary = Color(0xFF0284C7),
        secondary = Color(0xFF06B6D4),
        accent = Color(0xFF38BDF8),
        darkBackground = Color(0xFF051329),
        darkSurface = Color(0xFF0B2144),
        darkSurfaceElevated = Color(0xFF133261),
        lightBackground = Color(0xFFF0F9FF),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFE0F2FE),
        glowColor = Color(0x330284C7)
    ),
    EMERALD(
        title = "Emerald",
        primary = Color(0xFF10B981),
        secondary = Color(0xFF34D399),
        accent = Color(0xFF6EE7B7),
        darkBackground = Color(0xFF061A14),
        darkSurface = Color(0xFF0D2A22),
        darkSurfaceElevated = Color(0xFF133C31),
        lightBackground = Color(0xFFECFDF5),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFD1FAE5),
        glowColor = Color(0x3310B981)
    ),
    VIOLET(
        title = "Violet",
        primary = Color(0xFF8B5CF6),
        secondary = Color(0xFFA78BFA),
        accent = Color(0xFFC4B5FD),
        darkBackground = Color(0xFF110C24),
        darkSurface = Color(0xFF1D163D),
        darkSurfaceElevated = Color(0xFF2B2057),
        lightBackground = Color(0xFFF5F3FF),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFEDE9FE),
        glowColor = Color(0x338B5CF6)
    ),
    CRIMSON(
        title = "Crimson",
        primary = Color(0xFFF43F5E),
        secondary = Color(0xFFFB7185),
        accent = Color(0xFFFDA4AF),
        darkBackground = Color(0xFF1A0A10),
        darkSurface = Color(0xFF2E121E),
        darkSurfaceElevated = Color(0xFF421C2C),
        lightBackground = Color(0xFFFFF1F2),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFFFE4E6),
        glowColor = Color(0x33F43F5E)
    ),
    AMBER(
        title = "Amber",
        primary = Color(0xFFF59E0B),
        secondary = Color(0xFFFBBF24),
        accent = Color(0xFFFDE68A),
        darkBackground = Color(0xFF1A1305),
        darkSurface = Color(0xFF2C200B),
        darkSurfaceElevated = Color(0xFF413013),
        lightBackground = Color(0xFFFFFBEB),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFFEF3C7),
        glowColor = Color(0x33F59E0B)
    ),
    CYBER(
        title = "Cyber",
        primary = Color(0xFF00FFCC),
        secondary = Color(0xFFFF007F),
        accent = Color(0xFF00E5FF),
        darkBackground = Color(0xFF030712),
        darkSurface = Color(0xFF0B132B),
        darkSurfaceElevated = Color(0xFF1C2541),
        lightBackground = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFE2E8F0),
        glowColor = Color(0x4000FFCC)
    ),
    ARCTIC(
        title = "Arctic",
        primary = Color(0xFF38BDF8),
        secondary = Color(0xFF7DD3FC),
        accent = Color(0xFFBAE6FD),
        darkBackground = Color(0xFF081421),
        darkSurface = Color(0xFF0F263D),
        darkSurfaceElevated = Color(0xFF183B5E),
        lightBackground = Color(0xFFF0F9FF),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFE0F2FE),
        glowColor = Color(0x3338BDF8)
    ),
    ROSE(
        title = "Rose",
        primary = Color(0xFFFB7185),
        secondary = Color(0xFFF472B6),
        accent = Color(0xFFFBCFE8),
        darkBackground = Color(0xFF1A0C16),
        darkSurface = Color(0xFF2B1626),
        darkSurfaceElevated = Color(0xFF3E2037),
        lightBackground = Color(0xFFFDF2F8),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFFCE7F3),
        glowColor = Color(0x33FB7185)
    ),
    GRAPHITE(
        title = "Graphite",
        primary = Color(0xFF94A3B8),
        secondary = Color(0xFFCBD5E1),
        accent = Color(0xFFE2E8F0),
        darkBackground = Color(0xFF0F172A),
        darkSurface = Color(0xFF1E293B),
        darkSurfaceElevated = Color(0xFF334155),
        lightBackground = Color(0xFFF8FAFC),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFE2E8F0),
        glowColor = Color(0x3394A3B8)
    ),
    PHARMACY_GREEN(
        title = "Pharmacy Green",
        primary = Color(0xFF059669),
        secondary = Color(0xFF10B981),
        accent = Color(0xFF34D399),
        darkBackground = Color(0xFF041913),
        darkSurface = Color(0xFF0A2B21),
        darkSurfaceElevated = Color(0xFF113D30),
        lightBackground = Color(0xFFF0FDF4),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFDCFCE7),
        glowColor = Color(0x33059669)
    ),
    NEON_DARK(
        title = "Neon Dark",
        primary = Color(0xFF39FF14),
        secondary = Color(0xFF00FFA3),
        accent = Color(0xFF70FF00),
        darkBackground = Color(0xFF020603),
        darkSurface = Color(0xFF08150B),
        darkSurfaceElevated = Color(0xFF102615),
        lightBackground = Color(0xFFF4FBF5),
        lightSurface = Color(0xFFFFFFFF),
        lightSurfaceElevated = Color(0xFFDCFCE7),
        glowColor = Color(0x4D39FF14)
    )
}

data class PharmaThemeConfig(
    val preset: ThemePreset = ThemePreset.MIDNIGHT,
    val isDark: Boolean = true,
    val customAccentColor: Color? = null
)

fun getGradientBrush(preset: ThemePreset, isDark: Boolean): Brush {
    val start = preset.primary
    val end = preset.secondary
    return Brush.linearGradient(colors = listOf(start, end))
}
