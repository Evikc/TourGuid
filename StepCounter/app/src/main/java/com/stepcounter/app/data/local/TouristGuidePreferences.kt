package com.stepcounter.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stepcounter.app.model.LocationPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tourist_guide_prefs")

object PreferencesKeys {
    val LOCATION_PERMISSION_GRANTED = booleanPreferencesKey("location_permission_granted")
    val TRACK_HISTORY = stringPreferencesKey("track_history")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
}

class TouristGuidePreferences(private val context: Context) {
    
    private val dataStore = context.dataStore

    val isLocationPermissionGranted: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LOCATION_PERMISSION_GRANTED] ?: false
        }

    val trackHistory: Flow<List<LocationPoint>> = dataStore.data
        .map { preferences ->
            val json = preferences[PreferencesKeys.TRACK_HISTORY] ?: "[]"
            parseLocationPoints(json)
        }

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setLocationPermissionGranted(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_PERMISSION_GRANTED] = granted
        }
    }

    suspend fun saveTrackPoint(point: LocationPoint) {
        dataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKeys.TRACK_HISTORY] ?: "[]"
            val points = parseLocationPoints(currentJson).toMutableList()
            points.add(point)
            preferences[PreferencesKeys.TRACK_HISTORY] = serializeLocationPoints(points)
        }
    }

    suspend fun clearTrackHistory() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRACK_HISTORY] = "[]"
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    private fun parseLocationPoints(json: String): List<LocationPoint> {
        return try {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val obj = array.getJSONObject(index)
                LocationPoint(
                    latitude = obj.getDouble("latitude"),
                    longitude = obj.getDouble("longitude"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeLocationPoints(points: List<LocationPoint>): String {
        val array = JSONArray()
        points.forEach { point ->
            val obj = JSONObject().apply {
                put("latitude", point.latitude)
                put("longitude", point.longitude)
                put("timestamp", point.timestamp)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
