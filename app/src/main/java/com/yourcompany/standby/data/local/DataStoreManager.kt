package com.yourcompany.standby.data.local

import android.content.Context
import android.text.format.DateFormat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val BACKGROUND_COLOR = stringPreferencesKey("background_color") // default "#000000"
        val TEXT_COLOR = stringPreferencesKey("text_color") // default "#FFFFFF"
        val CLOCK_FONT = stringPreferencesKey("clock_font") // default "DEFAULT"
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour") // default follows system
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds") // default false
        val SHOW_DATE = booleanPreferencesKey("show_date") // default true
        val SHOW_DAY_OF_WEEK = booleanPreferencesKey("show_day_of_week") // default false
        val AUTO_ROTATE = booleanPreferencesKey("auto_rotate") // default true
        val NIGHT_MODE = booleanPreferencesKey("night_mode") // default false
        val AUTO_START_CHARGING = booleanPreferencesKey("auto_start_charging") // default false
        val SCREEN_SAVER_TIMEOUT = intPreferencesKey("screen_saver_timeout") // default 5 (minutes)
        val FOCUS_DURATION = intPreferencesKey("focus_duration") // default 25
        val BREAK_DURATION = intPreferencesKey("break_duration") // default 5
        val AUTO_START_BREAKS = booleanPreferencesKey("auto_start_breaks") // default false
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled") // default true
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled") // default true

        // Timer Recovery State
        val TIMER_TYPE = stringPreferencesKey("timer_type") // default "FOCUS"
        val TIMER_REMAINING_MS = longPreferencesKey("timer_remaining_ms") // default 0L
        val TIMER_TOTAL_MS = longPreferencesKey("timer_total_ms") // default 0L
        val TIMER_IS_RUNNING = booleanPreferencesKey("timer_is_running") // default false
        val TIMER_START_TIMESTAMP = longPreferencesKey("timer_start_timestamp") // default 0L

        val AUTO_START_CARD_DISMISSED = booleanPreferencesKey("auto_start_card_dismissed") // default false
        val NOTIFICATION_CARD_DISMISSED = booleanPreferencesKey("notification_card_dismissed") // default false
        val POMODORO_CURRENT_STEP = intPreferencesKey("pomodoro_current_step") // default 0
    }

    val backgroundColor: Flow<String> = dataStore.data.map { preferences ->
        preferences[BACKGROUND_COLOR] ?: "#000000"
    }

    val textColor: Flow<String> = dataStore.data.map { preferences ->
        preferences[TEXT_COLOR] ?: "#FFFFFF"
    }

    val clockFont: Flow<String> = dataStore.data.map { preferences ->
        preferences[CLOCK_FONT] ?: "DEFAULT"
    }

    val is24Hour: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_24_HOUR] ?: DateFormat.is24HourFormat(context)
    }

    val showSeconds: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_SECONDS] ?: false
    }

    val showDate: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_DATE] ?: true
    }

    val showDayOfWeek: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_DAY_OF_WEEK] ?: false
    }

    val autoRotate: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_ROTATE] ?: true
    }

    val nightMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NIGHT_MODE] ?: false
    }

    val autoStartCharging: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_START_CHARGING] ?: false
    }

    val screenSaverTimeout: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SCREEN_SAVER_TIMEOUT] ?: 5
    }

    val focusDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FOCUS_DURATION] ?: 25
    }

    val breakDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[BREAK_DURATION] ?: 5
    }

    val autoStartBreaks: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_START_BREAKS] ?: false
    }

    val soundEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED] ?: true
    }

    val timerType: Flow<String> = dataStore.data.map { preferences ->
        preferences[TIMER_TYPE] ?: "FOCUS"
    }

    val timerRemainingMs: Flow<Long> = dataStore.data.map { preferences ->
        preferences[TIMER_REMAINING_MS] ?: 0L
    }

    val timerTotalMs: Flow<Long> = dataStore.data.map { preferences ->
        preferences[TIMER_TOTAL_MS] ?: 0L
    }

    val timerIsRunning: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[TIMER_IS_RUNNING] ?: false
    }

    val timerStartTimestamp: Flow<Long> = dataStore.data.map { preferences ->
        preferences[TIMER_START_TIMESTAMP] ?: 0L
    }

    suspend fun setBackgroundColor(value: String) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_COLOR] = value
        }
    }

    suspend fun setTextColor(value: String) {
        dataStore.edit { preferences ->
            preferences[TEXT_COLOR] = value
        }
    }

    suspend fun setClockFont(value: String) {
        dataStore.edit { preferences ->
            preferences[CLOCK_FONT] = value
        }
    }

    suspend fun setIs24Hour(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_24_HOUR] = value
        }
    }

    suspend fun setShowSeconds(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_SECONDS] = value
        }
    }

    suspend fun setShowDate(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DATE] = value
        }
    }

    suspend fun setShowDayOfWeek(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DAY_OF_WEEK] = value
        }
    }

    suspend fun setAutoRotate(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_ROTATE] = value
        }
    }

    suspend fun setNightMode(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[NIGHT_MODE] = value
        }
    }

    suspend fun setAutoStartCharging(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_START_CHARGING] = value
        }
    }

    suspend fun setScreenSaverTimeout(value: Int) {
        dataStore.edit { preferences ->
            preferences[SCREEN_SAVER_TIMEOUT] = value
        }
    }

    suspend fun setFocusDuration(value: Int) {
        dataStore.edit { preferences ->
            preferences[FOCUS_DURATION] = value
        }
    }

    suspend fun setBreakDuration(value: Int) {
        dataStore.edit { preferences ->
            preferences[BREAK_DURATION] = value
        }
    }

    suspend fun setAutoStartBreaks(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_START_BREAKS] = value
        }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = value
        }
    }

    suspend fun setVibrationEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = value
        }
    }

    suspend fun setTimerType(value: String) {
        dataStore.edit { preferences ->
            preferences[TIMER_TYPE] = value
        }
    }

    suspend fun setTimerRemainingMs(value: Long) {
        dataStore.edit { preferences ->
            preferences[TIMER_REMAINING_MS] = value
        }
    }

    suspend fun setTimerTotalMs(value: Long) {
        dataStore.edit { preferences ->
            preferences[TIMER_TOTAL_MS] = value
        }
    }

    suspend fun setTimerIsRunning(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[TIMER_IS_RUNNING] = value
        }
    }

    suspend fun setTimerStartTimestamp(value: Long) {
        dataStore.edit { preferences ->
            preferences[TIMER_START_TIMESTAMP] = value
        }
    }

    val autoStartCardDismissed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_START_CARD_DISMISSED] ?: false
    }

    val notificationCardDismissed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_CARD_DISMISSED] ?: false
    }

    suspend fun setAutoStartCardDismissed(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_START_CARD_DISMISSED] = value
        }
    }

    suspend fun setNotificationCardDismissed(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_CARD_DISMISSED] = value
        }
    }

    val pomodoroCurrentStep: Flow<Int> = dataStore.data.map { preferences ->
        preferences[POMODORO_CURRENT_STEP] ?: 1
    }

    suspend fun setPomodoroCurrentStep(value: Int) {
        dataStore.edit { preferences ->
            preferences[POMODORO_CURRENT_STEP] = value
        }
    }
}
