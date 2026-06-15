package com.yourcompany.standby.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.standby.data.local.DataStoreManager
import com.yourcompany.standby.data.local.JournalEntryDao
import com.yourcompany.standby.data.local.entity.AppState
import com.yourcompany.standby.data.local.entity.JournalEntry
import com.yourcompany.standby.data.repository.AppStateRepository
import com.yourcompany.standby.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appStateRepository: AppStateRepository,
    private val journalEntryDao: JournalEntryDao,
    private val quoteRepository: QuoteRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val appState: StateFlow<AppState?> = appStateRepository.appStateFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val journalEntries: StateFlow<List<JournalEntry>> = journalEntryDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nightMode: StateFlow<Boolean> = dataStoreManager.nightMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeJournalEntry: StateFlow<JournalEntry?> = kotlinx.coroutines.flow.combine(
        appState,
        journalEntries
    ) { state, entries ->
        val activeId = state?.activeJournalEntryId
        entries.firstOrNull { it.id == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    // Centralized clock ticker (emits every second)
    private val _currentTime = MutableStateFlow(LocalDateTime.now())
    val currentTime: StateFlow<LocalDateTime> = _currentTime.asStateFlow()

    // Shared flow for sending UI events (like Limit Reached alerts)
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents = _uiEvents.asSharedFlow()

    val customInspirationalQuotes: StateFlow<List<String>> = quoteRepository.customQuotes

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    _batteryLevel.value = (level * 100) / scale
                }

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
    }

    private var lastCycleTime = 0L
    private var lastScheduledId: String? = null

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Read initial battery status
        val batteryStatus: Intent? = context.registerReceiver(null, filter)
        batteryStatus?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                _batteryLevel.value = (level * 100) / scale
            }
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            _isCharging.value = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }

        // Clock Ticker loop
        viewModelScope.launch {
            while (true) {
                _currentTime.value = LocalDateTime.now()
                delay(1000)
            }
        }

        // activeJournalEntry is now reactively combined from appState and journalEntries

        // Start scheduling engine loop
        startSchedulingLoop()
    }

    private fun startSchedulingLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val state = appStateRepository.getAppState()
                    val entries = journalEntryDao.getEnabled()
                    if (entries.isNotEmpty()) {
                        when (state.journalDisplayMode) {
                            "SCHEDULED" -> {
                                val now = LocalTime.now()
                                val active = getActiveEntry(now, entries)
                                if (active != null) {
                                    if (active.id != lastScheduledId) {
                                        lastScheduledId = active.id
                                        if (active.id != state.activeJournalEntryId) {
                                            appStateRepository.updateAppState(state.copy(activeJournalEntryId = active.id))
                                        }
                                    }
                                } else {
                                    val fallback = entries.firstOrNull { it.isEnabled }
                                    if (fallback != null && fallback.id != lastScheduledId) {
                                        lastScheduledId = fallback.id
                                        if (fallback.id != state.activeJournalEntryId) {
                                            appStateRepository.updateAppState(state.copy(activeJournalEntryId = fallback.id))
                                        }
                                    }
                                }
                            }
                            "RANDOM", "SEQUENTIAL" -> {
                                lastScheduledId = null
                                val intervalMs = state.cycleIntervalMinutes * 60 * 1000L
                                val nowMs = System.currentTimeMillis()
                                if (lastCycleTime == 0L || (nowMs - lastCycleTime >= intervalMs) || state.activeJournalEntryId == null) {
                                    val nextEntry = if (state.journalDisplayMode == "RANDOM") {
                                        entries.random()
                                    } else {
                                        val currentIndex = entries.indexOfFirst { it.id == state.activeJournalEntryId }
                                        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % entries.size else 0
                                        entries[nextIndex]
                                    }
                                    if (nextEntry.id != state.activeJournalEntryId) {
                                        appStateRepository.updateAppState(state.copy(activeJournalEntryId = nextEntry.id))
                                    }
                                    lastCycleTime = nowMs
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(15000) // check every 15 seconds
            }
        }
    }

    // Resolves active entry with overnight wrap-around and specificity resolution (narrowest range)
    fun getActiveEntry(now: LocalTime, entries: List<JournalEntry>): JournalEntry? {
        val enabled = entries.filter { it.isEnabled }
        if (enabled.isEmpty()) return null

        val matching = enabled.filter { entry ->
            if (!entry.startTime.isNullOrEmpty() && !entry.endTime.isNullOrEmpty()) {
                try {
                    val start = LocalTime.parse(entry.startTime)
                    val end = LocalTime.parse(entry.endTime)
                    if (start.isBefore(end) || start == end) {
                        !now.isBefore(start) && !now.isAfter(end)
                    } else {
                        // crosses midnight
                        !now.isBefore(start) || !now.isAfter(end)
                    }
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }

        if (matching.isNotEmpty()) {
            // Pick narrowest range (most specific duration)
            return matching.minByOrNull { entry ->
                try {
                    val start = LocalTime.parse(entry.startTime)
                    val end = LocalTime.parse(entry.endTime)
                    if (start.isBefore(end) || start == end) {
                        java.time.Duration.between(start, end).toMinutes()
                    } else {
                        val toMidnight = java.time.Duration.between(start, LocalTime.MAX).toMinutes() + 1
                        val fromMidnight = java.time.Duration.between(LocalTime.MIN, end).toMinutes()
                        toMidnight + fromMidnight
                    }
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }
        }

        // Fallback to first enabled entry
        return enabled.firstOrNull()
    }

    fun updateScreenIndex(index: Int) {
        viewModelScope.launch {
            val current = appStateRepository.getAppState()
            if (current.currentScreenIndex != index) {
                appStateRepository.updateAppState(current.copy(currentScreenIndex = index))
            }
        }
    }

    fun saveQuoteBoardState(
        quote: String,
        author: String,
        is24h: Boolean,
        showSec: Boolean,
        fontSize: Int,
        colorHex: String,
        isBold: Boolean,
        isItalic: Boolean,
        alignment: String,
        fontFamily: String,
        themeMode: String
    ) {
        viewModelScope.launch {
            val current = appStateRepository.getAppState()
            appStateRepository.updateAppState(
                current.copy(
                    quoteText = quote,
                    quoteAuthor = author,
                    is24HourFormat = is24h,
                    showSeconds = showSec,
                    quoteFontSize = fontSize,
                    quoteColorHex = colorHex,
                    quoteIsBold = isBold,
                    quoteIsItalic = isItalic,
                    quoteAlignment = alignment,
                    quoteFontFamily = fontFamily,
                    themeMode = themeMode
                )
            )
        }
    }

    fun savePhotoJournalImage(imagePath: String?) {
        viewModelScope.launch {
            val appStateVal = appStateRepository.getAppState()
            val activeId = appStateVal.activeJournalEntryId

            // Solid Color validation
            val finalPath = if (imagePath?.startsWith("solid:") == true) {
                val hex = imagePath.removePrefix("solid:")
                val regex = Regex("^#[0-9A-Fa-f]{6}$")
                if (regex.matches(hex)) {
                    imagePath
                } else {
                    Log.w("SolidColorValidation", "Invalid solid color hex: $imagePath. Falling back to #FF0000.")
                    "solid:#FF0000"
                }
            } else {
                imagePath
            }

            if (activeId != null) {
                val entry = journalEntryDao.getById(activeId)
                if (entry != null && finalPath != null) {
                    journalEntryDao.update(entry.copy(imagePath = finalPath))
                }
            } else {
                val newEntry = JournalEntry(
                    imagePath = finalPath ?: "solid:#FF0000",
                    quoteText = "Peace begins with a smile.",
                    quoteAuthor = "Mother Teresa"
                )
                journalEntryDao.insert(newEntry)
                appStateRepository.updateAppState(appStateVal.copy(activeJournalEntryId = newEntry.id))
            }
        }
    }

    fun savePhotoJournalQuote(
        quote: String,
        author: String,
        fontSize: Int,
        colorHex: String,
        isBold: Boolean,
        isItalic: Boolean,
        alignment: String
    ) {
        viewModelScope.launch {
            val appStateVal = appStateRepository.getAppState()
            val activeId = appStateVal.activeJournalEntryId
            if (activeId != null) {
                val entry = journalEntryDao.getById(activeId)
                if (entry != null) {
                    journalEntryDao.update(
                        entry.copy(
                            quoteText = quote,
                            quoteAuthor = author,
                            fontSize = fontSize,
                            colorHex = colorHex,
                            isBold = isBold,
                            isItalic = isItalic,
                            alignment = alignment
                        )
                    )
                }
            } else {
                val newEntry = JournalEntry(
                    imagePath = "solid:#FF0000",
                    quoteText = quote,
                    quoteAuthor = author,
                    fontSize = fontSize,
                    colorHex = colorHex,
                    isBold = isBold,
                    isItalic = isItalic,
                    alignment = alignment
                )
                journalEntryDao.insert(newEntry)
                appStateRepository.updateAppState(appStateVal.copy(activeJournalEntryId = newEntry.id))
            }
        }
    }

    // CRUD for Journal Entries
    fun addJournalEntry(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            journalEntryDao.insert(entry)
            val appStateVal = appStateRepository.getAppState()
            if (appStateVal.activeJournalEntryId == null) {
                appStateRepository.updateAppState(appStateVal.copy(activeJournalEntryId = entry.id))
            }
        }
    }

    fun updateJournalEntry(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            journalEntryDao.update(entry)
        }
    }

    fun deleteJournalEntry(entry: JournalEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            journalEntryDao.delete(entry)
            val appStateVal = appStateRepository.getAppState()
            if (entry.id == appStateVal.activeJournalEntryId) {
                val remaining = journalEntryDao.getEnabled()
                val nextId = remaining.firstOrNull()?.id
                appStateRepository.updateAppState(appStateVal.copy(activeJournalEntryId = nextId))
            }
        }
    }

    fun updateJournalDisplaySettings(mode: String, intervalMinutes: Int) {
        viewModelScope.launch {
            val current = appStateRepository.getAppState()
            appStateRepository.updateAppState(
                current.copy(
                    journalDisplayMode = mode,
                    cycleIntervalMinutes = intervalMinutes
                )
            )
        }
    }

    fun setActiveJournalEntry(id: String?) {
        viewModelScope.launch {
            val current = appStateRepository.getAppState()
            appStateRepository.updateAppState(current.copy(activeJournalEntryId = id))
        }
    }

    fun cycleToNextJournalEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val state = appStateRepository.getAppState()
                val entries = journalEntryDao.getEnabled()
                if (entries.isNotEmpty()) {
                    val nextEntry = if (state.journalDisplayMode == "RANDOM") {
                        if (entries.size > 1) {
                            var picked = entries.random()
                            while (picked.id == state.activeJournalEntryId) {
                                picked = entries.random()
                            }
                            picked
                        } else {
                            entries.first()
                        }
                    } else {
                        val currentIndex = entries.indexOfFirst { it.id == state.activeJournalEntryId }
                        val nextIndex = if (currentIndex != -1) (currentIndex + 1) % entries.size else 0
                        entries[nextIndex]
                    }
                    if (nextEntry.id != state.activeJournalEntryId) {
                        appStateRepository.updateAppState(state.copy(activeJournalEntryId = nextEntry.id))
                    }
                    lastCycleTime = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Fail silently
        }
    }

    // QuoteRepository Delegation
    fun addCustomInspirationalQuote(quote: String) {
        viewModelScope.launch {
            val success = quoteRepository.addCustomQuote(quote)
            if (!success) {
                _uiEvents.emit("Limit reached (Max 500 quotes)")
            }
        }
    }

    fun deleteCustomInspirationalQuote(quote: String) {
        viewModelScope.launch {
            quoteRepository.deleteCustomQuote(quote)
        }
    }

    fun importCustomInspirationalQuotes(quotes: List<String>) {
        viewModelScope.launch {
            val (imported, limitReached) = quoteRepository.importCustomQuotes(quotes)
            if (limitReached) {
                _uiEvents.emit("Limit reached (Max 500 quotes). Imported $imported quotes.")
            } else if (imported > 0) {
                _uiEvents.emit("Successfully imported $imported quotes.")
            }
        }
    }

    fun getRandomInspirationalQuote(): String {
        return quoteRepository.getRandomQuote()
    }
}
