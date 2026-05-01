package com.stepcounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: LocationPoint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<LocationPoint>)

    @Query("SELECT * FROM location_points WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    fun observeDay(epochDay: Long): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE dateEpochDay >= :fromEpochDay AND dateEpochDay <= :toEpochDay ORDER BY timestamp ASC")
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE dateEpochDay = :epochDay ORDER BY timestamp ASC")
    suspend fun getDayPoints(epochDay: Long): List<LocationPoint>

    @Query("DELETE FROM location_points WHERE dateEpochDay < :beforeEpochDay")
    suspend fun deleteOlderThan(beforeEpochDay: Long)

    @Query("SELECT COUNT(*) FROM location_points WHERE dateEpochDay = :epochDay")
    suspend fun getCountForDay(epochDay: Long): Int
}
