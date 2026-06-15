package com.yourcompany.standby.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.standby.data.local.entity.AppState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuoteEditorSheet(
    visible: Boolean,
    appState: AppState,
    customQuotes: List<String>,
    onAddCustomQuote: (String) -> Unit,
    onDeleteCustomQuote: (String) -> Unit,
    onImportCustomQuote: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    onSave: (quote: String, author: String, is24h: Boolean, showSec: Boolean, size: Int, color: String, bold: Boolean, italic: Boolean, alignment: String, family: String, theme: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        var showCustomQuotesManager by remember { mutableStateOf(false) }
        var quoteText by remember { mutableStateOf(appState.quoteText) }
        var authorText by remember { mutableStateOf(appState.quoteAuthor) }
        var is24HourFormat by remember { mutableStateOf(appState.is24HourFormat) }
        var showSecondsState by remember { mutableStateOf(appState.showSeconds) }

        // Customization states
        var fontSizeSp by remember { mutableStateOf(appState.quoteFontSize) }
        var quoteColorHex by remember { mutableStateOf(appState.quoteColorHex) }
        var quoteIsBold by remember { mutableStateOf(appState.quoteIsBold) }
        var quoteIsItalic by remember { mutableStateOf(appState.quoteIsItalic) }
        var quoteAlignment by remember { mutableStateOf(appState.quoteAlignment) }
        var quoteFontFamily by remember { mutableStateOf(appState.quoteFontFamily) }
        var themeMode by remember { mutableStateOf(appState.themeMode) }

        val colorsList = listOf(
            "#FFFFFF", "#BDBDBD", "#FFD54F", "#4DB6AC",
            "#FF8A65", "#7E57C2", "#E91E63", "#CDDC39",
            "#FF0000", "#FF5252", "#2196F3", "#000000"
        )

        val fontFamilies = listOf("Sans", "Mono", "Serif", "Digital", "Rounded")
        val themeModes = listOf("SYSTEM", "DARK", "LIGHT")

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A1A),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Quote Board",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Live Preview
                Text(text = "Live Preview", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A2A2A))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val previewColor = try {
                        Color(android.graphics.Color.parseColor(quoteColorHex))
                    } catch (e: Exception) {
                        Color.White
                    }
                    val previewAlign = when (quoteAlignment.uppercase()) {
                        "LEFT" -> TextAlign.Start
                        "RIGHT" -> TextAlign.End
                        else -> TextAlign.Center
                    }
                    val previewFontFamily = when (quoteFontFamily) {
                        "Mono" -> FontFamily.Monospace
                        "Serif" -> FontFamily.Serif
                        "Digital" -> FontFamily.Default
                        "Rounded" -> FontFamily.Default
                        else -> FontFamily.SansSerif
                    }
                    val previewWeight = if (quoteIsBold) FontWeight.Bold else FontWeight.Normal
                    val previewStyle = if (quoteIsItalic) FontStyle.Italic else FontStyle.Normal

                    Text(
                        text = if (quoteText.isNotEmpty()) "\"$quoteText\"" else "\"Peace begins with a smile.\"",
                        color = previewColor,
                        fontSize = fontSizeSp.sp,
                        fontWeight = previewWeight,
                        fontStyle = previewStyle,
                        fontFamily = previewFontFamily,
                        textAlign = previewAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Text Inputs
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { if (it.length <= 300) quoteText = it },
                    label = { Text("Quote Text (Max 300)", color = Color.White.copy(alpha = 0.6f)) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B00),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                OutlinedTextField(
                    value = authorText,
                    onValueChange = { if (it.length <= 50) authorText = it },
                    label = { Text("Author (Max 50)", color = Color.White.copy(alpha = 0.6f)) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B00),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Customization: Font Size
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Font Size", color = Color.White, fontSize = 14.sp)
                        Text(text = "$fontSizeSp sp", color = Color(0xFFFF6B00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = fontSizeSp.toFloat(),
                        onValueChange = { fontSizeSp = it.toInt() },
                        valueRange = 12f..32f
                    )
                }

                // Customization: Text Color (12 swatches)
                Column {
                    Text(text = "Text Color", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = 6,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorsList.forEach { hex ->
                            val isSelected = hex.equals(quoteColorHex, ignoreCase = true)
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { quoteColorHex = hex }
                            )
                        }
                    }
                }

                // Customization: Styles (Bold, Italic) & Alignment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconToggleButton(
                            checked = quoteIsBold,
                            onCheckedChange = { quoteIsBold = it }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = "Bold",
                                tint = if (quoteIsBold) Color(0xFFFF6B00) else Color.White
                            )
                        }
                        IconToggleButton(
                            checked = quoteIsItalic,
                            onCheckedChange = { quoteIsItalic = it }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatItalic,
                                contentDescription = "Italic",
                                tint = if (quoteIsItalic) Color(0xFFFF6B00) else Color.White
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { quoteAlignment = "LEFT" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft,
                                contentDescription = "Align Left",
                                tint = if (quoteAlignment == "LEFT") Color(0xFFFF6B00) else Color.White
                            )
                        }
                        IconButton(onClick = { quoteAlignment = "CENTER" }) {
                            Icon(
                                imageVector = Icons.Default.FormatAlignCenter,
                                contentDescription = "Align Center",
                                tint = if (quoteAlignment == "CENTER") Color(0xFFFF6B00) else Color.White
                            )
                        }
                        IconButton(onClick = { quoteAlignment = "RIGHT" }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatAlignRight,
                                contentDescription = "Align Right",
                                tint = if (quoteAlignment == "RIGHT") Color(0xFFFF6B00) else Color.White
                            )
                        }
                    }
                }

                // Customization: Font Family
                Column {
                    Text(text = "Font Family", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fontFamilies.forEach { family ->
                            val isSelected = quoteFontFamily == family
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFFF6B00) else Color(0xFF2A2A2A))
                                    .clickable { quoteFontFamily = family }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = family,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Customization: Theme Mode
                Column {
                    Text(text = "App Theme Mode", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeModes.forEach { mode ->
                            val isSelected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFFF6B00) else Color(0xFF2A2A2A))
                                    .clickable { themeMode = mode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Preferences Section
                Text(text = "System Preferences", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "24-Hour Time Format", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = is24HourFormat,
                        onCheckedChange = { is24HourFormat = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF6B00)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Show Seconds", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = showSecondsState,
                        onCheckedChange = { showSecondsState = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF6B00)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showCustomQuotesManager = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Text("Manage Custom Inspiration Quotes", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSave(
                            quoteText, authorText, is24HourFormat, showSecondsState,
                            fontSizeSp, quoteColorHex, quoteIsBold, quoteIsItalic,
                            quoteAlignment, quoteFontFamily, themeMode
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showCustomQuotesManager) {
            CustomQuotesManagerSheet(
                visible = showCustomQuotesManager,
                customQuotes = customQuotes,
                onDismiss = { showCustomQuotesManager = false },
                onAddQuote = onAddCustomQuote,
                onDeleteQuote = onDeleteCustomQuote,
                onImportQuotes = onImportCustomQuote
            )
        }
    }
}

@Preview
@Composable
fun QuoteEditorSheetPreview() {
    QuoteEditorSheet(
        visible = true,
        appState = AppState(),
        customQuotes = emptyList(),
        onAddCustomQuote = {},
        onDeleteCustomQuote = {},
        onImportCustomQuote = {},
        onDismiss = {},
        onSave = { _, _, _, _, _, _, _, _, _, _, _ -> }
    )
}
