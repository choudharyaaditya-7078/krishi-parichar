package com.example.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.mqtt.MqttConnectionState
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SunAmber

/**
 * SECTION A: Top Application Header with Krishi Parichar logo, title, tagline,
 * and live EMQX Cloud MQTT broker connectivity indicator pill.
 */
@Composable
fun TopHeaderSection(
    connectionState: MqttConnectionState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_header_section"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Agricultural Logo + Brand Title & Tagline
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = PrimaryGreen.copy(alpha = 0.25f),
                        spotColor = PrimaryGreen.copy(alpha = 0.25f)
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF0B5B36))
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_krishi_logo_1795265537101),
                    contentDescription = stringResource(R.string.cd_brand_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .scale(1.32f)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.brand_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("brand_title_text")
                )
                Text(
                    text = stringResource(R.string.brand_tagline),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right: Compact Glass EMQX Cloud Status Pill
        CloudConnectionStatusPill(connectionState = connectionState)
    }
}

@Composable
private fun CloudConnectionStatusPill(
    connectionState: MqttConnectionState,
    modifier: Modifier = Modifier
) {
    val shouldPulse = connectionState == MqttConnectionState.CONNECTED ||
        connectionState == MqttConnectionState.CONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "cloud_status_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val dotColor by animateColorAsState(
        targetValue = when (connectionState) {
            MqttConnectionState.CONNECTED -> AccentGreen
            MqttConnectionState.CONNECTING -> SunAmber
            MqttConnectionState.DISCONNECTED -> SecondaryText
            MqttConnectionState.ERROR -> Color(0xFFD94A4A)
        },
        animationSpec = tween(220),
        label = "status_dot_color"
    )

    val labelColor by animateColorAsState(
        targetValue = when (connectionState) {
            MqttConnectionState.CONNECTED -> DeepGreen
            MqttConnectionState.CONNECTING -> Color(0xFF9A5B00)
            MqttConnectionState.DISCONNECTED -> SecondaryText
            MqttConnectionState.ERROR -> Color(0xFFB3261E)
        },
        animationSpec = tween(220),
        label = "status_label_color"
    )

    val pillTintEnd by animateColorAsState(
        targetValue = when (connectionState) {
            MqttConnectionState.CONNECTED -> Color(0xD2ECFAF2)
            MqttConnectionState.CONNECTING -> Color(0xD6FFF6E5)
            MqttConnectionState.DISCONNECTED -> Color(0xD2EEF3F8)
            MqttConnectionState.ERROR -> Color(0xD6FDECEC)
        },
        animationSpec = tween(220),
        label = "status_pill_tint"
    )

    val statusText = when (connectionState) {
        MqttConnectionState.CONNECTED -> stringResource(R.string.status_cloud_connected)
        MqttConnectionState.CONNECTING -> stringResource(R.string.status_cloud_connecting)
        MqttConnectionState.DISCONNECTED -> stringResource(R.string.status_cloud_offline)
        MqttConnectionState.ERROR -> stringResource(R.string.status_cloud_error)
    }

    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = pillShape,
                ambientColor = dotColor.copy(alpha = 0.14f),
                spotColor = dotColor.copy(alpha = 0.14f)
            )
            .clip(pillShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xE6FFFFFF),
                        pillTintEnd
                    )
                )
            )
            .border(
                border = BorderStroke(1.dp, GlassBorderLight),
                shape = pillShape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("connectivity_status_pill"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (shouldPulse) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = pulseAlpha))
                )
            }
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = labelColor,
            maxLines = 1
        )
    }
}
