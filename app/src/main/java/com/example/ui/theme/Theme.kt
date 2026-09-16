package com.example.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle

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
    typography = Typography
  ) {
    CompositionLocalProvider(
      LocalTextStyle provides TextStyle(
        fontFamily = VazirmatnFontFamily,
        color = TextPrimaryDark
      ),
      content = content
    )
  }
}


