package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ThemePreference {
    DARK,
    LIGHT,
    SYSTEM
}

private val DlsDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = StadiumNavyDeep,
    primaryContainer = Color(0xFF003B4A),
    onPrimaryContainer = Color(0xFFB3F7FF),
    secondary = PitchGreen,
    onSecondary = StadiumNavyDeep,
    secondaryContainer = Color(0xFF003D21),
    onSecondaryContainer = Color(0xFFB9FCD4),
    tertiary = ChampionshipGold,
    onTertiary = StadiumNavyDeep,
    tertiaryContainer = Color(0xFF423200),
    onTertiaryContainer = Color(0xFFFFE58F),
    background = StadiumNavyDeep,
    onBackground = TextPrimaryDark,
    surface = StadiumNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StadiumSlateCard,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = StadiumNavyDeep,
    surfaceContainerLow = StadiumNavySurface,
    surfaceContainer = StadiumSlateCard,
    surfaceContainerHigh = StadiumSlateElevated,
    outline = StadiumBorderSubtle,
    outlineVariant = Color(0xFF1F2D49),
    error = CrimsonAlert,
    onError = Color.White
)

private val DlsLightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E6FF),
    onPrimaryContainer = Color(0xFF001A43),
    secondary = TurfGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8F7DD),
    onSecondaryContainer = Color(0xFF00210F),
    tertiary = Color(0xFFB37B00),
    onTertiary = Color.White,
    background = DaylightBg,
    onBackground = TextPrimaryLight,
    surface = DaylightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DaylightCard,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLowest = DaylightSurface,
    surfaceContainerLow = DaylightBg,
    surfaceContainer = DaylightCard,
    surfaceContainerHigh = Color(0xFFDCE5F2),
    outline = DaylightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = CrimsonAlert,
    onError = Color.White
)

val DlsShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DlsKitStudioTheme(
    themePreference: ThemePreference = ThemePreference.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themePreference) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) DlsDarkColorScheme else DlsLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DlsTypography,
        shapes = DlsShapes,
        content = content
    )
}
