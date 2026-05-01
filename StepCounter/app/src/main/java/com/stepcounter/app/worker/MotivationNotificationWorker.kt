package com.stepcounter.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stepcounter.app.data.StepsRepository
import com.stepcounter.app.service.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * Worker для отправки мотивационных уведомлений каждые 10 минут.
 * Уведомление не отправляется, если цель уже достигнута за сегодня.
 */
@HiltWorker
class MotivationNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: StepsRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            // Проверяем, достигнута ли цель сегодня
            val todaySteps = repository.getTodayStepsSnapshot()
            val goal = repository.getDailyGoal()
            
            if (todaySteps >= goal) {
                // Цель достигнута — не отправляем мотивационное уведомление
                return Result.success()
            }
            
            // Проверяем время (8:00 - 22:00)
            val currentHour = java.time.LocalTime.now().hour
            if (currentHour < 8 || currentHour >= 22) {
                // Вне рабочего времени — не отправляем
                return Result.success()
            }
            
            // Отправляем мотивационное уведомление
            notificationHelper.showMotivationNotification()
            
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
