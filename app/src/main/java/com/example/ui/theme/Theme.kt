package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueLight,
    onPrimary = DeepIndigoBackground,
    primaryContainer = WeatherGradientStart,
    onPrimaryContainer = SkyBlueContainer,
    secondary = IndigoAccent,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = DeepIndigoBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurfaceCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = RiskSevereRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SkyBlueContainer,
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = IndigoAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEF2FF),
    onSecondaryContainer = Color(0xFF312E81),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF475569),
    error = RiskSevereRed,
    onError = Color.White
)

@Composable
fun MausamTheme(
    darkTheme: Boolean = true, // Default to deep sky-blue weather tech dark theme for stunning visuals
    dynamicColor: Boolean = false, // Keep MAUSAM's signature blue-indigo-purple palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
