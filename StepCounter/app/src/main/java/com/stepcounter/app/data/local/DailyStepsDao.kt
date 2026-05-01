package com.stepcounter.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyStepsEntity)

    @Query("SELECT * FROM daily_steps WHERE dateEpochDay = :epochDay LIMIT 1")
    fun observeDay(epochDay: Long): Flow<DailyStepsEntity?>

    @Query(
        "SELECT * FROM daily_steps WHERE dateEpochDay >= :fromEpochDay AND dateEpochDay <= :toEpochDay ORDER BY dateEpochDay ASC",
    )
    fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DailyStepsEntity>>
}
