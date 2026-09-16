package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NoirColorScheme = darkColorScheme(
  primary = CrimsonPrimary,
  onPrimary = TextPrimaryDark,
  primaryContainer = CrimsonDark,
  onPrimaryContainer = GoldLight,
  secondary = GoldAccent,
  onSecondary = DeepObsidian,
  secondaryContainer = SurfaceElevated,
  onSecondaryContainer = GoldLight,
  tertiary = MysticPurpleLight,
  onTertiary = DeepObsidian,
  background = DeepObsidian,
  onBackground = TextPrimaryDark,
  surface = SurfaceDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceElevated,
  onSurfaceVariant = TextSecondaryDark,
  outline = SurfaceBorder,
  error = DangerRed,
  onError = TextPrimaryDark
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NoirColorScheme,
    typography = Typography,
    content = content
  )
}

