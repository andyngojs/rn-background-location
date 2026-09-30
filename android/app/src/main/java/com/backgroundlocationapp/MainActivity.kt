package com.backgroundlocationapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.backgroundlocationapp.TrackingGpsLocation.TrackingService

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "BackgroundLocationApp"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Location FGS needs location permission granted first (Android 14+ throws otherwise).
    val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissions += Manifest.permission.POST_NOTIFICATIONS
    }
    ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
  }

  // Background location must be requested separately, after foreground location is granted.
  // Android 11+ opens Settings for the user to pick "Allow all the time".
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
        !isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
      ActivityCompat.requestPermissions(
          this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2)
    }
  }

  // Back in foreground: service not needed.
  override fun onStart() {
    super.onStart()
      Log.d("[EASTSTARRING]", "App is coming to foreground")
    stopService(Intent(this, TrackingService::class.java))
  }

  // Screen off or app sent to background. A location FGS started from background
  // needs background location; foreground-only permission throws SecurityException.
  override fun onStop() {
    super.onStop()
    val required =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Manifest.permission.ACCESS_BACKGROUND_LOCATION
        else Manifest.permission.ACCESS_FINE_LOCATION
      Log.d("[EASTSTARRING]", "App is go to background")
    if (isGranted(required)) {
      ContextCompat.startForegroundService(this, Intent(this, TrackingService::class.java))
    }
  }

  private fun isGranted(permission: String) =
      ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
