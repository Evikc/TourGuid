package com.stepcounter.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stepcounter.app.service.StepCounterService

/**
 * После [android.intent.action.BOOT_COMPLETED] перезапускаем foreground-сервис
 * (регистрации Health Services не переживают перезагрузку).
 */
class StartStepCounterWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        StepCounterService.start(applicationContext)
        return Result.success()
    }
}
