package com.yourcompany.standby.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

// Safe dummy migration for version 1 to 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Stub migration block to bridge schema version 1 to 2 safely
    }
}

// Migration from version 2 to 3
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create the journal_entries table matching version 3 schema
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `journal_entries` (
                `id` TEXT NOT NULL, 
                `imagePath` TEXT NOT NULL, 
                `quoteText` TEXT NOT NULL, 
                `quoteAuthor` TEXT NOT NULL, 
                `fontSize` INTEGER NOT NULL DEFAULT 16, 
                `colorHex` TEXT NOT NULL DEFAULT '#FFFFFF', 
                `isBold` INTEGER NOT NULL DEFAULT 0, 
                `isItalic` INTEGER NOT NULL DEFAULT 0, 
                `alignment` TEXT NOT NULL DEFAULT 'CENTER', 
                `startTime` TEXT, 
                `endTime` TEXT, 
                `displayOrder` INTEGER NOT NULL DEFAULT 0, 
                `isEnabled` INTEGER NOT NULL DEFAULT 1, 
                `createdAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        // 2. Add new columns to app_state safely if they do not exist
        val appStateColumns = mutableListOf<String>()
        db.query("PRAGMA table_info(app_state)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex != -1) {
                while (cursor.moveToNext()) {
                    appStateColumns.add(cursor.getString(nameIndex))
                }
            }
        }

        if (!appStateColumns.contains("journalDisplayMode")) {
            db.execSQL("ALTER TABLE app_state ADD COLUMN journalDisplayMode TEXT NOT NULL DEFAULT 'SEQUENTIAL'")
        }
        if (!appStateColumns.contains("cycleIntervalMinutes")) {
            db.execSQL("ALTER TABLE app_state ADD COLUMN cycleIntervalMinutes INTEGER NOT NULL DEFAULT 5")
        }
        if (!appStateColumns.contains("activeJournalEntryId")) {
            db.execSQL("ALTER TABLE app_state ADD COLUMN activeJournalEntryId TEXT")
        }

        // 3. Migrate existing quote text/author/style from app_state into a default journal entry
        try {
            val hasQuoteText = appStateColumns.contains("quoteText")
            val hasQuoteAuthor = appStateColumns.contains("quoteAuthor")
            val hasQuoteFontSize = appStateColumns.contains("quoteFontSize")
            val hasQuoteColorHex = appStateColumns.contains("quoteColorHex")
            val hasQuoteIsBold = appStateColumns.contains("quoteIsBold")
            val hasQuoteIsItalic = appStateColumns.contains("quoteIsItalic")
            val hasQuoteAlignment = appStateColumns.contains("quoteAlignment")

            var quoteText = "Peace begins with a smile."
            var quoteAuthor = "Mother Teresa"
            var quoteFontSize = 20
            var quoteColorHex = "#FFFFFF"
            var quoteIsBold = 0
            var quoteIsItalic = 1
            var quoteAlignment = "CENTER"

            val queryCols = mutableListOf<String>()
            if (hasQuoteText) queryCols.add("quoteText")
            if (hasQuoteAuthor) queryCols.add("quoteAuthor")
            if (hasQuoteFontSize) queryCols.add("quoteFontSize")
            if (hasQuoteColorHex) queryCols.add("quoteColorHex")
            if (hasQuoteIsBold) queryCols.add("quoteIsBold")
            if (hasQuoteIsItalic) queryCols.add("quoteIsItalic")
            if (hasQuoteAlignment) queryCols.add("quoteAlignment")

            if (queryCols.isNotEmpty()) {
                val queryStr = queryCols.joinToString(", ")
                db.query("SELECT $queryStr FROM app_state LIMIT 1").use { cursor ->
                    if (cursor.moveToFirst()) {
                        var idx = 0
                        if (hasQuoteText) quoteText = cursor.getString(idx++) ?: quoteText
                        if (hasQuoteAuthor) quoteAuthor = cursor.getString(idx++) ?: quoteAuthor
                        if (hasQuoteFontSize) quoteFontSize = cursor.getInt(idx++)
                        if (hasQuoteColorHex) quoteColorHex = cursor.getString(idx++) ?: quoteColorHex
                        if (hasQuoteIsBold) quoteIsBold = cursor.getInt(idx++)
                        if (hasQuoteIsItalic) quoteIsItalic = cursor.getInt(idx++)
                        if (hasQuoteAlignment) quoteAlignment = cursor.getString(idx) ?: quoteAlignment
                    }
                }
            }

            val entryId = UUID.randomUUID().toString()
            val createdAt = System.currentTimeMillis()

            // Insert default journal entry
            db.execSQL(
                "INSERT INTO journal_entries (id, imagePath, quoteText, quoteAuthor, fontSize, colorHex, isBold, isItalic, alignment, startTime, endTime, displayOrder, isEnabled, createdAt) " +
                        "VALUES (?, 'solid:#FF0000', ?, ?, ?, ?, ?, ?, ?, NULL, NULL, 0, 1, ?)",
                arrayOf(entryId, quoteText, quoteAuthor, quoteFontSize, quoteColorHex, quoteIsBold, quoteIsItalic, quoteAlignment, createdAt)
            )

            // Update app_state activeJournalEntryId to point to this new entry
            db.execSQL("UPDATE app_state SET activeJournalEntryId = ? WHERE id = 1", arrayOf(entryId))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
