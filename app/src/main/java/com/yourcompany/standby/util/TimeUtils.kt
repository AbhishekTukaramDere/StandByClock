package com.yourcompany.standby.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("EEEE, d MMM", Locale.US)
        return sdf.format(date)
    }
}
