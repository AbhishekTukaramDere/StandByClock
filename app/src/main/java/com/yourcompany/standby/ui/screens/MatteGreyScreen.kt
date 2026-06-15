package com.yourcompany.standby.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
fun MatteGreyScreen(
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
            .background(Color(0xFF8FA68E)) // Slate Grey
    ) {
        if (isLandscape) {
            // Landscape Layout: Side-by-side separated by massive dot
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = hoursStr,
                        color = Color.White,
                        fontSize = 150.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(36.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )

                    Spacer(modifier = Modifier.width(36.dp))

                    Text(
                        text = minutesStr,
                        color = Color.White,
                        fontSize = 150.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }

                if (showSeconds) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = secondsStr,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
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
                    // Hours
                    Text(
                        text = hoursStr,
                        color = Color.White,
                        fontSize = 130.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )

                    // Oversized white dot (40dp)
                    Box(
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )

                    // Minutes
                    Text(
                        text = minutesStr,
                        color = Color.White,
                        fontSize = 130.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }

                if (showSeconds) {
                    Text(
                        text = secondsStr,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun MatteGreyScreenPortraitPreview() {
    MatteGreyScreen(is24Hour = false, showSeconds = true)
}
