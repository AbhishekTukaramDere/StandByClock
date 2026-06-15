package com.yourcompany.standby.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PageDotIndicator(
    currentPage: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val size by animateDpAsState(
                targetValue = if (isActive) 8.dp else 6.dp,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "DotSize"
            )
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1.0f else 0.4f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "DotAlpha"
            )

            // Outer Box wrapper to ensure 48dp x 48dp touch target
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // disable ripple to keep it clean
                        onClick = { onPageSelected(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner visual dot
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = alpha))
                )
            }
        }
    }
}

@Preview
@Composable
fun PageDotIndicatorPreview() {
    PageDotIndicator(
        currentPage = 2,
        pageCount = 7,
        onPageSelected = {}
    )
}
