package com.stepcounter.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationPoint::class],
    version = 1,
    exportSchema = false,
)
abstract class TracksDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
