package com.example.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteMedium
import com.example.ui.theme.PrimaryText

/**
 * Reusable translucent glassmorphism card surface with layered vertical frost gradient,
 * luminous translucent border, and subtle colored ambient elevation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    gradientColors: List<Color> = listOf(GlassWhiteHigh, GlassWhiteMedium),
    borderColors: List<Color> = listOf(GlassBorderLight, Color(0x80FFFFFF), Color(0x55C7E1FF)),
    shadowElevation: Dp = 10.dp,
    shadowTint: Color = PrimaryText.copy(alpha = 0.08f),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowTint,
                spotColor = shadowTint
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(colors = borderColors)
                ),
                shape = shape
            )
            .then(clickableModifier),
        content = content
    )
}
