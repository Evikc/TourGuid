package com.stepcounter.app.model

data class Attraction(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null
)

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrackSegment(
    val points: List<LocationPoint>,
    val totalDistance: Float = 0f
)
