package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.util.FirebaseInitializer
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

private const val TAG = "AuthRepository"

/**
 * Repository responsible for managing Firebase Authentication and the Android Credential Manager
 * authentication lifecycle. Resilient against missing configurations or offline environments.
 */
class AuthRepository(
    private val auth: FirebaseAuth? = null,
    private val credentialManager: CredentialManager? = null
) {
    /**
     * Secondary convenience constructor: initializes CredentialManager and Firebase from Android Context.
     */
    constructor(context: Context) : this(
        resolveAuth(context),
        try { CredentialManager.create(context.applicationContext) } catch (_: Exception) { null }
    )

    companion object {
        fun resolveAuth(context: Context): FirebaseAuth? {
            FirebaseInitializer.ensureInitialized(context)
            return try {
                Firebase.auth
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseAuth service not available: ${e.message}")
                null
            }
        }
    }

    /**
     * Currently authenticated Firebase user (null if signed out).
     */
    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * UID of the authenticated user, or null if unauthenticated.
     */
    val currentUserId: String?
        get() = auth?.currentUser?.uid

    /**
     * Returns true if there is an active Firebase session.
     */
    fun isAuthenticated(): Boolean = auth?.currentUser != null

    /**
     * Observes Firebase Authentication state changes as a cold reactive Kotlin Flow.
     * Guaranteed to emit current user on subscription and clean up listeners on completion.
     */
    fun authStateFlow(): Flow<FirebaseUser?> {
        val activeAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser)
            }
            activeAuth.addAuthStateListener(listener)
            trySend(activeAuth.currentUser)

            awaitClose {
                activeAuth.removeAuthStateListener(listener)
            }
        }
    }

    /**
     * Authenticates the user with Google Sign-In using Android Jetpack Credential Manager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): Result<FirebaseUser> {
        val credManager = credentialManager ?: try {
            CredentialManager.create(context.applicationContext)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val resolvedClientId = serverClientId?.takeIf { it.isNotBlank() }
            ?: try {
                context.getString(R.string.default_web_client_id).takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }

        if (resolvedClientId.isNullOrBlank()) {
            val msg = "Missing default_web_client_id. Configure OAuth credentials in Google Cloud/Firebase console."
            Log.w(TAG, msg)
            return Result.failure(IllegalStateException(msg))
        }

        val activeAuth = auth ?: resolveAuth(context)
        if (activeAuth == null) {
            return Result.failure(IllegalStateException("Firebase Auth service is unavailable."))
        }

        return try {
            val signInOption = GetSignInWithGoogleOption.Builder(resolvedClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            Log.d(TAG, "Requesting credentials from CredentialManager...")
            val response = credManager.getCredential(
                context = context,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Log.d(TAG, "ID token received. Exchanging with Firebase Authentication...")
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult: AuthResult = activeAuth.signInWithCredential(authCredential).awaitTask()
                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "Firebase sign-in successful: ${user.uid} (${user.email})")
                    Result.success(user)
                } else {
                    Result.failure(IllegalStateException("Firebase Auth succeeded but returned null user."))
                }
            } else {
                val errMsg = "Unexpected credential type: ${credential::class.java.name}"
                Log.e(TAG, errMsg)
                Result.failure(IllegalStateException(errMsg))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User canceled Google Sign-In: ${e.message}")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error during Google Sign-In", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Authentication failed during Google Sign-In", e)
            Result.failure(e)
        }
    }

    /**
     * Signs out of Firebase Auth and clears stored credential state.
     */
    suspend fun signOut(context: Context? = null): Result<Unit> {
        return try {
            auth?.signOut()
            if (context != null) {
                val credManager = credentialManager ?: try { CredentialManager.create(context.applicationContext) } catch (_: Exception) { null }
                try {
                    credManager?.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to clear credential manager state: ${e.message}")
                }
            }
            Log.d(TAG, "User successfully signed out.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            Result.failure(e)
        }
    }

    /**
     * Refreshes the currently authenticated user's token and metadata.
     */
    suspend fun reloadUser(): Result<FirebaseUser?> {
        return try {
            val user = auth?.currentUser
            user?.reload()?.awaitTask()
            Result.success(auth?.currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reload user", e)
            Result.failure(e)
        }
    }
}
