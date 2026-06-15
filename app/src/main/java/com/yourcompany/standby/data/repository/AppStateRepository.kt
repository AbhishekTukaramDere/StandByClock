package com.yourcompany.standby.data.repository

import com.yourcompany.standby.data.local.AppStateDao
import com.yourcompany.standby.data.local.entity.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateRepository @Inject constructor(
    private val appStateDao: AppStateDao
) {
    // Flow of AppState, ensures a default row exists
    val appStateFlow: Flow<AppState?> = appStateDao.getAppStateFlow()
        .onStart {
            ensureDefaultAppState()
        }
        .flowOn(Dispatchers.IO)

    suspend fun getAppState(): AppState = withContext(Dispatchers.IO) {
        ensureDefaultAppState()
        appStateDao.getAppState() ?: AppState()
    }

    suspend fun updateAppState(appState: AppState) = withContext(Dispatchers.IO) {
        appStateDao.update(appState)
    }

    private suspend fun ensureDefaultAppState() {
        withContext(Dispatchers.IO) {
            if (appStateDao.getAppState() == null) {
                appStateDao.insert(AppState(id = 1))
            }
        }
    }
}
