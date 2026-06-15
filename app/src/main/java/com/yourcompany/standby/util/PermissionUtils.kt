package com.yourcompany.standby.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(activity: Activity, requestCode: Int = 101) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
    }

    fun isAutoStartPermissionAvailable(): Boolean {
        val manufacturer = Build.MANUFACTURER.uppercase()
        val oems = listOf("XIAOMI", "REDMI", "OPPO", "REALME", "VIVO", "SAMSUNG", "ONEPLUS", "HUAWEI")
        return oems.any { manufacturer.contains(it) }
    }

    fun openAutoStartSettings(context: Context) {
        val manufacturer = Build.MANUFACTURER.uppercase()
        val intents = mutableListOf<Intent>()

        when {
            manufacturer.contains("XIAOMI") || manufacturer.contains("REDMI") -> {
                intents.add(Intent().setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"))
            }
            manufacturer.contains("OPPO") || manufacturer.contains("REALME") -> {
                intents.add(Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"))
                intents.add(Intent().setClassName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"))
                intents.add(Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"))
            }
            manufacturer.contains("VIVO") -> {
                intents.add(Intent().setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"))
                intents.add(Intent().setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"))
            }
            manufacturer.contains("SAMSUNG") -> {
                intents.add(Intent().setClassName("com.samsung.android.lool", "com.samsung.android.lool.appmanager.AppMgrActivity"))
            }
            manufacturer.contains("ONEPLUS") -> {
                intents.add(Intent().setClassName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
            }
            manufacturer.contains("HUAWEI") -> {
                intents.add(Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"))
            }
        }

        // Try OEM intents in order
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback to next intent
            }
        }

        // Generic Fallback to Application Details Settings
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Final fallback to generic Settings
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return if (pm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    fun requestBatteryOptimizationIgnore(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to battery saver settings if action is not supported or fails
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    // Final fallback to generic settings
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}
