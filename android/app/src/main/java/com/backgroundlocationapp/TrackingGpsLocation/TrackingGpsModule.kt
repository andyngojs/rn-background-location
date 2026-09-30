package com.backgroundlocationapp.TrackingGpsLocation

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule

class TrackingGpsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        const val NAME = "TrackingGpsModule"
    }

    override fun getName(): String = NAME
}
