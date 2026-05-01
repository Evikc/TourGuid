package com.stepcounter.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DailyStepsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StepsDatabase : RoomDatabase() {
    abstract fun dailyStepsDao(): DailyStepsDao
}
