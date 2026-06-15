package com.yourcompany.standby.ui.screens

import android.app.Activity
import android.os.Build
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.standby.ui.components.NotificationPermissionCard
import com.yourcompany.standby.ui.viewmodel.PomodoroViewModel
import com.yourcompany.standby.util.PermissionUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun PomodoroScreen(
    modifier: Modifier = Modifier,
    viewModel: PomodoroViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val remainingTimeMs by viewModel.remainingTimeMs.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val timerType by viewModel.timerType.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()

    val minutes = (remainingTimeMs / 1000) / 60
    val seconds = (remainingTimeMs / 1000) % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLandscape) {
            // Landscape Layout: Split-Screen
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 50%
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    FocusDial(
                        timeText = timeFormatted,
                        statusText = timerType,
                        isRunning = timerState == "RUNNING",
                        onPlayPauseClick = {
                            if (timerState == "RUNNING") viewModel.pauseTimer() else viewModel.startTimer()
                        }
                    )
                }

                // Right 50%
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    PomodoroTitleSection(completedCount = (currentStep - 1) / 2)

                    TimelineCapsule(currentStep = currentStep)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResetButton(onClick = { viewModel.resetTimer() })
                        SkipButton(onClick = { viewModel.skipTimer() })
                    }

                    AmbientClock()
                }
            }
        } else {
            // Portrait Layout: Vertical Stack
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top margin spacer for dots
                Spacer(modifier = Modifier.height(24.dp))

                PomodoroTitleSection(completedCount = (currentStep - 1) / 2)

                FocusDial(
                    timeText = timeFormatted,
                    statusText = timerType,
                    isRunning = timerState == "RUNNING",
                    onPlayPauseClick = {
                        if (timerState == "RUNNING") viewModel.pauseTimer() else viewModel.startTimer()
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimelineCapsule(currentStep = currentStep)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ResetButton(onClick = { viewModel.resetTimer() })
                        SkipButton(onClick = { viewModel.skipTimer() })
                    }

                    AmbientClock()
                }

                // Permission Warning cards at very bottom (dismissible)
                val isNotificationGranted = PermissionUtils.isNotificationPermissionGranted(context)
                val isNotificationCardDismissed by viewModel.notificationCardDismissed.collectAsState()
                Column(modifier = Modifier.fillMaxWidth()) {
                    NotificationPermissionCard(
                        visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationGranted && !isNotificationCardDismissed,
                        onDismiss = { viewModel.dismissNotificationCard() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroTitleSection(completedCount: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "POMODORO",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.2.sp * 14 // 0.2em
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .background(Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Completed: $completedCount/4 pomodoros",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FocusDial(
    timeText: String,
    statusText: String,
    isRunning: Boolean,
    onPlayPauseClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(240.dp)
            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Crescent moon at top of circle
        Text(
            text = "🌙",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeText,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.15.sp * 14
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Play/Pause Action Ring Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White, CircleShape)
                    .clickable { onPlayPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineCapsule(currentStep: Int) {
    val steps = listOf("🌙", "☕", "🌙", "☕", "🌙", "☕", "🌙", "☕", "🏃")
    val infiniteTransition = rememberInfiniteTransition(label = "TimelinePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, icon ->
                val stepNum = index + 1
                val isCompleted = stepNum < currentStep
                val isCurrent = stepNum == currentStep
                val alpha = if (isCompleted) 1.0f else if (isCurrent) 1.0f else 0.4f

                Box(
                    modifier = Modifier
                        .scale(if (isCurrent) pulseScale else 1.0f)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = icon,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = alpha)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$currentStep/9",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ResetButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A2A2A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SkipButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF2A2A2A))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Skip", color = Color.White, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.SkipNext,
            contentDescription = "Skip",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun AmbientClock() {
    val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    Box(
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun PomodoroScreenPortraitPreview() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            PomodoroTitleSection(completedCount = 2)
            FocusDial(timeText = "25:00", statusText = "FOCUS", isRunning = false, onPlayPauseClick = {})
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimelineCapsule(currentStep = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    ResetButton(onClick = {})
                    SkipButton(onClick = {})
                }
                Spacer(modifier = Modifier.height(8.dp))
                AmbientClock()
            }
        }
    }
}
