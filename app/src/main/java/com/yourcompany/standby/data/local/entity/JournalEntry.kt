package com.yourcompany.standby.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val quoteText: String,
    val quoteAuthor: String,
    val fontSize: Int = 16,
    val colorHex: String = "#FFFFFF",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: String = "CENTER",
    val startTime: String? = null, // format: "HH:mm"
    val endTime: String? = null,   // format: "HH:mm"
    val displayOrder: Int = 0,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
