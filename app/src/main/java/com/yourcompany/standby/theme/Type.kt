package com.yourcompany.standby.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define the 5 Font Families
val DefaultFontFamily = FontFamily.SansSerif
val MonospaceFontFamily = FontFamily.Monospace
val SerifFontFamily = FontFamily.Serif

// Digital Font Fallback using system monospace typeface
val DigitalFontFamily = FontFamily(androidx.compose.ui.text.font.Typeface(Typeface.MONOSPACE))

// Rounded Font Fallback (uses system sans-serif as we don't have rounded custom font files yet)
val RoundedFontFamily = FontFamily.SansSerif

fun getFontFamily(fontName: String): FontFamily {
    return when (fontName.uppercase()) {
        "MONOSPACE" -> MonospaceFontFamily
        "SERIF" -> SerifFontFamily
        "DIGITAL" -> DigitalFontFamily
        "ROUNDED" -> RoundedFontFamily
        else -> DefaultFontFamily
    }
}

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
