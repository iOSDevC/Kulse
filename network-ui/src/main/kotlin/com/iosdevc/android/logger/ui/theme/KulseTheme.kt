package com.iosdevc.android.logger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val KulseDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E7D32),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFFCE93D8),
    onTertiary = Color(0xFF4A148C),
    tertiaryContainer = Color(0xFF6A1B9A),
    onTertiaryContainer = Color(0xFFE1BEE7),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFC62828),
    onErrorContainer = Color(0xFFFFCDD2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF616161),
    outlineVariant = Color(0xFF424242),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF303030),
    inversePrimary = Color(0xFF1565C0),
    surfaceTint = Color(0xFF90CAF9),
)

private val MonospaceFamily = FontFamily.Monospace

private val KulseTypography = Typography(
    displayLarge = Typography().displayLarge,
    displayMedium = Typography().displayMedium,
    displaySmall = Typography().displaySmall,
    headlineLarge = Typography().headlineLarge,
    headlineMedium = Typography().headlineMedium,
    headlineSmall = Typography().headlineSmall,
    titleLarge = Typography().titleLarge,
    titleMedium = Typography().titleMedium,
    titleSmall = Typography().titleSmall,
    bodyLarge = TextStyle(
        fontFamily = MonospaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = MonospaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = MonospaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = Typography().labelLarge,
    labelMedium = Typography().labelMedium,
    labelSmall = Typography().labelSmall,
)

@Composable
fun KulseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = KulseDarkColorScheme,
        typography = KulseTypography,
        content = content,
    )
}
