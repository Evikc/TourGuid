package com.stepcounter.app.data

import com.stepcounter.app.data.local.DailyStepsDao
import com.stepcounter.app.data.local.DailyStepsEntity
import com.stepcounter.app.data.local.StepsPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepsRepository @Inject constructor(
    private val dao: DailyStepsDao,
    private val prefs: StepsPreferences,
) {
    private fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

    fun observeTodaySteps(): Flow<Int> =
        dao.observeDay(todayEpochDay()).map { it?.stepCount ?: 0 }

    suspend fun getTodayStepsSnapshot(): Int =
        observeTodaySteps().first()

    fun observeDailyGoal(): Flow<Int> = prefs.dailyGoalSteps

    fun observeLastSevenDays(): Flow<List<DailyStepsEntity>> {
        val end = todayEpochDay()
        val start = end - 6
        return dao.observeRange(start, end)
    }

    suspend fun upsertTodaySteps(rawSteps: Long) {
        val count = rawSteps.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val goal = prefs.dailyGoalSteps.first()
        dao.upsert(
            DailyStepsEntity(
                dateEpochDay = todayEpochDay(),
                stepCount = count,
                goalSteps = goal,
            ),
        )
    }

    suspend fun upsertStepsForDay(epochDay: Long, rawSteps: Long) {
        val count = rawSteps.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        val goal = prefs.dailyGoalSteps.first()
        dao.upsert(
            DailyStepsEntity(
                dateEpochDay = epochDay,
                stepCount = count,
                goalSteps = goal,
            ),
        )
    }

    suspend fun getDailyGoal(): Int = prefs.dailyGoalSteps.first()

    suspend fun setDailyGoal(value: Int) {
        prefs.setDailyGoal(value)
    }

    suspend fun getOrInitStepCounterBaseForToday(todayEpochDay: Long, current: Float): Float {
        val existing = prefs.stepCounterBaseForDay(todayEpochDay).first()
        if (existing != null) return existing
        prefs.setStepCounterBaseForDay(todayEpochDay, current)
        return current
    }

    suspend fun markGoalNotificationSentForDay(todayEpochDay: Long) {
        prefs.setLastGoalNotificationDay(todayEpochDay)
    }

    suspend fun shouldSendGoalNotification(todayEpochDay: Long): Boolean {
        val last = prefs.lastGoalNotificationEpochDay.first()
        return last != todayEpochDay
    }
}
