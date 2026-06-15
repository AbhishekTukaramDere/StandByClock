package com.yourcompany.standby.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yourcompany.standby.MainActivity
import com.yourcompany.standby.service.PomodoroTimerService

object NotificationUtils {
    const val CHANNEL_ID = "pomodoro_channel"
    const val TIMER_NOTIFICATION_ID = 1001
    const val COMPLETION_NOTIFICATION_ID = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pomodoro Timer"
            val descriptionText = "Notifications for Pomodoro focus sessions and breaks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildTimerNotification(
        context: Context,
        type: String, // "FOCUS" or "BREAK"
        remainingMs: Long,
        isRunning: Boolean
    ): Notification {
        val timeStr = TimeUtils.formatDuration(remainingMs)
        val title = if (type == "FOCUS") "Focus Session" else "Break Time"
        val text = if (isRunning) "$title: $timeStr remaining" else "$title: Paused ($timeStr)"

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pause/Resume action
        val playPauseIntent = Intent(context, PomodoroTimerService::class.java).apply {
            action = if (isRunning) PomodoroTimerService.ACTION_PAUSE else PomodoroTimerService.ACTION_RESUME
        }
        val playPausePendingIntent = PendingIntent.getService(
            context,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPauseActionText = if (isRunning) "Pause" else "Resume"

        // Skip action
        val skipIntent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_SKIP
        }
        val skipPendingIntent = PendingIntent.getService(
            context,
            2,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, playPauseActionText, playPausePendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Skip", skipPendingIntent)
            .build()
    }

    fun buildCompletionNotification(
        context: Context,
        completedType: String // "FOCUS" or "BREAK"
    ): Notification {
        val title = if (completedType == "FOCUS") "Focus Completed!" else "Break Completed!"
        val text = if (completedType == "FOCUS") "Time for a break." else "Ready to focus?"

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Start Break" or "Start Focus" action
        val nextIntent = Intent(context, PomodoroTimerService::class.java).apply {
            action = if (completedType == "FOCUS") PomodoroTimerService.ACTION_START_BREAK else PomodoroTimerService.ACTION_START_FOCUS
        }
        val nextPendingIntent = PendingIntent.getService(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextActionText = if (completedType == "FOCUS") "Start Break" else "Start Focus"

        // Dismiss action
        val dismissIntent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_DISMISS_NOTIFICATION
        }
        val dismissPendingIntent = PendingIntent.getService(
            context,
            4,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(android.R.drawable.ic_media_play, nextActionText, nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()
    }
}
