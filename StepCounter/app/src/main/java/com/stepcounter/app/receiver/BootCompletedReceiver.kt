package com.stepcounter.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.stepcounter.app.worker.StartStepCounterWorker

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val work = OneTimeWorkRequestBuilder<StartStepCounterWorker>().build()
        WorkManager.getInstance(context).enqueue(work)
    }
}
