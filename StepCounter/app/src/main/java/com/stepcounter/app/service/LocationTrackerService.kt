package com.stepcounter.app.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.stepcounter.app.R
import com.stepcounter.app.data.local.LocationPoint
import com.stepcounter.app.data.local.TracksDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Foreground-сервис для отслеживания геолокации.
 * - Запрашивает и проверяет разрешения на доступ к геолокации
 * - Стабильно работает в фоне
 * - Сохраняет трек (логирование точек)
 * - Уведомляет о ближайших достопримечательностях
 */
@AndroidEntryPoint
class LocationTrackerService : Service() {

    @Inject lateinit var database: TracksDatabase

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()
    
    private val _nearbyAttraction = MutableStateFlow<String?>(null)
    val nearbyAttraction: StateFlow<String?> = _nearbyAttraction.asStateFlow()

    // Список достопримечательностей (координаты и названия)
    private val attractions = listOf(
        Attraction("Эйфелева башня", 48.8584, 2.2945),
        Attraction("Лувр", 48.8606, 2.3376),
        Attraction("Нотр-Дам де Пари", 48.8530, 2.3499),
        Attraction("Триумфальная арка", 48.8738, 2.2950),
        Attraction("Сакре-Кёр", 48.8867, 2.3431)
    )

    private val notificationHelper = NotificationHelper(this)

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        notificationHelper.ensureChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NotificationHelper.LOCATION_FOREGROUND_NOTIFICATION_ID,
            buildForegroundNotification(),
        )
        startLocationUpdates()
        return START_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.stepcounter.app.ui.MainActivity::class.java),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_LOCATION)
            .setContentTitle(getString(R.string.location_tracking_title))
            .setContentText(getString(R.string.location_tracking_text))
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _currentLocation.value = location
                    scope.launch(Dispatchers.IO) {
                        saveLocationPoint(location)
                        checkNearbyAttractions(location)
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "No location permission")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL_MS / 2)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission error", e)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun saveLocationPoint(location: Location) {
        val todayEpochDay = LocalDate.now().toEpochDay()
        val point = LocationPoint(
            dateEpochDay = todayEpochDay,
            timestamp = System.currentTimeMillis(),
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed
        )
        database.locationDao().insert(point)
        Log.d(TAG, "Saved location: ${location.latitude}, ${location.longitude}")
    }

    private fun checkNearbyAttractions(location: Location) {
        val thresholdMeters = 100.0 // радиус уведомления в метрах
        
        for (attraction in attractions) {
            val distance = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                attraction.latitude,
                attraction.longitude,
                distance
            )
            
            if (distance[0] < thresholdMeters) {
                val message = getString(R.string.nearby_attraction_format, attraction.name)
                _nearbyAttraction.value = message
                notificationHelper.showNearbyAttractionNotification(attraction.name, distance[0].toInt())
                Log.d(TAG, "Near attraction: ${attraction.name} at ${distance[0]}m")
                break
            }
        }
    }

    /**
     * Расчет дистанции между двумя точками (в метрах).
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "LocationTrackerService"
        private const val LOCATION_UPDATE_INTERVAL_MS = 5000L // 5 секунд

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackerService::class.java),
            )
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, LocationTrackerService::class.java))
        }
    }
}

/**
 * Модель достопримечательности.
 */
data class Attraction(
    val name: String,
    val latitude: Double,
    val longitude: Double
)
