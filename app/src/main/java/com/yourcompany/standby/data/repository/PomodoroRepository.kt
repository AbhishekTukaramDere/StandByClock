package com.yourcompany.standby.data.repository

import com.yourcompany.standby.data.local.PomodoroSessionDao
import com.yourcompany.standby.data.local.entity.PomodoroSession
import com.yourcompany.standby.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    suspend fun insertSession(session: PomodoroSession) {
        pomodoroSessionDao.insert(session)
    }

    fun getTodaySessions(): Flow<List<PomodoroSession>> {
        val startOfDay = TimeUtils.getStartOfDay()
        return pomodoroSessionDao.getTodaySessions(startOfDay)
    }

    fun getTodayStats(): Flow<TodayStats> {
        return getTodaySessions().map { sessions ->
            val count = sessions.size
            val totalMinutes = sessions.sumOf { it.durationMinutes }
            TodayStats(count, totalMinutes)
        }
    }
}

data class TodayStats(
    val count: Int,
    val totalMinutes: Int
)
