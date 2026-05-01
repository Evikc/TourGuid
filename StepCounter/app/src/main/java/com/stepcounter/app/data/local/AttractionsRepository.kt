package com.stepcounter.app.data.local

import com.stepcounter.app.model.Attraction

object AttractionsRepository {
    
    // Пример достопримечательностей (можно заменить на загрузку из сети или БД)
    val attractions = listOf(
        Attraction(
            id = "1",
            name = "Красная площадь",
            description = "Главная площадь Москвы, объект Всемирного наследия ЮНЕСКО.",
            latitude = 55.7539,
            longitude = 37.6208
        ),
        Attraction(
            id = "2",
            name = "Храм Василия Блаженного",
            description = "Православный храм на Красной площади, памятник русской архитектуры.",
            latitude = 55.7525,
            longitude = 37.6231
        ),
        Attraction(
            id = "3",
            name = "ГУМ",
            description = "Главный универсальный магазин на Красной площади.",
            latitude = 55.7546,
            longitude = 37.6215
        ),
        Attraction(
            id = "4",
            name = "Мавзолей Ленина",
            description = "Усыпальница В.И. Ленина на Красной площади.",
            latitude = 55.7535,
            longitude = 37.6195
        ),
        Attraction(
            id = "5",
            name = "Исторический музей",
            description = "Государственный исторический музей в Москве.",
            latitude = 55.7555,
            longitude = 37.6178
        )
    )

    fun getNearestAttraction(latitude: Double, longitude: Double): Attraction? {
        return attractions.minByOrNull { 
            calculateDistance(latitude, longitude, it.latitude, it.longitude) 
        }
    }

    fun getAttractionsWithinRadius(
        latitude: Double, 
        longitude: Double, 
        radiusMeters: Float
    ): List<Attraction> {
        return attractions.filter { 
            calculateDistance(latitude, longitude, it.latitude, it.longitude) <= radiusMeters 
        }
    }

    // Расчет расстояния между двумя точками в метрах (формула Haversine)
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val earthRadius = 6371000.0 // радиус Земли в метрах
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return (earthRadius * c).toFloat()
    }
}
