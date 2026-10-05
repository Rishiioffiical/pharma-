package com.example

import android.app.Application
import android.util.Log
import com.example.util.AppCheckManager
import com.example.util.FirebaseInitializer

class PharmaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseInitializer.ensureInitialized(this)
            AppCheckManager.initializeAppCheck(this)
        } catch (e: Exception) {
            Log.e("PharmaApplication", "Failed to initialize App Check / Firebase during application start", e)
        }
    }
}
