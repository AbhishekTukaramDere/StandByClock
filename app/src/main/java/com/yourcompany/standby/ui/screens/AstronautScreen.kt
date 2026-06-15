package com.yourcompany.standby.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AstronautScreen(
    is24Hour: Boolean,
    showSeconds: Boolean,
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val hoursStr = currentTime.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH" else "hh"))
    val minutesStr = currentTime.format(DateTimeFormatter.ofPattern("mm"))
    val secondsStr = currentTime.format(DateTimeFormatter.ofPattern("ss"))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Twinkling stars in background
        TwinklingStarsBackground(modifier = Modifier.fillMaxSize())

        if (isLandscape) {
            // Landscape Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hours
                AnimatedDigitPair(hoursStr)

                // Center Astronaut
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AstronautDrawing(
                        secondsText = secondsStr,
                        showSeconds = showSeconds,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Minutes
                AnimatedDigitPair(minutesStr)
            }
        } else {
            // Portrait Layout: Vertical Stack
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Stacked digits
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedDigitPair(hoursStr)
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedDigitPair(minutesStr)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Astronaut on Lunar Horizon
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AstronautDrawing(
                        secondsText = secondsStr,
                        showSeconds = showSeconds,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AnimatedDigitPair(digits: String) {
    Row(horizontalArrangement = Arrangement.Center) {
        val d1 = digits.getOrNull(0)?.toString() ?: "0"
        val d2 = digits.getOrNull(1)?.toString() ?: "0"
        AnimatedDigit(d1)
        AnimatedDigit(d2)
    }
}

@Composable
fun AnimatedDigit(digit: String, modifier: Modifier = Modifier) {
    var previousDigit by remember { mutableStateOf(digit) }
    val animAlpha = remember { Animatable(0f) }

    LaunchedEffect(digit) {
        if (digit != previousDigit) {
            animAlpha.snapTo(0f)
            animAlpha.animateTo(1f, animationSpec = tween(300))
            previousDigit = digit
        } else {
            animAlpha.snapTo(1f)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outline background (dark-grey)
        Text(
            text = digit,
            color = Color(0xFF222222),
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        // Solid white fill overlay
        Text(
            text = digit,
            color = Color.White.copy(alpha = animAlpha.value),
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TwinklingStarsBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "StarTwinkle")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Canvas(modifier = modifier) {
        // Draw some cross-shaped stars
        val starPositions = listOf(
            Offset(size.width * 0.15f, size.height * 0.2f),
            Offset(size.width * 0.85f, size.height * 0.15f),
            Offset(size.width * 0.3f, size.height * 0.55f),
            Offset(size.width * 0.7f, size.height * 0.65f),
            Offset(size.width * 0.25f, size.height * 0.8f),
            Offset(size.width * 0.75f, size.height * 0.85f)
        )

        starPositions.forEachIndexed { index, pos ->
            val factor = if (index % 2 == 0) alpha else (1.2f - alpha)
            val color = Color.White.copy(alpha = factor.coerceIn(0.1f, 1.0f))
            
            // Draw cross-shaped star
            drawLine(
                color = color,
                start = Offset(pos.x - 8.dp.toPx(), pos.y),
                end = Offset(pos.x + 8.dp.toPx(), pos.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = color,
                start = Offset(pos.x, pos.y - 8.dp.toPx()),
                end = Offset(pos.x, pos.y + 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }
}

@Composable
fun AstronautDrawing(
    secondsText: String,
    showSeconds: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 1. Draw Astronaut Vector Outline and Curved Lunar Horizon
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Lunar horizon
            val horizonPath = Path().apply {
                moveTo(0f, height * 0.9f)
                quadraticBezierTo(width * 0.5f, height * 0.8f, width, height * 0.9f)
            }
            drawPath(
                path = horizonPath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx())
            )

            // Astronaut Body (Outline)
            // Suit center
            val cx = width * 0.5f
            val cy = height * 0.52f

            // Torso
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(cx - 24.dp.toPx(), cy),
                size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 56.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Helmet outline
            drawCircle(
                color = Color.White,
                radius = 32.dp.toPx(),
                center = Offset(cx, cy - 28.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Left Arm
            drawLine(
                color = Color.White,
                start = Offset(cx - 24.dp.toPx(), cy + 8.dp.toPx()),
                end = Offset(cx - 44.dp.toPx(), cy + 24.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            // Right Arm
            drawLine(
                color = Color.White,
                start = Offset(cx + 24.dp.toPx(), cy + 8.dp.toPx()),
                end = Offset(cx + 44.dp.toPx(), cy + 24.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            // Left Leg
            drawLine(
                color = Color.White,
                start = Offset(cx - 12.dp.toPx(), cy + 56.dp.toPx()),
                end = Offset(cx - 12.dp.toPx(), cy + 80.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            // Right Leg
            drawLine(
                color = Color.White,
                start = Offset(cx + 12.dp.toPx(), cy + 56.dp.toPx()),
                end = Offset(cx + 12.dp.toPx(), cy + 80.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 2. Visor Micro-Clock centered in the helmet
        if (showSeconds) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-28).dp) // Offset to center of helmet
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = secondsText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun AstronautScreenPortraitPreview() {
    AstronautScreen(is24Hour = false, showSeconds = true)
}
