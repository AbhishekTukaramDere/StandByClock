package com.yourcompany.standby.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.yourcompany.standby.data.local.DataStoreManager
import com.yourcompany.standby.data.local.entity.PomodoroSession
import com.yourcompany.standby.data.repository.PomodoroRepository
import com.yourcompany.standby.receiver.PomodoroAlarmReceiver
import com.yourcompany.standby.util.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroTimerService : Service() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @Inject
    lateinit var pomodoroRepository: PomodoroRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var countDownTimer: CountDownTimer? = null

    // State Variables
    private var timerRemainingMs = 0L
    private var timerTotalMs = 0L
    private var timerType = "FOCUS" // "FOCUS" or "BREAK"
    private var timerIsRunning = false
    private var timerStartTimestamp = 0L

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SKIP = "ACTION_SKIP"
        const val ACTION_START_BREAK = "ACTION_START_BREAK"
        const val ACTION_START_FOCUS = "ACTION_START_FOCUS"
        const val ACTION_DISMISS_NOTIFICATION = "ACTION_DISMISS_NOTIFICATION"
        const val ACTION_ALARM_TRIGGERED = "com.yourcompany.standby.ALARM_TRIGGERED"

        const val ACTION_TIMER_TICK = "com.yourcompany.standby.TIMER_TICK"
        const val ACTION_TIMER_COMPLETED = "com.yourcompany.standby.TIMER_COMPLETED"

        const val EXTRA_REMAINING_MS = "EXTRA_REMAINING_MS"
        const val EXTRA_TOTAL_MS = "EXTRA_TOTAL_MS"
        const val EXTRA_TYPE = "EXTRA_TYPE"
        const val EXTRA_IS_RUNNING = "EXTRA_IS_RUNNING"
        const val EXTRA_DURATION_MINUTES = "EXTRA_DURATION_MINUTES"
    }

    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        serviceScope.launch {
            // Check for service recovery on cold startup
            val savedIsRunning = dataStoreManager.timerIsRunning.first()
            if (action == null && savedIsRunning) {
                recoverTimerState()
            }
        }

        when (action) {
            ACTION_START -> {
                val type = intent.getStringExtra(EXTRA_TYPE) ?: "FOCUS"
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 25)
                startNewTimer(type, durationMinutes)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimerService()
            ACTION_SKIP -> skipTimer()
            ACTION_START_BREAK -> {
                serviceScope.launch {
                    val breakMin = dataStoreManager.breakDuration.first()
                    startNewTimer("BREAK", breakMin)
                }
            }
            ACTION_START_FOCUS -> {
                serviceScope.launch {
                    val focusMin = dataStoreManager.focusDuration.first()
                    startNewTimer("FOCUS", focusMin)
                }
            }
            ACTION_DISMISS_NOTIFICATION -> dismissCompletionNotification()
            ACTION_ALARM_TRIGGERED -> handleAlarmTriggered()
        }

        return START_REDELIVER_INTENT
    }

    private fun recoverTimerState() {
        serviceScope.launch {
            if (!com.yourcompany.standby.util.PermissionUtils.isNotificationPermissionGranted(this@PomodoroTimerService)) {
                val showPromptIntent = Intent("com.yourcompany.standby.SHOW_NOTIFICATION_PERMISSION_PROMPT")
                sendBroadcast(showPromptIntent)
                launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        applicationContext,
                        "Please allow notifications to use the timer",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                stopTimerService()
                return@launch
            }
            val type = dataStoreManager.timerType.first()
            val total = dataStoreManager.timerTotalMs.first()
            val startTs = dataStoreManager.timerStartTimestamp.first()

            val elapsedSinceStart = System.currentTimeMillis() - startTs
            val correctedRemaining = total - elapsedSinceStart

            if (correctedRemaining > 0L) {
                timerType = type
                timerRemainingMs = correctedRemaining
                timerTotalMs = correctedRemaining
                timerStartTimestamp = System.currentTimeMillis()
                timerIsRunning = true
                scheduleAlarm(timerRemainingMs)
                runTimer(timerRemainingMs)
            } else {
                // Timer completed while app was killed
                handleTimerCompletion(type, (total / (60 * 1000)).toInt(), wasAutoStarted = true)
                stopTimerService()
            }
        }
    }

    private fun startNewTimer(type: String, durationMinutes: Int) {
        if (!com.yourcompany.standby.util.PermissionUtils.isNotificationPermissionGranted(this)) {
            val showPromptIntent = Intent("com.yourcompany.standby.SHOW_NOTIFICATION_PERMISSION_PROMPT")
            sendBroadcast(showPromptIntent)
            android.widget.Toast.makeText(
                applicationContext,
                "Please allow notifications to use the timer",
                android.widget.Toast.LENGTH_LONG
            ).show()
            stopTimerService()
            return
        }
        countDownTimer?.cancel()
        cancelAlarm()

        timerType = type
        timerTotalMs = durationMinutes * 60 * 1000L
        timerRemainingMs = timerTotalMs
        timerStartTimestamp = System.currentTimeMillis()
        timerIsRunning = true

        saveStateToDataStore()
        scheduleAlarm(timerRemainingMs)
        
        // Start Foreground Service with correct system service type
        val notification = NotificationUtils.buildTimerNotification(this, timerType, timerRemainingMs, timerIsRunning)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NotificationUtils.TIMER_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NotificationUtils.TIMER_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is android.app.ForegroundServiceStartNotAllowedException) {
                Log.e("PomodoroTimerService", "ForegroundServiceStartNotAllowedException caught during startNewTimer", e)
            } else {
                throw e
            }
        }

        runTimer(timerRemainingMs)
    }

    private fun runTimer(ms: Long) {
        countDownTimer = object : CountDownTimer(ms, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = System.currentTimeMillis() - timerStartTimestamp
                timerRemainingMs = (timerTotalMs - elapsed).coerceAtLeast(0L)
                saveStateToDataStore()
                broadcastTick()
                updateNotification()
            }

            override fun onFinish() {
                timerRemainingMs = 0L
                timerIsRunning = false
                saveStateToDataStore()
                broadcastTick()
                cancelAlarm()
                
                val durationMin = (timerTotalMs / (60 * 1000)).toInt()
                handleTimerCompletion(timerType, durationMin, wasAutoStarted = false)
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        cancelAlarm()
        timerIsRunning = false
        saveStateToDataStore()
        broadcastTick()
        updateNotification()
    }

    private fun resumeTimer() {
        timerIsRunning = true
        timerStartTimestamp = System.currentTimeMillis()
        timerTotalMs = timerRemainingMs
        saveStateToDataStore()
        scheduleAlarm(timerRemainingMs)
        runTimer(timerRemainingMs)
    }

    private fun getTimerTypeForStep(step: Int): String {
        return when (step) {
            1, 3, 5, 7 -> "FOCUS"
            2, 4, 6, 8 -> "BREAK"
            9 -> "LONG_BREAK"
            else -> "FOCUS"
        }
    }

    private suspend fun getDurationForStep(step: Int): Int {
        return when (step) {
            1, 3, 5, 7 -> dataStoreManager.focusDuration.first()
            2, 4, 6, 8 -> dataStoreManager.breakDuration.first()
            9 -> 15
            else -> dataStoreManager.focusDuration.first()
        }
    }

    private fun skipTimer() {
        countDownTimer?.cancel()
        cancelAlarm()
        timerRemainingMs = 0L
        timerIsRunning = false
        broadcastTick()

        serviceScope.launch {
            val currentType = timerType
            val durationMin = (timerTotalMs / (60 * 1000)).toInt()

            // Save completed session to DB
            pomodoroRepository.insertSession(
                PomodoroSession(
                    id = UUID.randomUUID().toString(),
                    type = currentType,
                    durationMinutes = durationMin,
                    completedAt = System.currentTimeMillis(),
                    wasAutoStarted = false
                )
            )

            // Advance step
            val currentStep = dataStoreManager.pomodoroCurrentStep.first()
            val nextStep = if (currentStep >= 9) 1 else currentStep + 1
            dataStoreManager.setPomodoroCurrentStep(nextStep)

            val nextType = getTimerTypeForStep(nextStep)
            val nextDuration = getDurationForStep(nextStep)

            // Broadcast completion
            val intent = Intent(ACTION_TIMER_COMPLETED)
            sendBroadcast(intent)

            launch(Dispatchers.Main) {
                startNewTimer(nextType, nextDuration)
            }
        }
    }

    private fun handleTimerCompletion(type: String, durationMinutes: Int, wasAutoStarted: Boolean) {
        // Play notification sound
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
        ringtone?.play()

        // Vibrate
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

        // Save session to Room database
        serviceScope.launch {
            pomodoroRepository.insertSession(
                PomodoroSession(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    durationMinutes = durationMinutes,
                    completedAt = System.currentTimeMillis(),
                    wasAutoStarted = wasAutoStarted
                )
            )

            // Advance step
            val currentStep = dataStoreManager.pomodoroCurrentStep.first()
            val nextStep = if (currentStep >= 9) 1 else currentStep + 1
            dataStoreManager.setPomodoroCurrentStep(nextStep)

            val nextType = getTimerTypeForStep(nextStep)
            val nextDuration = getDurationForStep(nextStep)

            // Broadcast completion
            val intent = Intent(ACTION_TIMER_COMPLETED)
            sendBroadcast(intent)

            // Switch to break automatically if configured
            val autoStart = dataStoreManager.autoStartBreaks.first()
            if (type == "FOCUS" && autoStart) {
                launch(Dispatchers.Main) {
                    startNewTimer(nextType, nextDuration)
                }
            } else {
                // Post completion notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val headsUp = NotificationUtils.buildCompletionNotification(this@PomodoroTimerService, type)
                notificationManager.notify(NotificationUtils.COMPLETION_NOTIFICATION_ID, headsUp)
                
                stopTimerService()
            }
        }
    }

    private fun handleAlarmTriggered() {
        countDownTimer?.cancel()
        serviceScope.launch {
            val type = dataStoreManager.timerType.first()
            val total = dataStoreManager.timerTotalMs.first()
            timerType = type
            timerTotalMs = total
            timerRemainingMs = 0L
            timerIsRunning = false
            saveStateToDataStore()
            broadcastTick()
            
            val durationMin = (total / (60 * 1000)).toInt()
            launch(Dispatchers.Main) {
                handleTimerCompletion(type, durationMin, wasAutoStarted = false)
            }
        }
    }

    private fun broadcastTick() {
        val intent = Intent(ACTION_TIMER_TICK).apply {
            putExtra(EXTRA_REMAINING_MS, timerRemainingMs)
            putExtra(EXTRA_TOTAL_MS, timerTotalMs)
            putExtra(EXTRA_TYPE, timerType)
            putExtra(EXTRA_IS_RUNNING, timerIsRunning)
        }
        sendBroadcast(intent)
    }

    private fun updateNotification() {
        val notification = NotificationUtils.buildTimerNotification(this, timerType, timerRemainingMs, timerIsRunning)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationUtils.TIMER_NOTIFICATION_ID, notification)
    }

    private fun saveStateToDataStore() {
        serviceScope.launch {
            dataStoreManager.setTimerType(timerType)
            dataStoreManager.setTimerRemainingMs(timerRemainingMs)
            dataStoreManager.setTimerTotalMs(timerTotalMs)
            dataStoreManager.setTimerIsRunning(timerIsRunning)
            dataStoreManager.setTimerStartTimestamp(timerStartTimestamp)
        }
    }

    private fun dismissCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationUtils.COMPLETION_NOTIFICATION_ID)
    }

    private fun scheduleAlarm(ms: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, PomodoroAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)

        val triggerTime = System.currentTimeMillis() + ms
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e("PomodoroTimerService", "SecurityException scheduling exact alarm", e)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, PomodoroAlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun stopTimerService() {
        countDownTimer?.cancel()
        cancelAlarm()
        timerIsRunning = false
        saveStateToDataStore()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        countDownTimer?.cancel()
    }
}
