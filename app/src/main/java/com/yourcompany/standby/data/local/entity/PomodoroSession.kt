package com.yourcompany.standby.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // e.g., "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    val durationMinutes: Int,
    val completedAt: Long,
    val wasAutoStarted: Boolean
)
