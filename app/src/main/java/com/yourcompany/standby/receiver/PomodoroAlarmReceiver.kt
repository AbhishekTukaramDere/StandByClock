package com.yourcompany.standby.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.yourcompany.standby.service.PomodoroTimerService

class PomodoroAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PomodoroAlarmReceiver", "Alarm fired! Action: ${intent.action}")
        if (intent.action == PomodoroTimerService.ACTION_ALARM_TRIGGERED) {
            val serviceIntent = Intent(context, PomodoroTimerService::class.java).apply {
                action = PomodoroTimerService.ACTION_ALARM_TRIGGERED
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e("PomodoroAlarmReceiver", "Failed to start PomodoroTimerService on alarm trigger", e)
            }
        }
    }
}
