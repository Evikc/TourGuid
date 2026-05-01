package com.stepcounter.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stepcounter.app.service.LocationTrackerService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, LocationTrackerService::class.java)
            try {
                context.startForegroundService(serviceIntent)
            } catch (e: Exception) {
                // Обработка ошибки запуска сервиса
            }
        }
    }
}
