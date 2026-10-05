package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseInitializer {
    private const val TAG = "FirebaseInitializer"

    /**
     * Ensures FirebaseApp is safely initialized even if google-services.json is absent or incomplete.
     */
    fun ensureInitialized(context: Context): Boolean {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            return true
        }

        // Try standard resource-based initialization
        try {
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                Log.d(TAG, "FirebaseApp initialized from resources.")
                return true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Standard Firebase initialization from resources skipped: ${e.message}")
        }

        // Safe fallback initialization for environments awaiting google-services.json provisioning
        return try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:200244444745:android:c39478f79f417855")
                .setApiKey("AIzaSyB00000000000000000000000000000000")
                .setProjectId("pharmahub-app")
                .setDatabaseUrl("https://pharmahub-app-default-rtdb.firebaseio.com")
                .setStorageBucket("pharmahub-app.appspot.com")
                .build()

            FirebaseApp.initializeApp(context.applicationContext, options)
            Log.d(TAG, "FirebaseApp initialized with fallback configuration.")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize FirebaseApp with fallback: ${e.message}")
            false
        }
    }
}
