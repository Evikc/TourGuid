package com.stepcounter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.stepsDataStore: DataStore<Preferences> by preferencesDataStore("step_counter_prefs")

@Singleton
class StepsPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore get() = context.stepsDataStore

    val dailyGoalSteps: Flow<Int> = dataStore.data.map { prefs ->
        prefs[DAILY_GOAL_KEY] ?: DEFAULT_GOAL
    }

    val lastGoalNotificationEpochDay: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[LAST_GOAL_NOTIFY_DAY_KEY]
    }

    fun stepCounterBaseForDay(epochDay: Long): Flow<Float?> =
        dataStore.data.map { prefs -> prefs[stepCounterBaseKey(epochDay)] }

    suspend fun setDailyGoal(value: Int) {
        dataStore.edit { it[DAILY_GOAL_KEY] = value.coerceAtLeast(1) }
    }

    suspend fun setLastGoalNotificationDay(epochDay: Long) {
        dataStore.edit { it[LAST_GOAL_NOTIFY_DAY_KEY] = epochDay }
    }

    suspend fun setStepCounterBaseForDay(epochDay: Long, base: Float) {
        dataStore.edit { it[stepCounterBaseKey(epochDay)] = base }
    }

    companion object {
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal_steps")
        val LAST_GOAL_NOTIFY_DAY_KEY = longPreferencesKey("last_goal_notify_epoch_day")
        const val DEFAULT_GOAL = 1_000

        private fun stepCounterBaseKey(epochDay: Long) =
            floatPreferencesKey("step_counter_base_$epochDay")
    }
}
