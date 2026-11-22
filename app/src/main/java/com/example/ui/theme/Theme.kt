package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val KrishiLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = SoftMintSurface,
    onPrimaryContainer = DeepGreen,
    secondary = WaterBlueStart,
    onSecondary = Color.White,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = PrimaryText,
    tertiary = AccentGreen,
    onTertiary = Color.White,
    tertiaryContainer = SoftLavender,
    onTertiaryContainer = PrimaryText,
    background = BackgroundWhite,
    onBackground = PrimaryText,
    surface = BackgroundWhite,
    onSurface = PrimaryText,
    surfaceVariant = SkyBlue,
    onSurfaceVariant = SecondaryText,
    outline = GlassBorderSubtle,
    outlineVariant = LightBlue
)

val KrishiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KrishiParicharTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KrishiLightColorScheme,
        typography = KrishiTypography,
        shapes = KrishiShapes,
        content = content
    )
}
