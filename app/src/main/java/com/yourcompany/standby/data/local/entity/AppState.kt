package com.yourcompany.standby.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppState(
    @PrimaryKey val id: Int = 1,
    val currentScreenIndex: Int = 0,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroShortBreakMinutes: Int = 5,
    val pomodoroLongBreakMinutes: Int = 15,
    val pomodoroSessionsCompleted: Int = 0,
    // Quote Board (Screen F) Customization
    val quoteText: String = "Peace begins with a smile.",
    val quoteAuthor: String = "Mother Teresa",
    val quoteFontSize: Int = 20,
    val quoteColorHex: String = "#FFFFFF",
    val quoteIsBold: Boolean = false,
    val quoteIsItalic: Boolean = true,
    val quoteAlignment: String = "CENTER",
    val quoteFontFamily: String = "Sans", // Sans, Mono, Serif, Digital, Rounded
    // System settings
    val is24HourFormat: Boolean = false,
    val showSeconds: Boolean = true,
    val themeMode: String = "SYSTEM", // "SYSTEM", "DARK", "LIGHT"
    // Photo Journal (Screen G) settings
    val journalDisplayMode: String = "SEQUENTIAL", // "SCHEDULED", "RANDOM", "SEQUENTIAL"
    val cycleIntervalMinutes: Int = 5,
    val activeJournalEntryId: String? = null
)
