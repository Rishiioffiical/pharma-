package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

object AppCheckManager {
    private const val TAG = "AppCheckManager"
    private var isInitialized = false

    /**
     * Initializes Firebase App Check with Play Integrity, reCAPTCHA v3, or Debug provider.
     * Secures Firebase services (Firestore, Auth, Cloud Storage, Functions) against unauthorized clients.
     *
     * @param context Application context.
     * @param forceReCaptcha If true, prioritizes reCAPTCHA v3 over Play Integrity when a site key is present.
     */
    fun initializeAppCheck(context: Context, forceReCaptcha: Boolean = false) {
        if (isInitialized) return

        try {
            // Ensure FirebaseApp is initialized
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            val appCheck = FirebaseAppCheck.getInstance()

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Configuring App Check with DebugAppCheckProviderFactory")
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                val recaptchaSiteKey = try {
                    context.getString(R.string.recaptcha_site_key).trim()
                } catch (_: Exception) {
                    ""
                }

                if (forceReCaptcha && recaptchaSiteKey.isNotEmpty()) {
                    Log.d(TAG, "Configuring App Check with RecaptchaAppCheckProviderFactory")
                    appCheck.installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(recaptchaSiteKey)
                    )
                } else {
                    Log.d(TAG, "Configuring App Check with PlayIntegrityAppCheckProviderFactory")
                    try {
                        appCheck.installAppCheckProviderFactory(
                            PlayIntegrityAppCheckProviderFactory.getInstance()
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Play Integrity initialization failed, checking reCAPTCHA fallback", e)
                        if (recaptchaSiteKey.isNotEmpty()) {
                            appCheck.installAppCheckProviderFactory(
                                RecaptchaAppCheckProviderFactory.getInstance(recaptchaSiteKey)
                            )
                        } else {
                            throw e
                        }
                    }
                }
            }

            appCheck.setTokenAutoRefreshEnabled(true)
            isInitialized = true
            Log.i(TAG, "Firebase App Check initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Could not initialize Firebase App Check: ${e.message}")
        }
    }
}
