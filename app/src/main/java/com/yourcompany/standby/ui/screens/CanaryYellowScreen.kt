package com.yourcompany.standby.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CanaryYellowScreen(
    batteryLevel: Int,
    isCharging: Boolean,
    is24Hour: Boolean,
    showSeconds: Boolean,
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val hoursStr = currentTime.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH" else "hh"))
    val minutesStr = currentTime.format(DateTimeFormatter.ofPattern("mm"))
    val secondsStr = currentTime.format(DateTimeFormatter.ofPattern("ss"))
    val amPmStr = currentTime.format(DateTimeFormatter.ofPattern("a"))
    val timeLabel = if (is24Hour) {
        "${hoursStr}:${minutesStr}"
    } else {
        "${hoursStr}:${minutesStr} $amPmStr"
    }

    val dateStr = currentTime.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFEE00)) // Canary Yellow
    ) {
        // Status Pills (top corners)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Clock
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏰ $timeLabel",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Pill: Battery
            val batteryDotColor = if (isCharging) Color(0xFF4CAF50) else Color.White
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔋 $batteryLevel%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(batteryDotColor)
                    )
                }
            }
        }

        if (isLandscape) {
            // Landscape Layout: Flanked Tri-Partite
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Far Left: Hours
                Text(
                    text = hoursStr,
                    color = Color.Black,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                // Center: Artwork + Seconds
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    SilhouetteArtwork(
                        modifier = Modifier
                            .size(160.dp)
                    )
                    if (showSeconds) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = secondsStr,
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Far Right: Minutes
                Text(
                    text = minutesStr,
                    color = Color.Black,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }

            // Date Capsule at bottom center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = dateStr,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
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

                // Time display (Stacked vertically)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hours
                    Text(
                        text = hoursStr,
                        color = Color.Black,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    // Two massive black dots (separator)
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Black))
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Black))
                    }

                    // Minutes
                    Text(
                        text = minutesStr,
                        color = Color.Black,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }

                // Seconds
                if (showSeconds) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = secondsStr,
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Overlapping silhouettes artwork
                SilhouetteArtwork(
                    modifier = Modifier
                        .size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Date capsule
                Box(
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = dateStr,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SilhouetteArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Left profile path
        val leftPath = Path().apply {
            moveTo(width * 0.45f, height * 0.1f)
            // forehead
            cubicTo(width * 0.3f, height * 0.1f, width * 0.2f, height * 0.25f, width * 0.2f, height * 0.35f)
            // nose
            lineTo(width * 0.1f, height * 0.42f)
            lineTo(width * 0.22f, height * 0.48f)
            // lips
            cubicTo(width * 0.15f, height * 0.54f, width * 0.15f, height * 0.6f, width * 0.25f, height * 0.62f)
            // chin
            cubicTo(width * 0.2f, height * 0.7f, width * 0.22f, height * 0.75f, width * 0.3f, height * 0.78f)
            // neck
            lineTo(width * 0.32f, height * 0.95f)
            lineTo(width * 0.55f, height * 0.95f)
            // back of head/neck
            cubicTo(width * 0.55f, height * 0.7f, width * 0.55f, height * 0.35f, width * 0.45f, height * 0.1f)
        }

        // Right profile path
        val rightPath = Path().apply {
            moveTo(width * 0.55f, height * 0.1f)
            // forehead
            cubicTo(width * 0.7f, height * 0.1f, width * 0.8f, height * 0.25f, width * 0.8f, height * 0.35f)
            // nose
            lineTo(width * 0.9f, height * 0.42f)
            lineTo(width * 0.78f, height * 0.48f)
            // lips
            cubicTo(width * 0.85f, height * 0.54f, width * 0.85f, height * 0.6f, width * 0.75f, height * 0.62f)
            // chin
            cubicTo(width * 0.8f, height * 0.7f, width * 0.78f, height * 0.75f, width * 0.7f, height * 0.78f)
            // neck
            lineTo(width * 0.68f, height * 0.95f)
            lineTo(width * 0.45f, height * 0.95f)
            // back of head/neck
            cubicTo(width * 0.45f, height * 0.7f, width * 0.45f, height * 0.35f, width * 0.55f, height * 0.1f)
        }

        // Draw left profile in a semi-transparent black
        drawPath(
            path = leftPath,
            color = Color.Black.copy(alpha = 0.15f)
        )
        // Draw right profile in a semi-transparent black
        drawPath(
            path = rightPath,
            color = Color.Black.copy(alpha = 0.15f)
        )

        // Draw left eye
        val leftEyeCenter = Offset(width * 0.36f, height * 0.38f)
        drawCircle(
            color = Color.Black,
            radius = 16.dp.toPx(),
            center = leftEyeCenter
        )
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = leftEyeCenter
        )
        drawCircle(
            color = Color.Black,
            radius = 6.dp.toPx(),
            center = leftEyeCenter
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = leftEyeCenter - Offset(2.dp.toPx(), 2.dp.toPx())
        )

        // Draw right eye
        val rightEyeCenter = Offset(width * 0.64f, height * 0.38f)
        drawCircle(
            color = Color.Black,
            radius = 16.dp.toPx(),
            center = rightEyeCenter
        )
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = rightEyeCenter
        )
        drawCircle(
            color = Color.Black,
            radius = 6.dp.toPx(),
            center = rightEyeCenter
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = rightEyeCenter - Offset(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun CanaryYellowScreenPortraitPreview() {
    CanaryYellowScreen(
        batteryLevel = 71,
        isCharging = false,
        is24Hour = false,
        showSeconds = true
    )
}
