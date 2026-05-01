package com.stepcounter.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.stepcounter.app.R
import com.stepcounter.app.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    // Массив мотивационных сообщений
    private val motivationMessages = listOf(
        "Начни шагать!",
        "Пошли разомнемся!",
        "Еще немного — и цель близка!",
        "Шаг за шагом к рекорду!",
        "Вставай и двигайся!"
    )

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val stepsChannel = NotificationChannel(
            CHANNEL_STEPS,
            context.getString(R.string.steps_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.steps_channel_desc)
        }
        val goalChannel = NotificationChannel(
            CHANNEL_GOAL,
            context.getString(R.string.goal_reached_title),
            NotificationManager.IMPORTANCE_HIGH,
        )
        val motivationChannel = NotificationChannel(
            CHANNEL_MOTIVATION,
            context.getString(R.string.motivation_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.motivation_channel_desc)
        }
        manager.createNotificationChannel(stepsChannel)
        manager.createNotificationChannel(goalChannel)
        manager.createNotificationChannel(motivationChannel)
    }

    fun buildForegroundNotification(): Notification {
        ensureChannels()
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(context, CHANNEL_STEPS)
            .setContentTitle(context.getString(R.string.foreground_notification_title))
            .setContentText(context.getString(R.string.foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun showGoalReachedNotification() {
        ensureChannels()
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_GOAL)
            .setContentTitle(context.getString(R.string.goal_reached_title))
            .setContentText(context.getString(R.string.goal_reached_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(GOAL_NOTIFICATION_ID, notification)
    }
    
    /**
     * Показывает мотивационное уведомление со случайным текстом.
     */
    fun showMotivationNotification() {
        ensureChannels()
        val randomMessage = motivationMessages.random()
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(randomMessage)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(MOTIVATION_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_STEPS = "steps_foreground"
        const val CHANNEL_GOAL = "steps_goal"
        const val CHANNEL_MOTIVATION = "steps_motivation"
        const val FOREGROUND_NOTIFICATION_ID = 42
        const val GOAL_NOTIFICATION_ID = 43
        const val MOTIVATION_NOTIFICATION_ID = 44
    }
}
