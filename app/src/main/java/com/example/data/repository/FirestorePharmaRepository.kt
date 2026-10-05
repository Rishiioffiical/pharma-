package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FirestoreResource
import com.example.data.model.FirestoreUser
import com.example.util.FirebaseInitializer
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "FirestorePharmaRepo"

/**
 * Task extension function to await Google Play / Firebase Task completion safely with coroutine cancellation.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        cont.cancel()
    }
}

/**
 * Cloud Firestore repository for storing and synchronizing academic resources and user profiles.
 * Safe and resilient: never throws on initialization if cloud credentials are being configured.
 */
class FirestorePharmaRepository(
    private val db: FirebaseFirestore? = null,
    private val auth: FirebaseAuth? = null
) {
    /**
     * Secondary convenience constructor: resolves database ID from string resources.
     */
    constructor(context: Context) : this(
        resolveFirestoreInstance(context.applicationContext),
        AuthRepository.resolveAuth(context.applicationContext)
    )

    companion object {
        private fun resolveFirestoreInstance(appContext: Context): FirebaseFirestore? {
            FirebaseInitializer.ensureInitialized(appContext)
            return try {
                val dbId = try {
                    appContext.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    "(default)"
                }
                if (dbId.isNotBlank() && dbId != "(default)") {
                    FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
                } else {
                    FirebaseFirestore.getInstance()
                }
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseFirestore instance not available: ${e.message}")
                null
            }
        }
    }

    /**
     * Ensures an authenticated session is active before allowing write operations.
     */
    private fun requireUserId(): String {
        return auth?.currentUser?.uid ?: throw IllegalStateException("Operation requires an active authenticated session.")
    }

    // =========================================================================
    // USER PROFILE FIRESTORE OPERATIONS
    // =========================================================================

    /**
     * Real-time stream of the current user's profile document.
     */
    fun getUserProfileFlow(userId: String): Flow<FirestoreUser?> {
        val firestore = db ?: return flowOf(null)
        if (auth?.currentUser == null) return flowOf(null)

        return callbackFlow {
            val docRef = firestore.collection("users").document(userId)
            val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user profile $userId", error)
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(FirestoreUser::class.java)
                trySend(user)
            }

            awaitClose { registration.remove() }
        }
    }

    /**
     * Saves or updates a user profile document in Firestore.
     */
    suspend fun saveUserProfile(user: FirestoreUser): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not available."))
        return try {
            val currentUid = requireUserId()
            val finalUser = if (user.id.isBlank()) user.copy(id = currentUid) else user
            firestore.collection("users").document(finalUser.id).set(finalUser).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile", e)
            Result.failure(e)
        }
    }

    /**
     * One-time fetch of a user profile by ID.
     */
    suspend fun getUserProfile(userId: String): Result<FirestoreUser?> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not available."))
        return try {
            val snapshot = firestore.collection("users").document(userId).get().awaitTask()
            val user = snapshot.toObject(FirestoreUser::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user profile $userId", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // RESOURCE METADATA FIRESTORE OPERATIONS
    // =========================================================================

    /**
     * Real-time stream of all approved academic study resources.
     */
    fun getApprovedResourcesFlow(): Flow<List<FirestoreResource>> {
        val firestore = db ?: return flowOf(emptyList())

        return callbackFlow {
            val query: Query = firestore.collection("resources")
                .whereEqualTo("status", "APPROVED")

            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to approved resources", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(FirestoreResource::class.java) } ?: emptyList()
                trySend(items)
            }

            awaitClose { registration.remove() }
        }
    }

    /**
     * Real-time stream of resources uploaded by a specific student / user.
     */
    fun getUserResourcesFlow(uploaderId: String): Flow<List<FirestoreResource>> {
        val firestore = db ?: return flowOf(emptyList())

        return callbackFlow {
            val query: Query = firestore.collection("resources")
                .whereEqualTo("uploaderId", uploaderId)

            val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user uploads $uploaderId", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { it.toObject(FirestoreResource::class.java) } ?: emptyList()
                trySend(items)
            }

            awaitClose { registration.remove() }
        }
    }

    /**
     * Saves or updates a resource metadata document in Firestore.
     */
    suspend fun saveResource(resource: FirestoreResource): Result<FirestoreResource> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not available."))
        return try {
            val finalId = if (resource.id.isBlank()) "res_${UUID.randomUUID().toString().take(12)}" else resource.id
            val finalResource = resource.copy(
                id = finalId,
                uploaderId = auth?.currentUser?.uid ?: resource.uploaderId
            )
            firestore.collection("resources").document(finalId).set(finalResource).awaitTask()
            Result.success(finalResource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save resource", e)
            Result.failure(e)
        }
    }

    /**
     * Updates an existing resource's moderation status and notes.
     */
    suspend fun updateResourceStatus(
        resourceId: String,
        newStatus: String,
        moderatorNotes: String = ""
    ): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not available."))
        return try {
            val updates = mapOf(
                "status" to newStatus,
                "moderatorNotes" to moderatorNotes
            )
            firestore.collection("resources").document(resourceId).update(updates).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update resource status $resourceId", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a resource document from Firestore.
     */
    suspend fun deleteResource(resourceId: String): Result<Unit> {
        val firestore = db ?: return Result.failure(IllegalStateException("Firestore is not available."))
        return try {
            firestore.collection("resources").document(resourceId).delete().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete resource $resourceId", e)
            Result.failure(e)
        }
    }
}
