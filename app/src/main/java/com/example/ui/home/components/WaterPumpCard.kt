package com.example.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.mqtt.PumpState
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
 * Reflects strictly hardware-confirmed PumpState (ON, OFF, UNKNOWN) reported by the ESP8266
 * via EMQX Cloud MQTT, with pending-command loading and restrained error feedback.
 */
@Composable
fun WaterPumpCard(
    pumpState: PumpState,
    isCommandPending: Boolean,
    isLastStatusRetained: Boolean,
    lastError: String?,
    onPumpAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfirmedOn = pumpState == PumpState.ON

    val cardGradientTop by animateColorAsState(
        targetValue = if (isConfirmedOn) Color(0xE0F4FCFF) else Color(0xDDF4F7FB),
        animationSpec = tween(260),
        label = "pump_card_top"
    )
    val cardGradientBottom by animateColorAsState(
        targetValue = if (isConfirmedOn) Color(0xCCE5F5EE) else Color(0xCCE8EFF7),
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
            if (isConfirmedOn) Color(0x8822C55E) else Color(0x88C7E1FF)
        ),
        shadowElevation = 12.dp,
        shadowTint = if (isConfirmedOn) PrimaryGreen.copy(alpha = 0.14f) else PrimaryText.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT: Water Pump Illustration + Animated Water-Flow Visual (strictly when confirmed ON)
                WaterPumpVisualFrame(isConfirmedOn = isConfirmedOn)

                Spacer(modifier = Modifier.width(14.dp))

                // CENTER: Heading, Confirmed Status Badge, Pending Indicator & Subtitle
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

                    PumpStatusBadge(pumpState = pumpState)

                    Spacer(modifier = Modifier.height(5.dp))

                    if (isCommandPending) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("water_pump_pending_indicator")
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.8.dp,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.pump_status_sending),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                ),
                                color = DeepGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        val subtitleText = when (pumpState) {
                            PumpState.ON -> stringResource(
                                if (isLastStatusRetained) {
                                    R.string.pump_subtitle_retained_on
                                } else {
                                    R.string.pump_subtitle_on
                                }
                            )
                            PumpState.OFF -> stringResource(
                                if (isLastStatusRetained) {
                                    R.string.pump_subtitle_retained_off
                                } else {
                                    R.string.pump_subtitle_off
                                }
                            )
                            PumpState.UNKNOWN -> stringResource(R.string.pump_subtitle_unknown)
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp
                            ),
                            color = SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // RIGHT: Large Circular Power Button reflecting confirmed state & pending disable
                PowerControlButton(
                    pumpState = pumpState,
                    isCommandPending = isCommandPending,
                    onClick = onPumpAction
                )
            }

            // Restrained inline error notice (preserves last confirmed pump state and allows retry)
            AnimatedVisibility(
                visible = !lastError.isNullOrBlank(),
                enter = fadeIn(tween(180)) + expandVertically(),
                exit = fadeOut(tween(150)) + shrinkVertically()
            ) {
                if (!lastError.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xD9FDECEC))
                            .border(
                                width = 1.dp,
                                color = Color(0x4DD94A4A),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("water_pump_error_banner"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFB3261E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = lastError,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF8C1D18),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterPumpVisualFrame(
    isConfirmedOn: Boolean,
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
        targetValue = if (isConfirmedOn) 1f else 0.72f,
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
                    color = if (isConfirmedOn) Color(0x9938BDF8) else Color(0x88FFFFFF)
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

        // Subtle flowing water wave shimmer along the lower edge ONLY when confirmed ON
        if (isConfirmedOn) {
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
    pumpState: PumpState,
    modifier: Modifier = Modifier
) {
    val isConfirmedOn = pumpState == PumpState.ON

    val badgeBg by animateColorAsState(
        targetValue = if (isConfirmedOn) SoftMintSurface else PumpOffBadgeBg,
        animationSpec = tween(220),
        label = "status_bg"
    )
    val badgeTextColor by animateColorAsState(
        targetValue = if (isConfirmedOn) DeepGreen else PumpOffBadgeText,
        animationSpec = tween(220),
        label = "status_text"
    )
    val dotColor by animateColorAsState(
        targetValue = if (isConfirmedOn) AccentGreen else PumpOffIcon,
        animationSpec = tween(220),
        label = "status_dot"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(badgeBg)
            .border(
                width = 1.dp,
                color = if (isConfirmedOn) AccentGreen.copy(alpha = 0.35f) else PumpOffBorder,
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
            targetState = pumpState,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
            label = "pump_status_label"
        ) { state ->
            val labelRes = when (state) {
                PumpState.ON -> R.string.pump_status_on
                PumpState.OFF -> R.string.pump_status_off
                PumpState.UNKNOWN -> R.string.pump_status_unknown
            }
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = if (state == PumpState.UNKNOWN) 10.sp else 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = if (state == PumpState.UNKNOWN) 0.4.sp else 0.8.sp
                ),
                color = badgeTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("water_pump_status_text")
            )
        }
    }
}

@Composable
private fun PowerControlButton(
    pumpState: PumpState,
    isCommandPending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConfirmedOn = pumpState == PumpState.ON
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isCommandPending) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "power_btn_scale"
    )

    val topColor by animateColorAsState(
        targetValue = if (isConfirmedOn) AccentGreen else PumpOffBg,
        animationSpec = tween(240),
        label = "btn_top"
    )
    val bottomColor by animateColorAsState(
        targetValue = if (isConfirmedOn) PrimaryGreen else Color(0xFFD5E1ED),
        animationSpec = tween(240),
        label = "btn_bottom"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isConfirmedOn) Color.White else PumpOffIcon,
        animationSpec = tween(240),
        label = "btn_icon"
    )
    val glowRingColor by animateColorAsState(
        targetValue = if (isConfirmedOn) AccentGreen.copy(alpha = 0.28f) else PumpOffBorder.copy(alpha = 0.45f),
        animationSpec = tween(240),
        label = "btn_glow"
    )

    // Subtle outer breathing aura when confirmed ON
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

    val actionContentDescription = stringResource(
        if (pumpState == PumpState.UNKNOWN) R.string.pump_turn_on_cd else R.string.pump_toggle_cd
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
                .scale(if (isConfirmedOn) auraScale else 1f)
                .clip(CircleShape)
                .background(glowRingColor)
        )

        // Core circular tactile button (>= 48dp touch target)
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isConfirmedOn) 10.dp else 3.dp,
                    shape = CircleShape,
                    ambientColor = if (isConfirmedOn) PrimaryGreen else PrimaryText.copy(alpha = 0.2f),
                    spotColor = if (isConfirmedOn) PrimaryGreen else PrimaryText.copy(alpha = 0.2f)
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
                    color = if (isConfirmedOn) Color(0xBBFFFFFF) else Color(0xCCFFFFFF),
                    shape = CircleShape
                )
                .clickable(
                    enabled = !isCommandPending,
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, color = Color.White),
                    role = Role.Button,
                    onClickLabel = actionContentDescription,
                    onClick = onClick
                )
                .testTag("water_pump_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isCommandPending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.6.dp,
                    color = if (isConfirmedOn) Color.White else PrimaryGreen
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = actionContentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
