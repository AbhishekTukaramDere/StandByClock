package com.yourcompany.standby.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yourcompany.standby.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSession)

    @Query("SELECT * FROM pomodoro_sessions WHERE completedAt >= :startOfDay ORDER BY completedAt DESC")
    fun getTodaySessions(startOfDay: Long): Flow<List<PomodoroSession>>
}
