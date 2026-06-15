package com.yourcompany.standby.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.standby.data.local.DataStoreManager
import com.yourcompany.standby.data.repository.PomodoroRepository
import com.yourcompany.standby.service.PomodoroTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PomodoroRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val focusDuration: StateFlow<Int> = dataStoreManager.focusDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val breakDuration: StateFlow<Int> = dataStoreManager.breakDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val notificationCardDismissed: StateFlow<Boolean> = dataStoreManager.notificationCardDismissed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _remainingTimeMs = MutableStateFlow(25 * 60 * 1000L)
    val remainingTimeMs: StateFlow<Long> = _remainingTimeMs.asStateFlow()

    private val _totalTimeMs = MutableStateFlow(25 * 60 * 1000L)
    val totalTimeMs: StateFlow<Long> = _totalTimeMs.asStateFlow()

    private val _timerType = MutableStateFlow("FOCUS") // "FOCUS", "BREAK", "LONG_BREAK"
    val timerType: StateFlow<String> = _timerType.asStateFlow()

    private val _timerState = MutableStateFlow("IDLE") // "IDLE", "RUNNING", "PAUSED"
    val timerState: StateFlow<String> = _timerState.asStateFlow()

    // 1-indexed step in the 9-step timeline (1 to 9)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    PomodoroTimerService.ACTION_TIMER_TICK -> {
                        _remainingTimeMs.value = it.getLongExtra(PomodoroTimerService.EXTRA_REMAINING_MS, 0L)
                        _totalTimeMs.value = it.getLongExtra(PomodoroTimerService.EXTRA_TOTAL_MS, 0L)
                        _timerType.value = it.getStringExtra(PomodoroTimerService.EXTRA_TYPE) ?: "FOCUS"
                        val isRunning = it.getBooleanExtra(PomodoroTimerService.EXTRA_IS_RUNNING, false)
                        _timerState.value = if (isRunning) "RUNNING" else "PAUSED"
                    }
                    PomodoroTimerService.ACTION_TIMER_COMPLETED -> {
                        _timerState.value = "IDLE"
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(PomodoroTimerService.ACTION_TIMER_TICK)
            addAction(PomodoroTimerService.ACTION_TIMER_COMPLETED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(timerReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(timerReceiver, filter)
        }

        // Collect current step and update local state
        viewModelScope.launch {
            dataStoreManager.pomodoroCurrentStep.collect { step ->
                _currentStep.value = step
                if (_timerState.value == "IDLE" || _timerState.value == "PAUSED") {
                    val duration = getDurationForStep(step)
                    _remainingTimeMs.value = duration * 60 * 1000L
                    _totalTimeMs.value = duration * 60 * 1000L
                    _timerType.value = getTimerTypeForStep(step)
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.timerIsRunning.collect { isRunning ->
                _timerState.value = if (isRunning) "RUNNING" else {
                    if (_timerState.value == "RUNNING") "IDLE" else _timerState.value
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.timerRemainingMs.collect { remaining ->
                if (_timerState.value == "RUNNING") {
                    _remainingTimeMs.value = remaining
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.timerTotalMs.collect { total ->
                if (_timerState.value == "RUNNING") {
                    _totalTimeMs.value = total
                }
            }
        }

        viewModelScope.launch {
            dataStoreManager.timerType.collect { type ->
                if (_timerState.value == "RUNNING") {
                    _timerType.value = type
                }
            }
        }
    }

    private fun startServiceSafe(intent: Intent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is android.app.ForegroundServiceStartNotAllowedException) {
                android.util.Log.e("PomodoroViewModel", "Foreground service start not allowed", e)
            } else {
                e.printStackTrace()
            }
        }
    }

    fun startTimer() {
        val type = getTimerTypeForStep(_currentStep.value)
        val durationMinutes = getDurationForStep(_currentStep.value)
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_START
            putExtra(PomodoroTimerService.EXTRA_TYPE, type)
            putExtra(PomodoroTimerService.EXTRA_DURATION_MINUTES, durationMinutes)
        }
        startServiceSafe(intent)
        _timerState.value = "RUNNING"
    }

    fun pauseTimer() {
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_PAUSE
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _timerState.value = "PAUSED"
    }

    fun resumeTimer() {
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_RESUME
        }
        startServiceSafe(intent)
        _timerState.value = "RUNNING"
    }

    fun resetTimer() {
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_STOP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _timerState.value = "IDLE"
        _currentStep.value = 1
        val duration = getDurationForStep(1)
        _remainingTimeMs.value = duration * 60 * 1000L
        _totalTimeMs.value = duration * 60 * 1000L
    }

    fun skipTimer() {
        val intent = Intent(context, PomodoroTimerService::class.java).apply {
            action = PomodoroTimerService.ACTION_SKIP
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismissNotificationCard() {
        viewModelScope.launch {
            dataStoreManager.setNotificationCardDismissed(true)
        }
    }

    private fun advanceStep() {
        val nextStep = if (_currentStep.value >= 9) 1 else _currentStep.value + 1
        _currentStep.value = nextStep
        _timerState.value = "IDLE"
        val duration = getDurationForStep(nextStep)
        _remainingTimeMs.value = duration * 60 * 1000L
        _totalTimeMs.value = duration * 60 * 1000L
        _timerType.value = getTimerTypeForStep(nextStep)
    }

    private fun getTimerTypeForStep(step: Int): String {
        return when (step) {
            1, 3, 5, 7 -> "FOCUS"
            2, 4, 6, 8 -> "BREAK"
            9 -> "LONG_BREAK"
            else -> "FOCUS"
        }
    }

    private fun getDurationForStep(step: Int): Int {
        return when (step) {
            1, 3, 5, 7 -> focusDuration.value
            2, 4, 6, 8 -> breakDuration.value
            9 -> 15 // Long break is 15 minutes by default
            else -> focusDuration.value
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(timerReceiver)
        } catch (e: Exception) {
            // Fail silently
        }
    }
}
