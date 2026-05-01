package com.stepcounter.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Точка трека (геолокация).
 * [dateEpochDay] — день записи ([java.time.LocalDate.toEpochDay]).
 * [timestamp] — точное время записи в миллисекундах.
 * [latitude], [longitude] — координаты.
 * [accuracy] — точность в метрах.
 * [altitude] — высота над уровнем моря (метры).
 * [speed] — скорость (м/с).
 */
@Entity(
    tableName = "location_points",
    indices = [Index("dateEpochDay"), Index("timestamp")]
)
data class LocationPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float
)
