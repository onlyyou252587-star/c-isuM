package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MidnightColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = MidnightBlack,
  primaryContainer = MidnightElevated,
  onPrimaryContainer = NeonCyan,
  secondary = NeonViolet,
  onSecondary = MidnightBlack,
  secondaryContainer = MidnightCardSurface,
  onSecondaryContainer = NeonViolet,
  tertiary = NeonPink,
  onTertiary = Color.White,
  background = MidnightBlack,
  onBackground = TextPrimary,
  surface = MidnightDarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = MidnightCardSurface,
  onSurfaceVariant = TextSecondary,
  outline = MidnightBorder,
  outlineVariant = MidnightCardSurface
)

// Midnight Bass is optimized for dark night audio sessions
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Always default to dark midnight aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MidnightColorScheme,
    typography = Typography,
    content = content
  )
}
