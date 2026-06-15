package com.yourcompany.standby.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yourcompany.standby.data.local.entity.AppState
import com.yourcompany.standby.data.local.entity.JournalEntry
import com.yourcompany.standby.data.local.entity.PomodoroSession

@Database(
    entities = [AppState::class, PomodoroSession::class, JournalEntry::class],
    version = 3,
    exportSchema = false
)
abstract class StandByDatabase : RoomDatabase() {
    abstract fun appStateDao(): AppStateDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun journalEntryDao(): JournalEntryDao
}
