package com.stepcounter.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.stepcounter.app.R
import com.stepcounter.app.data.local.AttractionsRepository
import com.stepcounter.app.model.LocationPoint
import com.stepcounter.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackerService : Service() {

    companion object {
        private const val TAG = "LocationTrackerService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val CHANNEL_NAME = "Отслеживание локации"
        
        // Радиус для уведомления о достопримечательности (в метрах)
        private const val ATTRACTION_NOTIFICATION_RADIUS = 100f
        
        // Интервал обновления локации
        private const val LOCATION_UPDATE_INTERVAL = 5000L
        private const val LOCATION_UPDATE_FASTEST_INTERVAL = 2000L
    }

    private val binder = LocalBinder()
    private var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Текущая локация
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    // История трека
    private val _trackHistory = MutableStateFlow<List<LocationPoint>>(emptyList())
    val trackHistory: StateFlow<List<LocationPoint>> = _trackHistory.asStateFlow()
    
    // Последняя уведомленная достопримечательность
    private var lastNotifiedAttractionId: String? = null
    
    // Общая дистанция трека
    private var totalDistanceMeters = 0f

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackerService = this@LocationTrackerService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        setupLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationTracking()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopLocationTracking()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Сервис отслеживания местоположения"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Туристический гид")
            .setContentText("Отслеживание местоположения активно")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(distance: Float, attractionName: String? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = if (attractionName != null) {
            "Рядом: $attractionName"
        } else {
            "Пройдено: ${distance.toInt()} м"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Туристический гид")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun setupLocationUpdates() {
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        _currentLocation.value = location
        
        val point = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = System.currentTimeMillis()
        )
        
        serviceScope.launch {
            // Добавляем точку в историю
            val currentHistory = _trackHistory.value
            val updatedHistory = currentHistory + point
            _trackHistory.value = updatedHistory
            
            // Расчет дистанции
            if (currentHistory.isNotEmpty()) {
                val lastPoint = currentHistory.last()
                val distance = AttractionsRepository.calculateDistance(
                    lastPoint.latitude,
                    lastPoint.longitude,
                    location.latitude,
                    location.longitude
                )
                totalDistanceMeters += distance
                
                // Логирование трека
                Log.d(TAG, "Track update: Total distance = ${totalDistanceMeters.toInt()}m, Points = ${updatedHistory.size}")
            }
            
            // Проверка на близость к достопримечательностям
            checkNearbyAttractions(location.latitude, location.longitude)
            
            // Обновление уведомления
            updateNotification(totalDistanceMeters)
        }
    }

    private fun checkNearbyAttractions(latitude: Double, longitude: Double) {
        val nearbyAttractions = AttractionsRepository.getAttractionsWithinRadius(
            latitude,
            longitude,
            ATTRACTION_NOTIFICATION_RADIUS
        )
        
        for (attraction in nearbyAttractions) {
            if (attraction.id != lastNotifiedAttractionId) {
                lastNotifiedAttractionId = attraction.id
                notifyAboutAttraction(attraction.name, attraction.description)
                Log.d(TAG, "Nearby attraction: ${attraction.name} (${attraction.description})")
                break // Уведомляем только о ближайшей
            }
        }
    }

    private fun notifyAboutAttraction(name: String, description: String) {
        serviceScope.launch {
            updateNotification(totalDistanceMeters, name)
        }
        
        // Отправка широковещательного сообщения для UI
        val intent = Intent("com.stepcounter.app.ATTRACTION_NEARBY").apply {
            putExtra("attraction_name", name)
            putExtra("attraction_description", description)
        }
        sendBroadcast(intent)
    }

    private fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL
            ).apply {
                setMinUpdateIntervalMillis(LOCATION_UPDATE_FASTEST_INTERVAL)
            }.build()

            try {
                locationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    mainLooper
                )
                Log.d(TAG, "Location tracking started")
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission error", e)
            }
        } else {
            Log.w(TAG, "Location permissions not granted")
        }
    }

    private fun stopLocationTracking() {
        try {
            locationCallback?.let {
                locationClient?.removeLocationUpdates(it)
            }
            Log.d(TAG, "Location tracking stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location tracking", e)
        }
    }

    fun getTotalDistance(): Float = totalDistanceMeters
    
    fun resetTrack() {
        totalDistanceMeters = 0f
        _trackHistory.value = emptyList()
        lastNotifiedAttractionId = null
    }
}
