package com.yourcompany.standby

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yourcompany.standby.data.local.StandByDatabase
import com.yourcompany.standby.theme.StandByClockTheme
import com.yourcompany.standby.ui.navigation.StandByNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseLazy: dagger.Lazy<StandByDatabase>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-rotate lock to landscape (either landscape or reverse landscape)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide system bars for immersive fullscreen clock display
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        var migrationFailed = false
        try {
            // Force database open and run migrations
            databaseLazy.get().openHelper.writableDatabase
        } catch (e: Exception) {
            Log.e("MainActivity", "Database migration failed during onCreate", e)
            migrationFailed = true
        }

        if (migrationFailed) {
            showMigrationFailedDialog()
        } else {
            setContent {
                StandByClockTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        StandByNavHost()
                    }
                }
            }
        }
    }

    private fun showMigrationFailedDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Database Update Failed")
            .setMessage("StandBy Clock was unable to migrate your database to the new version. Please reinstall the application.")
            .setPositiveButton("Close App") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}
