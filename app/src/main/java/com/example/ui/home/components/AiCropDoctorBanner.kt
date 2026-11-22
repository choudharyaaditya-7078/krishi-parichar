package com.example.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CenterFocusWeak
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkEmerald
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGlow
import com.example.ui.theme.PrimaryGreen

/**
 * SECTION F: Premium AI Crop Doctor Promotional Feature Banner.
 * UI-only visual showcase (no camera or disease recognition implementation).
 */
@Composable
fun AiCropDoctorBanner(
    modifier: Modifier = Modifier,
    onScanPlaceholderClick: () -> Unit = {}
) {
    val bannerShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = bannerShape,
                ambientColor = PrimaryGreen.copy(alpha = 0.34f),
                spotColor = DeepGreen.copy(alpha = 0.34f)
            )
            .clip(bannerShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF19A14E),
                        ForestGreen,
                        DarkEmerald
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 520f)
                )
            )
            .border(
                border = BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xAA99F6B8),
                            Color(0x44FFFFFF),
                            Color(0x884ADE80)
                        )
                    )
                ),
                shape = bannerShape
            )
            .testTag("ai_crop_doctor_banner")
    ) {
        // Subtle ambient green glow + decorative botanical leaves & sparkles
        BannerDecorativeCanvas(
            modifier = Modifier.matchParentSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 14.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN: Badge, Headline, Description, & SCAN PLANT CTA
            Column(
                modifier = Modifier.weight(1.18f),
                verticalArrangement = Arrangement.Center
            ) {
                // Krishi-Tech 5000 Pro subtle frosted tag
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x2EFFFFFF))
                        .border(
                            width = 0.8.dp,
                            color = Color(0x4DFFFFFF),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD1FFE0),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.crop_doctor_badge),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp
                        ),
                        color = Color(0xFFE5FFED),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.crop_doctor_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.crop_doctor_desc),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color(0xE0E8FBF0)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Prominent Luminous CTA Button (>= 48dp accessible height)
                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(50),
                            ambientColor = Color.Black.copy(alpha = 0.22f),
                            spotColor = Color.Black.copy(alpha = 0.22f)
                        )
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White, Color(0xFFEAFDF1))
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable(onClick = onScanPlaceholderClick)
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .testTag("scan_plant_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CenterFocusWeak,
                        contentDescription = null,
                        tint = DeepGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.crop_doctor_cta),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = DeepGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // RIGHT VISUAL: Young Green Plant Framed Inside Subtle Glowing Scanner Reticle
            PlantScannerIllustration(
                modifier = Modifier.size(132.dp)
            )
        }
    }
}

@Composable
private fun PlantScannerIllustration(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "plant_scan_line")
    val scanYFraction by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line_y"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(ForestGreen),
        contentAlignment = Alignment.Center
    ) {
        // Generated glossy young crop plant illustration
        Image(
            painter = painterResource(id = R.drawable.img_crop_doctor_plant_1795266096080),
            contentDescription = stringResource(R.string.cd_crop_plant),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Radial vignette blend into deep emerald banner + luminous scanner brackets & scanline
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft outer vignette so the image edges dissolve smoothly into the green banner
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,
                        0.66f to Color.Transparent,
                        1.0f to ForestGreen.copy(alpha = 0.72f)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.66f
                )
            )

            // Subtle animated scanning laser line
            val lineY = h * scanYFraction
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xCC86EFAC),
                        Color.White.copy(alpha = 0.92f),
                        Color(0xCC86EFAC),
                        Color.Transparent
                    )
                ),
                start = Offset(w * 0.14f, lineY),
                end = Offset(w * 0.86f, lineY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Luminous four-corner scanner brackets
            val pad = w * 0.08f
            val arm = w * 0.17f
            val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            val bracketColor = Color(0xD9DCFFE8)

            // Top-Left bracket
            drawPath(
                path = Path().apply {
                    moveTo(pad, pad + arm)
                    lineTo(pad, pad)
                    lineTo(pad + arm, pad)
                },
                color = bracketColor,
                style = stroke
            )
            // Top-Right bracket
            drawPath(
                path = Path().apply {
                    moveTo(w - pad - arm, pad)
                    lineTo(w - pad, pad)
                    lineTo(w - pad, pad + arm)
                },
                color = bracketColor,
                style = stroke
            )
            // Bottom-Left bracket
            drawPath(
                path = Path().apply {
                    moveTo(pad, h - pad - arm)
                    lineTo(pad, h - pad)
                    lineTo(pad + arm, h - pad)
                },
                color = bracketColor,
                style = stroke
            )
            // Bottom-Right bracket
            drawPath(
                path = Path().apply {
                    moveTo(w - pad - arm, h - pad)
                    lineTo(w - pad, h - pad)
                    lineTo(w - pad, h - pad - arm)
                },
                color = bracketColor,
                style = stroke
            )

            // Delicate sparkle stars near plant tips
            drawFourPointSparkle(
                center = Offset(w * 0.82f, h * 0.22f),
                radius = 5.5.dp.toPx(),
                color = Color(0xEEFFFFFF)
            )
            drawFourPointSparkle(
                center = Offset(w * 0.18f, h * 0.28f),
                radius = 3.8.dp.toPx(),
                color = Color(0xCCB6FFD0)
            )
        }
    }
}

@Composable
private fun BannerDecorativeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft glowing emerald light burst behind the plant area
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AccentGreen.copy(alpha = 0.32f),
                    MintGlow.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = Offset(w * 0.80f, h * 0.45f),
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = Offset(w * 0.80f, h * 0.45f)
        )

        // Subtle translucent decorative botanical leaf silhouette in lower-left corner
        val bgLeaf = Path().apply {
            moveTo(-w * 0.03f, h * 0.96f)
            cubicTo(w * 0.02f, h * 0.56f, w * 0.20f, h * 0.52f, w * 0.30f, h * 0.72f)
            cubicTo(w * 0.22f, h * 0.92f, w * 0.08f, h * 1.02f, -w * 0.03f, h * 0.96f)
            close()
        }
        drawPath(path = bgLeaf, color = Color(0x14FFFFFF))

        // Subtle top decorative leaf accent
        val topLeaf = Path().apply {
            moveTo(w * 0.50f, -h * 0.06f)
            cubicTo(w * 0.56f, h * 0.14f, w * 0.66f, h * 0.16f, w * 0.70f, -h * 0.04f)
            close()
        }
        drawPath(path = topLeaf, color = Color(0x14FFFFFF))

        // Subtle ambient sparkles
        drawFourPointSparkle(
            center = Offset(w * 0.55f, h * 0.24f),
            radius = 4.dp.toPx(),
            color = Color(0x77D1FFE0)
        )
        drawFourPointSparkle(
            center = Offset(w * 0.62f, h * 0.80f),
            radius = 3.2.dp.toPx(),
            color = Color(0x66FFFFFF)
        )
    }
}

private fun DrawScope.drawFourPointSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val inner = radius * 0.24f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + inner, center.y - inner, center.x + radius, center.y)
        quadraticTo(center.x + inner, center.y + inner, center.x, center.y + radius)
        quadraticTo(center.x - inner, center.y + inner, center.x - radius, center.y)
        quadraticTo(center.x - inner, center.y - inner, center.x, center.y - radius)
        close()
    }
    drawPath(path = path, color = color)
}
