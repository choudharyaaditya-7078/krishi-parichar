package com.example.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PumpOffBadgeBg
import com.example.ui.theme.PumpOffBadgeText
import com.example.ui.theme.PumpOffBg
import com.example.ui.theme.PumpOffBorder
import com.example.ui.theme.PumpOffIcon
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoftMintSurface
import com.example.ui.theme.WaterBlueMid
import com.example.ui.theme.WaterBlueStart
import kotlin.math.PI
import kotlin.math.sin

/**
 * SECTION C: Water Pump Control Card.
 * Interactive local UI-only demonstration allowing farmers to toggle between ON and OFF.
 */
@Composable
fun WaterPumpCard(
    isPumpOn: Boolean,
    onTogglePump: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardGradientTop by animateColorAsState(
        targetValue = if (isPumpOn) Color(0xE0F4FCFF) else Color(0xDDF4F7FB),
        animationSpec = tween(260),
        label = "pump_card_top"
    )
    val cardGradientBottom by animateColorAsState(
        targetValue = if (isPumpOn) Color(0xCCE5F5EE) else Color(0xCCE8EFF7),
        animationSpec = tween(260),
        label = "pump_card_bottom"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("water_pump_card"),
        shape = RoundedCornerShape(24.dp),
        gradientColors = listOf(cardGradientTop, cardGradientBottom),
        borderColors = listOf(
            Color(0xEBFFFFFF),
            if (isPumpOn) Color(0x8822C55E) else Color(0x88C7E1FF)
        ),
        shadowElevation = 12.dp,
        shadowTint = if (isPumpOn) PrimaryGreen.copy(alpha = 0.14f) else PrimaryText.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Water Pump Illustration + Subtle Animated Water-Flow Visual
            WaterPumpVisualFrame(isPumpOn = isPumpOn)

            Spacer(modifier = Modifier.width(14.dp))

            // CENTER: Heading, Animated ON/OFF Status Indicator, & Status Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.pump_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryText,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                PumpStatusBadge(isPumpOn = isPumpOn)

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = stringResource(
                        if (isPumpOn) R.string.pump_subtitle_on else R.string.pump_subtitle_off
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp
                    ),
                    color = SecondaryText,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // RIGHT: Large Circular Power Button with Luminous Border & Shadow
            PowerControlButton(
                isPumpOn = isPumpOn,
                onClick = onTogglePump
            )
        }
    }
}

@Composable
private fun WaterPumpVisualFrame(
    isPumpOn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_flow")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val imageAlpha by animateFloatAsState(
        targetValue = if (isPumpOn) 1f else 0.72f,
        animationSpec = tween(250),
        label = "pump_image_alpha"
    )

    val frameShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .size(width = 92.dp, height = 78.dp)
            .clip(frameShape)
            .background(Color(0xFFEEF6FF))
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isPumpOn) Color(0x9938BDF8) else Color(0x88FFFFFF)
                ),
                shape = frameShape
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_water_pump_1795266096264),
            contentDescription = stringResource(R.string.cd_water_pump),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha)
        )

        // Subtle flowing water wave shimmer along the lower edge when pump is active
        if (isPumpOn) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.6f)
                    val steps = 20
                    for (i in 0..steps) {
                        val x = (w / steps) * i
                        val y = h * 0.55f + sin((x / w) * 2.5f * PI + wavePhase).toFloat() * 3.5f
                        lineTo(x, y)
                    }
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            WaterBlueMid.copy(alpha = 0.65f),
                            WaterBlueStart.copy(alpha = 0.75f),
                            Color.Transparent
                        )
                    ),
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun PumpStatusBadge(
    isPumpOn: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeBg by animateColorAsState(
        targetValue = if (isPumpOn) SoftMintSurface else PumpOffBadgeBg,
        animationSpec = tween(220),
        label = "status_bg"
    )
    val badgeTextColor by animateColorAsState(
        targetValue = if (isPumpOn) DeepGreen else PumpOffBadgeText,
        animationSpec = tween(220),
        label = "status_text"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isPumpOn) AccentGreen else PumpOffIcon,
        animationSpec = tween(220),
        label = "status_dot"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(badgeBg)
            .border(
                width = 1.dp,
                color = if (isPumpOn) AccentGreen.copy(alpha = 0.35f) else PumpOffBorder,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        AnimatedContent(
            targetState = isPumpOn,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
            label = "pump_status_label"
        ) { active ->
            Text(
                text = stringResource(if (active) R.string.pump_status_on else R.string.pump_status_off),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                ),
                color = badgeTextColor,
                modifier = Modifier.testTag("water_pump_status_text")
            )
        }
    }
}

@Composable
private fun PowerControlButton(
    isPumpOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "power_btn_scale"
    )

    val topColor by animateColorAsState(
        targetValue = if (isPumpOn) AccentGreen else PumpOffBg,
        animationSpec = tween(240),
        label = "btn_top"
    )
    val bottomColor by animateColorAsState(
        targetValue = if (isPumpOn) PrimaryGreen else Color(0xFFD5E1ED),
        animationSpec = tween(240),
        label = "btn_bottom"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isPumpOn) Color.White else PumpOffIcon,
        animationSpec = tween(240),
        label = "btn_icon"
    )
    val glowRingColor by animateColorAsState(
        targetValue = if (isPumpOn) AccentGreen.copy(alpha = 0.28f) else PumpOffBorder.copy(alpha = 0.45f),
        animationSpec = tween(240),
        label = "btn_glow"
    )

    // Subtle outer breathing aura when active
    val infiniteTransition = rememberInfiniteTransition(label = "power_glow")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )

    Box(
        modifier = modifier
            .size(68.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer luminous glow ring
        Box(
            modifier = Modifier
                .size(66.dp)
                .scale(if (isPumpOn) auraScale else 1f)
                .clip(CircleShape)
                .background(glowRingColor)
        )

        // Core circular tactile button (>= 48dp touch target)
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isPumpOn) 10.dp else 3.dp,
                    shape = CircleShape,
                    ambientColor = if (isPumpOn) PrimaryGreen else PrimaryText.copy(alpha = 0.2f),
                    spotColor = if (isPumpOn) PrimaryGreen else PrimaryText.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(topColor, bottomColor),
                        start = Offset(0f, 0f),
                        end = Offset(120f, 120f)
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = if (isPumpOn) Color(0xBBFFFFFF) else Color(0xCCFFFFFF),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White),
                    role = Role.Switch,
                    onClickLabel = stringResource(R.string.pump_toggle_cd),
                    onClick = onClick
                )
                .testTag("water_pump_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = stringResource(R.string.pump_toggle_cd),
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
