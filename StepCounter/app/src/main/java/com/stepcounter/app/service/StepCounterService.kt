package com.stepcounter.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.stepcounter.app.data.StepsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Foreground-сервис: слушает шаги через [Sensor.TYPE_STEP_COUNTER] (или [Sensor.TYPE_STEP_DETECTOR]).
 *
 * Важно: TYPE_STEP_COUNTER — cumulative с момента последней перезагрузки. Для дневного счётчика
 * держим «базу» (первое значение дня) в DataStore и считаем \(today = current - base\).
 */
@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject lateinit var repository: StepsRepository

    @Inject lateinit var notificationHelper: NotificationHelper

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main.immediate)

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var registered = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannels()
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            notificationHelper.buildForegroundNotification(),
        )
        registerSensors()
        return START_STICKY
    }

    private fun registerSensors() {
        if (registered) return
        val ok = when {
            stepCounterSensor != null ->
                sensorManager.registerListener(
                    this,
                    stepCounterSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                )
            stepDetectorSensor != null ->
                sensorManager.registerListener(
                    this,
                    stepDetectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                )
            else -> false
        }
        registered = ok
        if (!ok) Log.w(TAG, "No step sensors available on this device.")
    }

    override fun onDestroy() {
        if (registered) sensorManager.unregisterListener(this)
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val todayEpochDay = LocalDate.now().toEpochDay()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val current = event.values.firstOrNull() ?: return
                scope.launch(Dispatchers.IO) {
                    val base = repository.getOrInitStepCounterBaseForToday(todayEpochDay, current)
                    val todaySteps = (current - base).coerceAtLeast(0f).toLong()
                    onNewSteps(todayEpochDay, todaySteps)
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                // Обычно event.values[0] == 1.0 на каждый шаг.
                scope.launch(Dispatchers.IO) {
                    val previous = repository.getTodayStepsSnapshot()
                    onNewSteps(todayEpochDay, (previous + 1).toLong())
                }
            }
        }
    }

    private suspend fun onNewSteps(todayEpochDay: Long, steps: Long) {
        val goal = repository.getDailyGoal()
        val previous = repository.getTodayStepsSnapshot()
        repository.upsertStepsForDay(todayEpochDay, steps)
        val crossed = previous < goal && steps >= goal
        if (crossed && repository.shouldSendGoalNotification(todayEpochDay)) {
            repository.markGoalNotificationSentForDay(todayEpochDay)
            notificationHelper.showGoalReachedNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        private const val TAG = "StepCounterService"

        fun start(context: android.content.Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, StepCounterService::class.java),
            )
        }
    }
}
