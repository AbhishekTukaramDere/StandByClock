package com.yourcompany.standby.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CircularTimer(
    progress: Float, // 0.0f to 1.0f
    centerText: String,
    size: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    trackColor: Color = Color.White.copy(alpha = 0.2f),
    progressColor: Color = Color.White,
    fontSize: TextUnit = 48.sp,
    textColor: Color = Color.White
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            
            // Draw background track
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokeWidthPx) / 2,
                style = Stroke(width = strokeWidthPx)
            )

            // Draw progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx)
            )
        }

        // Center text display
        Text(
            text = centerText,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
