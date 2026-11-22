package com.example.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DeepGreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText

/**
 * SECTION B: Welcome / Greeting area positioned organically over the morning sunrise
 * agricultural landscape backdrop.
 */
@Composable
fun WelcomeHeroSection(
    modifier: Modifier = Modifier
) {
    val leafDesc = stringResource(R.string.cd_leaf_accent)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp)
            .testTag("welcome_greeting_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.greeting_headline),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = PrimaryText,
                modifier = Modifier.testTag("greeting_headline_text")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Delicate botanical green leaf visual accent
            GreenLeafAccent(
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = leafDesc }
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = stringResource(R.string.greeting_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 14.5.sp
            ),
            color = SecondaryText.copy(alpha = 0.92f)
        )
    }
}

@Composable
private fun GreenLeafAccent(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Main glossy leaf blade
        val leafPath = Path().apply {
            moveTo(w * 0.18f, h * 0.84f)
            cubicTo(
                w * 0.10f, h * 0.42f,
                w * 0.44f, h * 0.12f,
                w * 0.88f, h * 0.14f
            )
            cubicTo(
                w * 0.86f, h * 0.58f,
                w * 0.56f, h * 0.88f,
                w * 0.18f, h * 0.84f
            )
            close()
        }

        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(
                colors = listOf(AccentGreen, PrimaryGreen, DeepGreen),
                start = Offset(w * 0.85f, h * 0.15f),
                end = Offset(w * 0.15f, h * 0.85f)
            )
        )

        // Subtle sunlit leaf center vein & stem
        val veinPath = Path().apply {
            moveTo(w * 0.12f, h * 0.90f)
            quadraticTo(w * 0.46f, h * 0.56f, w * 0.74f, h * 0.28f)
        }
        drawPath(
            path = veinPath,
            color = Color(0xCCDCFFE8),
            style = Stroke(width = w * 0.075f, cap = StrokeCap.Round)
        )
    }
}
