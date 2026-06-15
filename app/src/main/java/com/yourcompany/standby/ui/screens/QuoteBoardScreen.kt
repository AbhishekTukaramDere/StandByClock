package com.yourcompany.standby.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.standby.data.local.entity.AppState
import com.yourcompany.standby.ui.components.QuoteEditorSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val SparkleIcon = ImageVector.Builder(
    name = "Sparkle",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
        moveTo(12f, 2f)
        quadTo(12f, 12f, 2f, 12f)
        quadTo(12f, 12f, 12f, 22f)
        quadTo(12f, 12f, 22f, 12f)
        quadTo(12f, 12f, 12f, 2f)
        close()
    }
}.build()

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QuoteBoardScreen(
    appState: AppState,
    customQuotes: List<String>,
    onAddCustomQuote: (String) -> Unit,
    onDeleteCustomQuote: (String) -> Unit,
    onImportCustomQuotes: (List<String>) -> Unit,
    onSaveQuote: (
        quote: String,
        author: String,
        is24h: Boolean,
        showSec: Boolean,
        fontSize: Int,
        colorHex: String,
        isBold: Boolean,
        isItalic: Boolean,
        alignment: String,
        fontFamily: String,
        themeMode: String
    ) -> Unit,
    onInspireMe: () -> String,
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    var lastInspireTapTime by remember { mutableStateOf(0L) }

    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val coroutineScope = rememberCoroutineScope()

    // format ticker respecting 24h & seconds
    val timeFormat = if (appState.showSeconds) {
        if (appState.is24HourFormat) "HHmmss" else "hhmmss"
    } else {
        if (appState.is24HourFormat) "HHmm" else "hhmm"
    }
    val timeRaw = currentTime.format(DateTimeFormatter.ofPattern(timeFormat))
    val amPmStr = if (!appState.is24HourFormat) {
        currentTime.format(DateTimeFormatter.ofPattern("a"))
    } else ""

    // Idle timer for settings cog
    var showSettingsCog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2000)
        showSettingsCog = true
    }

    var showEditorSheet by remember { mutableStateOf(false) }

    // Text typing states
    var displayedQuoteText by remember { mutableStateOf(appState.quoteText) }
    var displayedQuoteAuthor by remember { mutableStateOf(appState.quoteAuthor) }
    var isTyping by remember { mutableStateOf(false) }
    var isTypingFinished by remember { mutableStateOf(true) }

    // Sync state externally
    LaunchedEffect(appState.quoteText, appState.quoteAuthor) {
        if (!isTyping) {
            displayedQuoteText = appState.quoteText
            displayedQuoteAuthor = appState.quoteAuthor
            isTypingFinished = true
        }
    }

    // Fade alphas
    var quoteAlpha by remember { mutableStateOf(1f) }
    var authorAlpha by remember { mutableStateOf(1f) }
    var quoteFadeDuration by remember { mutableStateOf(200) }

    val animatedQuoteAlpha by animateFloatAsState(
        targetValue = quoteAlpha,
        animationSpec = tween(durationMillis = quoteFadeDuration),
        label = "QuoteAlpha"
    )
    val animatedAuthorAlpha by animateFloatAsState(
        targetValue = authorAlpha,
        animationSpec = tween(durationMillis = 200),
        label = "AuthorAlpha"
    )

    fun triggerInspireMe() {
        val now = System.currentTimeMillis()
        if (now - lastInspireTapTime < 1000) return
        lastInspireTapTime = now
        coroutineScope.launch {
            isTyping = true
            isTypingFinished = false
            
            // 1. Fade out old quote & author (200ms)
            quoteFadeDuration = 200
            quoteAlpha = 0f
            authorAlpha = 0f
            delay(200)

            // 2. Load the random quote
            val newQuote = onInspireMe()
            displayedQuoteText = ""
            displayedQuoteAuthor = ""

            // 3. Fade in text container (300ms)
            quoteFadeDuration = 300
            quoteAlpha = 1f

            // 4. Type character by character (50ms per character)
            for (i in 1..newQuote.length) {
                displayedQuoteText = newQuote.substring(0, i)
                delay(50)
            }

            // 5. Author appears after finished
            isTypingFinished = true
            displayedQuoteAuthor = "" // Inspirational quotes have no author
            authorAlpha = 1f
            
            isTyping = false

            // Save new quote to AppState immediately
            onSaveQuote(
                newQuote,
                "",
                appState.is24HourFormat,
                appState.showSeconds,
                appState.quoteFontSize,
                appState.quoteColorHex,
                appState.quoteIsBold,
                appState.quoteIsItalic,
                appState.quoteAlignment,
                appState.quoteFontFamily,
                appState.themeMode
            )
        }
    }

    // Resolve Custom Styling
    val quoteColor = try {
        Color(android.graphics.Color.parseColor(appState.quoteColorHex))
    } catch (e: Exception) {
        Color.White
    }
    val quoteWeight = if (appState.quoteIsBold) FontWeight.Bold else FontWeight.Normal
    val quoteStyle = if (appState.quoteIsItalic) FontStyle.Italic else FontStyle.Normal
    val quoteAlign = when (appState.quoteAlignment.uppercase()) {
        "LEFT" -> TextAlign.Start
        "RIGHT" -> TextAlign.End
        else -> TextAlign.Center
    }
    val quoteFontFamily = when (appState.quoteFontFamily) {
        "Mono" -> FontFamily.Monospace
        "Serif" -> FontFamily.Serif
        "Digital" -> FontFamily.Default
        "Rounded" -> FontFamily.Default
        else -> FontFamily.SansSerif
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLandscape) {
            // Landscape Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                TickerString(timeRaw = timeRaw, amPmStr = amPmStr)

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .combinedClickable(
                            onLongClick = { if (!isTyping) showEditorSheet = true },
                            onClick = { if (!isTyping) showEditorSheet = true }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (displayedQuoteText.isNotEmpty()) "\"$displayedQuoteText\"" else "",
                        color = quoteColor.copy(alpha = animatedQuoteAlpha),
                        fontSize = appState.quoteFontSize.sp,
                        fontWeight = quoteWeight,
                        fontStyle = quoteStyle,
                        fontFamily = quoteFontFamily,
                        textAlign = quoteAlign,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isTypingFinished && displayedQuoteAuthor.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "— $displayedQuoteAuthor",
                            color = Color.White.copy(alpha = 0.6f * animatedAuthorAlpha),
                            fontSize = 14.sp
                        )
                    }
                }

                // Inspire Me Button (48dp outline circle, sparkles icon)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(enabled = !isTyping) { triggerInspireMe() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SparkleIcon,
                        contentDescription = "Inspire Me",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                TickerString(timeRaw = timeRaw, amPmStr = amPmStr)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A2A2A))
                        .combinedClickable(
                            onLongClick = { if (!isTyping) showEditorSheet = true },
                            onClick = { if (!isTyping) showEditorSheet = true }
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (displayedQuoteText.isNotEmpty()) "\"$displayedQuoteText\"" else "",
                            color = quoteColor.copy(alpha = animatedQuoteAlpha),
                            fontSize = appState.quoteFontSize.sp,
                            fontWeight = quoteWeight,
                            fontStyle = quoteStyle,
                            fontFamily = quoteFontFamily,
                            textAlign = quoteAlign,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isTypingFinished && displayedQuoteAuthor.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "— $displayedQuoteAuthor",
                                color = Color.White.copy(alpha = 0.6f * animatedAuthorAlpha),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Inspire Me Button (48dp outline circle, sparkles icon)
                Box(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(enabled = !isTyping) { triggerInspireMe() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SparkleIcon,
                        contentDescription = "Inspire Me",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Settings Cog
        LaunchedEffect(showSettingsCog) {
            if (!showSettingsCog) {
                delay(2000)
                showSettingsCog = true
            }
        }

        AnimatedVisibility(
            visible = showSettingsCog && !isTyping,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
        ) {
            IconButton(
                onClick = { showEditorSheet = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Slide up editor bottom sheet
        QuoteEditorSheet(
            visible = showEditorSheet,
            appState = appState,
            customQuotes = customQuotes,
            onAddCustomQuote = onAddCustomQuote,
            onDeleteCustomQuote = onDeleteCustomQuote,
            onImportCustomQuote = onImportCustomQuotes,
            onDismiss = { showEditorSheet = false },
            onSave = { newQuote, newAuthor, new24h, newSec, fontSize, colorHex, isBold, isItalic, alignment, fontFamily, themeMode ->
                showEditorSheet = false
                onSaveQuote(newQuote, newAuthor, new24h, newSec, fontSize, colorHex, isBold, isItalic, alignment, fontFamily, themeMode)
            }
        )
    }
}

@Composable
fun TickerString(timeRaw: String, amPmStr: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        timeRaw.forEach { char ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1A1A1A))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = char.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        if (amPmStr.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFF6B00))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = amPmStr,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}

@Composable
private fun UtilityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}



@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun QuoteBoardScreenPortraitPreview() {
    QuoteBoardScreen(
        appState = AppState(),
        customQuotes = emptyList(),
        onAddCustomQuote = {},
        onDeleteCustomQuote = {},
        onImportCustomQuotes = {},
        onSaveQuote = { _, _, _, _, _, _, _, _, _, _, _ -> },
        onInspireMe = { "This is a preview quote." }
    )
}
