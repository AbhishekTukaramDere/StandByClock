package com.yourcompany.standby.ui.components

import android.app.Activity
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.standby.util.PermissionUtils

@Composable
fun NotificationPermissionCard(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onPermissionResult: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Render only on Android 13+ (API >= 33) where runtime permission is required
    val isApi33OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    AnimatedVisibility(
        visible = visible && isApi33OrAbove,
        exit = fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable(enabled = true, onClick = {}),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Orange left accent border (4dp, #FF6B00)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(130.dp)
                        .background(Color(0xFFFF6B00))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Close button in top-right
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Notifications icon with orange tint
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFFFF6B00),
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Enable Notifications",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Required for Pomodoro timer alerts and persistent timer display.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (activity != null) {
                                        PermissionUtils.requestNotificationPermission(activity)
                                    }
                                    onPermissionResult?.invoke()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B00),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(
                                    text = "Allow",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun NotificationPermissionCardPreview() {
    NotificationPermissionCard(
        visible = true,
        onDismiss = {}
    )
}
