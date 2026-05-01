package com.stepcounter.app.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.stepcounter.app.data.local.AttractionsRepository
import com.stepcounter.app.model.Attraction
import com.stepcounter.app.service.LocationTrackerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var attractionNotificationReceiver: BroadcastReceiver? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (fineLocationGranted || coarseLocationGranted) {
            Log.d(TAG, "Location permission granted")
            startLocationService()
        } else {
            Log.w(TAG, "Location permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TouristGuideScreen(
                        onRequestLocationPermission = { requestLocationPermission() },
                        onStartTracking = { startLocationService() },
                        onStopTracking = { stopLocationService() }
                    )
                }
            }
        }
        
        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        registerAttractionReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterAttractionReceiver()
    }

    private fun checkAndRequestPermissions() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
        
        val hasFinePermission = ContextCompat.checkSelfPermission(
            this, fineLocationPermission
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            this, coarseLocationPermission
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasFinePermission || hasCoarsePermission) {
            Log.d(TAG, "Location permission already granted")
            startLocationService()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Log.d(TAG, "Location service started")
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationTrackerService::class.java)
        stopService(intent)
        Log.d(TAG, "Location service stopped")
    }

    private fun registerAttractionReceiver() {
        attractionNotificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.stepcounter.app.ATTRACTION_NEARBY") {
                    val name = intent.getStringExtra("attraction_name") ?: ""
                    val description = intent.getStringExtra("attraction_description") ?: ""
                    Log.d(TAG, "Attraction nearby: $name - $description")
                    // Можно показать Snackbar или Dialog с информацией
                }
            }
        }
        
        val filter = IntentFilter("com.stepcounter.app.ATTRACTION_NEARBY")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(attractionNotificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(attractionNotificationReceiver, filter)
        }
    }

    private fun unregisterAttractionReceiver() {
        try {
            attractionNotificationReceiver?.let {
                unregisterReceiver(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }
}

@Composable
fun TouristGuideScreen(
    onRequestLocationPermission: () -> Unit = {},
    onStartTracking: () -> Unit = {},
    onStopTracking: () -> Unit = {}
) {
    var isTracking by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<String>("Не определено") }
    var nearbyAttractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var totalDistance by remember { mutableStateOf(0f) }
    
    // Обновление данных о локации (в реальном приложении использовать ViewModel)
    LaunchedEffect(isTracking) {
        if (isTracking) {
            // Симуляция обновления данных
            // В реальном приложении здесь будет подписка на Flow из сервиса
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Туристический гид",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Карточка статуса
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isTracking) Color.Green else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTracking) "Отслеживание активно" else "Отслеживание остановлено",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Text(
                    text = "Текущая локация: $currentLocation",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Пройдено: ${totalDistance.toInt()} м",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Кнопки управления
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!isTracking) {
                        onStartTracking()
                        isTracking = true
                    } else {
                        onStopTracking()
                        isTracking = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isTracking) "Стоп" else "Старт")
            }
            
            OutlinedButton(
                onClick = { showMap = !showMap },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showMap) "Скрыть карту" else "Показать карту")
            }
        }
        
        // Список ближайших достопримечательностей
        Text(
            text = "Ближайшие достопримечательности",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (nearbyAttractions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Переместитесь ближе к достопримечательностям для получения информации",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            }
        } else {
            nearbyAttractions.forEach { attraction ->
                AttractionCard(attraction = attraction)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Информация о разрешениях
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Требуемые разрешения:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Доступ к геолокации (точной и приблизительной)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Фоновый доступ к геолокации",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• Уведомления",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onRequestLocationPermission,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Запросить разрешения")
                }
            }
        }
    }
}

@Composable
fun AttractionCard(attraction: Attraction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = attraction.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = attraction.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Координаты: ${String.format("%.4f, %.4f", attraction.latitude, attraction.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
