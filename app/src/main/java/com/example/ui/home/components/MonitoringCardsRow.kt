package com.example.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.MoistureGlassBottom
import com.example.ui.theme.MoistureGlassTop
import com.example.ui.theme.NutrientsGlassBottom
import com.example.ui.theme.NutrientsGlassTop
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SoilDarkBrown
import com.example.ui.theme.SoilRichBrown
import com.example.ui.theme.SunAmber
import com.example.ui.theme.SunGold
import com.example.ui.theme.WaterBlueEnd
import com.example.ui.theme.WaterBlueMid
import com.example.ui.theme.WaterBlueStart
import com.example.ui.theme.WeatherGlassBottom
import com.example.ui.theme.WeatherGlassTop

/**
 * SECTION D: Three visually equal-importance agricultural monitoring cards:
 * Weather, Soil Moisture, and Soil Nutrients.
 */
@Composable
fun MonitoringCardsRow(
    temperature: String,
    weatherCondition: String,
    soilMoisturePercent: Int,
    soilMoistureProgress: Float,
    soilNutrients: String,
    nutrientsLastReport: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monitoring_cards_row")
    ) {
        val isUltraNarrow = maxWidth < 310.dp
        val spacing = if (maxWidth >= 600.dp) 14.dp else 10.dp

        if (isUltraNarrow) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WeatherMonitoringCard(
                    temperature = temperature,
                    condition = weatherCondition,
                    modifier = Modifier.fillMaxWidth()
                )
                SoilMoistureMonitoringCard(
                    moisturePercent = soilMoisturePercent,
                    progress = soilMoistureProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                SoilNutrientsMonitoringCard(
                    nutrientsValue = soilNutrients,
                    lastReport = nutrientsLastReport,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                WeatherMonitoringCard(
                    temperature = temperature,
                    condition = weatherCondition,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SoilMoistureMonitoringCard(
                    moisturePercent = soilMoisturePercent,
                    progress = soilMoistureProgress,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SoilNutrientsMonitoringCard(
                    nutrientsValue = soilNutrients,
                    lastReport = nutrientsLastReport,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun WeatherMonitoringCard(
    temperature: String,
    condition: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag("card_weather"),
        shape = RoundedCornerShape(20.dp),
        gradientColors = listOf(WeatherGlassTop, WeatherGlassBottom),
        borderColors = listOf(Color(0xEBFFFFFF), Color(0x88B8DBFF)),
        shadowElevation = 8.dp,
        shadowTint = WaterBlueStart.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SunCloudIllustration(modifier = Modifier.size(42.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.card_weather_title),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = temperature,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = PrimaryText,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = condition,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SoilMoistureMonitoringCard(
    moisturePercent: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag("card_soil_moisture"),
        shape = RoundedCornerShape(20.dp),
        gradientColors = listOf(MoistureGlassTop, MoistureGlassBottom),
        borderColors = listOf(Color(0xEBFFFFFF), Color(0x88A8D3FF)),
        shadowElevation = 8.dp,
        shadowTint = WaterBlueStart.copy(alpha = 0.11f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GrowingPlantSoilIllustration(modifier = Modifier.size(42.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.card_moisture_title),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$moisturePercent%",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = PrimaryText,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth horizontal blue-to-light-blue progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x66FFFFFF))
                    .border(
                        width = 0.6.dp,
                        color = Color(0x99FFFFFF),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(WaterBlueStart, WaterBlueMid, WaterBlueEnd)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SoilNutrientsMonitoringCard(
    nutrientsValue: String,
    lastReport: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.testTag("card_soil_nutrients"),
        shape = RoundedCornerShape(20.dp),
        gradientColors = listOf(NutrientsGlassTop, NutrientsGlassBottom),
        borderColors = listOf(Color(0xEBFFFFFF), Color(0x88A7E5CB)),
        shadowElevation = 8.dp,
        shadowTint = PrimaryGreen.copy(alpha = 0.11f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LabFlaskLeafIllustration(modifier = Modifier.size(42.dp))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.card_nutrients_title),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = nutrientsValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.2).sp
                ),
                color = PrimaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Compact reporting status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xA6FFFFFF))
                    .border(
                        width = 0.8.dp,
                        color = PrimaryGreen.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = lastReport,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DeepGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SunCloudIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft glowing sun aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SunGold.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.64f, h * 0.34f),
                radius = w * 0.38f
            ),
            radius = w * 0.38f,
            center = Offset(w * 0.64f, h * 0.34f)
        )

        // Warm golden sun orb
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFD85A), SunGold, SunAmber),
                start = Offset(w * 0.50f, h * 0.14f),
                end = Offset(w * 0.82f, h * 0.52f)
            ),
            radius = w * 0.22f,
            center = Offset(w * 0.64f, h * 0.34f)
        )

        // Fluffy shaded cloud foreground
        val cloudPath = Path().apply {
            moveTo(w * 0.20f, h * 0.78f)
            lineTo(w * 0.72f, h * 0.78f)
            cubicTo(w * 0.86f, h * 0.78f, w * 0.88f, h * 0.56f, w * 0.73f, h * 0.54f)
            cubicTo(w * 0.71f, h * 0.36f, w * 0.46f, h * 0.34f, w * 0.40f, h * 0.49f)
            cubicTo(w * 0.26f, h * 0.45f, w * 0.14f, h * 0.58f, w * 0.18f, h * 0.72f)
            cubicTo(w * 0.18f, h * 0.76f, w * 0.19f, h * 0.78f, w * 0.20f, h * 0.78f)
            close()
        }

        drawPath(
            path = cloudPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFFE3F1FF)),
                startY = h * 0.36f,
                endY = h * 0.80f
            )
        )
        drawPath(
            path = cloudPath,
            color = Color(0x99B6D9FF),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
private fun GrowingPlantSoilIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Rich curved soil mound
        val soilPath = Path().apply {
            moveTo(w * 0.14f, h * 0.84f)
            quadraticTo(w * 0.50f, h * 0.58f, w * 0.86f, h * 0.84f)
            quadraticTo(w * 0.50f, h * 0.92f, w * 0.14f, h * 0.84f)
            close()
        }
        drawPath(
            path = soilPath,
            brush = Brush.verticalGradient(
                colors = listOf(SoilRichBrown, SoilDarkBrown),
                startY = h * 0.65f,
                endY = h * 0.90f
            )
        )

        // Fresh sprout stem
        drawLine(
            color = PrimaryGreen,
            start = Offset(w * 0.50f, h * 0.72f),
            end = Offset(w * 0.50f, h * 0.34f),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Left sprouting leaf
        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.50f)
            cubicTo(w * 0.22f, h * 0.48f, w * 0.16f, h * 0.26f, w * 0.26f, h * 0.22f)
            cubicTo(w * 0.42f, h * 0.22f, w * 0.50f, h * 0.36f, w * 0.50f, h * 0.50f)
            close()
        }
        drawPath(
            path = leftLeaf,
            brush = Brush.linearGradient(listOf(AccentGreen, PrimaryGreen))
        )

        // Right sprouting leaf
        val rightLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.44f)
            cubicTo(w * 0.52f, h * 0.24f, w * 0.68f, h * 0.14f, w * 0.80f, h * 0.18f)
            cubicTo(w * 0.80f, h * 0.36f, w * 0.66f, h * 0.46f, w * 0.50f, h * 0.44f)
            close()
        }
        drawPath(
            path = rightLeaf,
            brush = Brush.linearGradient(listOf(Color(0xFF4ADE80), PrimaryGreen))
        )

        // Sparkling moisture water droplet accent
        val dropPath = Path().apply {
            moveTo(w * 0.80f, h * 0.44f)
            cubicTo(w * 0.88f, h * 0.56f, w * 0.88f, h * 0.66f, w * 0.80f, h * 0.66f)
            cubicTo(w * 0.72f, h * 0.66f, w * 0.72f, h * 0.56f, w * 0.80f, h * 0.44f)
            close()
        }
        drawPath(
            path = dropPath,
            brush = Brush.verticalGradient(listOf(WaterBlueMid, WaterBlueStart))
        )
    }
}

@Composable
private fun LabFlaskLeafIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Conical flask silhouette
        val flaskPath = Path().apply {
            moveTo(w * 0.36f, h * 0.18f)
            lineTo(w * 0.54f, h * 0.18f)
            lineTo(w * 0.54f, h * 0.38f)
            lineTo(w * 0.74f, h * 0.76f)
            quadraticTo(w * 0.78f, h * 0.86f, w * 0.66f, h * 0.86f)
            lineTo(w * 0.24f, h * 0.86f)
            quadraticTo(w * 0.12f, h * 0.86f, w * 0.16f, h * 0.76f)
            lineTo(w * 0.36f, h * 0.38f)
            close()
        }
        drawPath(
            path = flaskPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xE6FFFFFF), Color(0xCCD8F6EC))
            )
        )

        // Glowing green-teal nutrient liquid inside flask
        val liquidPath = Path().apply {
            moveTo(w * 0.28f, h * 0.58f)
            quadraticTo(w * 0.45f, h * 0.53f, w * 0.62f, h * 0.58f)
            lineTo(w * 0.72f, h * 0.77f)
            quadraticTo(w * 0.74f, h * 0.84f, w * 0.65f, h * 0.84f)
            lineTo(w * 0.25f, h * 0.84f)
            quadraticTo(w * 0.16f, h * 0.84f, w * 0.18f, h * 0.77f)
            close()
        }
        drawPath(
            path = liquidPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF34D399), PrimaryGreen)
            )
        )

        // Flask glass rim & outline
        drawPath(
            path = flaskPath,
            color = DeepGreen.copy(alpha = 0.55f),
            style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
        )
        drawLine(
            color = DeepGreen.copy(alpha = 0.65f),
            start = Offset(w * 0.32f, h * 0.18f),
            end = Offset(w * 0.58f, h * 0.18f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Bubbles inside nutrient liquid
        drawCircle(
            color = Color(0xB3FFFFFF),
            radius = w * 0.035f,
            center = Offset(w * 0.42f, h * 0.72f)
        )
        drawCircle(
            color = Color(0x99FFFFFF),
            radius = w * 0.025f,
            center = Offset(w * 0.52f, h * 0.66f)
        )

        // Crisp green leaf overlapping upper right of flask
        val leafPath = Path().apply {
            moveTo(w * 0.58f, h * 0.50f)
            cubicTo(w * 0.58f, h * 0.28f, w * 0.74f, h * 0.16f, w * 0.90f, h * 0.20f)
            cubicTo(w * 0.88f, h * 0.42f, w * 0.74f, h * 0.54f, w * 0.58f, h * 0.50f)
            close()
        }
        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(
                colors = listOf(AccentGreen, DeepGreen)
            )
        )
        drawLine(
            color = Color(0xCCDCFFE8),
            start = Offset(w * 0.60f, h * 0.48f),
            end = Offset(w * 0.82f, h * 0.27f),
            strokeWidth = 1.3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
