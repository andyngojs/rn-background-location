package com.backgroundlocationapp.TrackingGpsLocation

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.backgroundlocationapp.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class TrackingService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private val locationManager by lazy { getSystemService(LocationManager::class.java) }

    // null = not checked yet, so the first check doesn't announce "GPS on".
    private var locationEnabled: Boolean? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach {
                Log.d("[EASTSTARRING]", "Location: ${it.latitude}, ${it.longitude} ±${it.accuracy}m")
            }
        }
    }

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = syncWithGpsState()
    }

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this, gpsReceiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        syncWithGpsState()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(gpsReceiver)
        fusedClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    // PROVIDERS_CHANGED fires once per provider on each toggle, so act only when the state really changes.
    // Permission is checked before the service is started; startForeground() throws first if it's missing.
    @SuppressLint("MissingPermission")
    private fun syncWithGpsState() {
        val enabled = LocationManagerCompat.isLocationEnabled(locationManager)
        if (enabled == locationEnabled) return
        val firstCheck = locationEnabled == null
        locationEnabled = enabled

        if (enabled) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS).build()
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            if (!firstCheck) {
                notifyStatus("GPS is on", "Location tracking resumed")
            }
        } else {
            fusedClient.removeLocationUpdates(locationCallback)
            notifyStatus("GPS is off", "Location tracking paused. Turn on GPS to continue.")
        }
        Log.d("[EASTSTARRING]", "GPS enabled: $enabled")
    }

    private fun notifyStatus(title: String, text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()
        // Same ID, so "GPS on" replaces "GPS off" instead of stacking.
        getSystemService(NotificationManager::class.java).notify(STATUS_NOTIFICATION_ID, notification)
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Location tracking", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking location")
            .setContentText("Foreground service is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        // Android 14+ throws SecurityException here unless location permission is already granted.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0
        )
    }

    companion object {
        private const val CHANNEL_ID = "tracking"
        private const val NOTIFICATION_ID = 1
        private const val STATUS_NOTIFICATION_ID = 2
        private const val UPDATE_INTERVAL_MS = 5_000L
    }
}
