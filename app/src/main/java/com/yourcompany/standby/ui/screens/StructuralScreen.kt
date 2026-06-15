package com.yourcompany.standby.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
fun StructuralScreen(
    batteryLevel: Int,
    isCharging: Boolean,
    is24Hour: Boolean,
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val hoursStr = currentTime.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH" else "hh"))
    val minutesStr = currentTime.format(DateTimeFormatter.ofPattern("mm"))
    val amPmStr = currentTime.format(DateTimeFormatter.ofPattern("a"))

    val timeLabel = if (is24Hour) {
        "${hoursStr}:${minutesStr}"
    } else {
        "${hoursStr}:${minutesStr} $amPmStr"
    }

    val dayOfMonth = currentTime.format(DateTimeFormatter.ofPattern("d"))
    val dayOfWeek = currentTime.format(DateTimeFormatter.ofPattern("EEEE")).uppercase()
    val footerText = "$dayOfMonth $dayOfWeek"

    val batteryDotColor = if (isCharging) Color(0xFF4CAF50) else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isLandscape) Color.Black else Color(0xFF8FA68E))
    ) {
        if (isLandscape) {
            // Landscape Layout: Horizontal ribbon
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Solid grey wide banner spanning horizontally
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF8FA68E))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stretched digits side-by-side
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.graphicsLayer {
                                scaleY = 1.5f
                            }
                        ) {
                            Text(
                                text = "$hoursStr:$minutesStr",
                                color = Color.White,
                                fontSize = 96.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Structural text stacked to the right
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏰ $timeLabel",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.width(60.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔋 $batteryLevel%",
                                    color = Color.White,
                                    fontSize = 14.sp,
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Borderless Footer
                Text(
                    text = footerText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        } else {
            // Portrait Layout: Vertical Stack
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hours (Stretched)
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleY = 1.5f
                        }
                    ) {
                        Text(
                            text = hoursStr,
                            color = Color.White,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // Structural Belt (razor-thin horizontal space)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 36.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏰ $timeLabel",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "—",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
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

                    // Minutes (Stretched)
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleY = 1.5f
                        }
                    ) {
                        Text(
                            text = minutesStr,
                            color = Color.White,
                            fontSize = 120.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                // Borderless Footer
                Text(
                    text = footerText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun StructuralScreenPortraitPreview() {
    StructuralScreen(batteryLevel = 71, isCharging = false, is24Hour = false)
}
