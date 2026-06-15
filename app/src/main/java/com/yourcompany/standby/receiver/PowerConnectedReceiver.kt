package com.yourcompany.standby.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yourcompany.standby.MainActivity
import com.yourcompany.standby.data.local.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PowerConnectedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_POWER_CONNECTED) {
            CoroutineScope(Dispatchers.Main).launch {
                val autoStart = dataStoreManager.autoStartCharging.first()
                if (autoStart) {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("LAUNCH_FULLSCREEN", true)
                    }
                    context.startActivity(launchIntent)
                }
            }
        }
    }
}
