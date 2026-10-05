package com.example

import android.app.Application
import android.util.Log
import com.example.util.AppCheckManager

class PharmaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            AppCheckManager.initializeAppCheck(this)
        } catch (e: Exception) {
            Log.e("PharmaApplication", "Failed to initialize App Check during application start", e)
        }
    }
}
