package com.yourcompany.standby.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourcompany.standby.theme.StandByClockTheme
import com.yourcompany.standby.ui.components.LoadingSpinner
import com.yourcompany.standby.ui.components.PageDotIndicator
import com.yourcompany.standby.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainCarouselHost(
    onNavigateToCrop: (String) -> Unit,
    croppedPath: String?,
    onConsumeCropResult: () -> Unit,
    onNavigateToManagement: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val appState by viewModel.appState.collectAsState()
    val activeEntry by viewModel.activeJournalEntry.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val customInspirationalQuotes by viewModel.customInspirationalQuotes.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val nightMode by viewModel.nightMode.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 7 }
    )

    // Sync database state to UI pager on start or database update
    LaunchedEffect(appState?.currentScreenIndex) {
        appState?.currentScreenIndex?.let { dbIndex ->
            if (dbIndex != pagerState.currentPage && dbIndex in 0..6) {
                pagerState.scrollToPage(dbIndex)
            }
        }
    }

    // Sync UI page updates back to database
    LaunchedEffect(pagerState.currentPage) {
        viewModel.updateScreenIndex(pagerState.currentPage)
    }

    // Listen for crop screen result
    LaunchedEffect(croppedPath) {
        if (croppedPath != null) {
            viewModel.savePhotoJournalImage(croppedPath)
            onConsumeCropResult()
        }
    }

    // Determine theme mode dynamically from AppState
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (appState?.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemDark
    }

    StandByClockTheme(darkTheme = darkTheme) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Horizontal Pager with 7 pages, beyondBoundsPageCount = 1
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondBoundsPageCount = 1
            ) { pageIndex ->
                val state = appState
                if (state != null) {
                    when (pageIndex) {
                        0 -> PomodoroScreen()
                        1 -> CanaryYellowScreen(
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            is24Hour = state.is24HourFormat,
                            showSeconds = state.showSeconds,
                            currentTime = currentTime
                        )
                        2 -> AstronautScreen(
                            is24Hour = state.is24HourFormat,
                            showSeconds = state.showSeconds,
                            currentTime = currentTime
                        )
                        3 -> MatteGreyScreen(
                            is24Hour = state.is24HourFormat,
                            showSeconds = state.showSeconds,
                            currentTime = currentTime
                        )
                        4 -> StructuralScreen(
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            is24Hour = state.is24HourFormat,
                            currentTime = currentTime
                        )
                        5 -> QuoteBoardScreen(
                            appState = state,
                            customQuotes = customInspirationalQuotes,
                            onAddCustomQuote = { viewModel.addCustomInspirationalQuote(it) },
                            onDeleteCustomQuote = { viewModel.deleteCustomInspirationalQuote(it) },
                            onImportCustomQuotes = { viewModel.importCustomInspirationalQuotes(it) },
                            onSaveQuote = { quote, author, is24h, showSec, fontSize, color, bold, italic, alignment, family, theme ->
                                viewModel.saveQuoteBoardState(quote, author, is24h, showSec, fontSize, color, bold, italic, alignment, family, theme)
                            },
                            onInspireMe = {
                                viewModel.getRandomInspirationalQuote()
                            },
                            currentTime = currentTime
                        )
                        6 -> PhotoJournalScreen(
                            activeEntry = activeEntry,
                            is24Hour = state.is24HourFormat,
                            onSaveQuote = { quote, author, fontSize, color, bold, italic, alignment ->
                                viewModel.savePhotoJournalQuote(quote, author, fontSize, color, bold, italic, alignment)
                            },
                            onNavigateToManagement = onNavigateToManagement,
                            onImageSelected = { path ->
                                viewModel.savePhotoJournalImage(path)
                            },
                            onNavigateToCrop = onNavigateToCrop,
                            onNextEntry = { viewModel.cycleToNextJournalEntry() },
                            currentTime = currentTime
                        )
                    }
                } else {
                    // Initial load placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSpinner(visible = true)
                    }
                }
            }

            // Pagination dots: anchored at top center (48dp touch target, check isScrollInProgress)
            if (appState != null) {
                PageDotIndicator(
                    currentPage = pagerState.currentPage,
                    pageCount = 7,
                    onPageSelected = { index ->
                        if (!pagerState.isScrollInProgress) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )
            }

            // Loading spinner ONLY during active transition between pages (currentPage != targetPage)
            LoadingSpinner(
                visible = pagerState.currentPage != pagerState.targetPage,
                modifier = Modifier.fillMaxSize()
            )

            // Night Mode Red Tint Overlay (skip on Canary Yellow page index 1)
            if (nightMode && pagerState.currentPage != 1) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.35f))
                )
            }
        }
    }
}
