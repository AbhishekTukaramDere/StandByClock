package com.yourcompany.standby

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StandByApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Clean up old shared cache files on startup (max age 7 days)
        com.yourcompany.standby.util.ImageUtils.cleanupOldCacheFiles(this)
    }
}
