package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FirestoreResource
import com.example.data.model.FirestoreUser
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
 * Adheres to zero-trust architecture, auth-gated access, and custom database resolution.
 */
class FirestorePharmaRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    /**
     * Secondary convenience constructor: resolves database ID from string resources.
     */
    constructor(context: Context) : this(
        resolveFirestoreInstance(context.applicationContext),
        Firebase.auth
    )

    companion object {
        private fun resolveFirestoreInstance(appContext: Context): FirebaseFirestore {
            val dbId = try {
                appContext.getString(R.string.firestore_database_id)
            } catch (_: Exception) {
                "(default)"
            }
            return if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        }
    }

    /**
     * Ensures an authenticated session is active before allowing write operations.
     */
    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Operation requires an active authenticated session.")
    }

    // =========================================================================
    // USER PROFILE FIRESTORE OPERATIONS
    // =========================================================================

    /**
     * Real-time stream of the current user's profile document.
     */
    fun getUserProfileFlow(userId: String): Flow<FirestoreUser?> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection("users").document(userId)
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

    /**
     * Saves or updates a user profile document in Firestore.
     */
    suspend fun saveUserProfile(user: FirestoreUser): Result<Unit> {
        return try {
            val currentUid = requireUserId()
            val finalUser = if (user.id.isBlank()) user.copy(id = currentUid) else user
            db.collection("users").document(finalUser.id).set(finalUser).awaitTask()
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
        return try {
            val snapshot = db.collection("users").document(userId).get().awaitTask()
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
    fun getApprovedResourcesFlow(): Flow<List<FirestoreResource>> = callbackFlow {
        val query: Query = db.collection("resources")
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

    /**
     * Real-time stream of resources uploaded by a specific student / user.
     */
    fun getUserSubmissionsFlow(userId: String): Flow<List<FirestoreResource>> = callbackFlow {
        val query: Query = db.collection("resources")
            .whereEqualTo("uploaderId", userId)

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user submissions for $userId", error)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(FirestoreResource::class.java) } ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    /**
     * Real-time stream of resources awaiting administrator/faculty review.
     */
    fun getPendingModerationResourcesFlow(): Flow<List<FirestoreResource>> = callbackFlow {
        val query: Query = db.collection("resources")
            .whereEqualTo("status", "PENDING_MODERATION")

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to pending moderation resources", error)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { it.toObject(FirestoreResource::class.java) } ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    /**
     * Submits resource metadata to Firestore.
     * Enforces that student uploads start with status = 'PENDING_MODERATION'.
     */
    suspend fun submitResource(
        title: String,
        subject: String,
        semester: Int,
        course: String,
        fileType: String,
        fileSize: String,
        tags: String = "",
        description: String = "",
        downloadUrl: String = "",
        storagePath: String = "",
        initialStatus: String = "PENDING_MODERATION"
    ): Result<FirestoreResource> {
        return try {
            val uploaderId = requireUserId()
            val uploaderEmail = auth.currentUser?.email.orEmpty()
            val resourceId = "res_${UUID.randomUUID().toString().take(10)}"

            val resource = FirestoreResource(
                id = resourceId,
                title = title.trim(),
                subject = subject.trim(),
                semester = semester,
                course = course.trim(),
                fileType = fileType.uppercase().trim(),
                fileSize = fileSize.trim(),
                tags = tags.trim(),
                description = description.trim(),
                downloadUrl = downloadUrl.trim(),
                storagePath = storagePath.trim(),
                status = initialStatus,
                uploaderId = uploaderId,
                uploaderEmail = uploaderEmail,
                createdAt = System.currentTimeMillis()
            )

            db.collection("resources").document(resourceId).set(resource).awaitTask()
            Log.d(TAG, "Resource metadata created successfully: $resourceId")
            Result.success(resource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit resource metadata", e)
            Result.failure(e)
        }
    }

    /**
     * Updates moderation status (e.g. APPROVED or REJECTED) with optional notes.
     */
    suspend fun updateResourceStatus(
        resourceId: String,
        newStatus: String,
        moderatorNotes: String = "",
        rejectionReason: String = ""
    ): Result<Unit> {
        return try {
            requireUserId()
            val updates = mapOf(
                "status" to newStatus,
                "moderatorNotes" to moderatorNotes,
                "rejectionReason" to rejectionReason
            )
            db.collection("resources").document(resourceId).update(updates).awaitTask()
            Log.d(TAG, "Resource $resourceId status updated to $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update resource status", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a resource document from Firestore.
     */
    suspend fun deleteResource(resourceId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("resources").document(resourceId).delete().awaitTask()
            Log.d(TAG, "Resource $resourceId deleted from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete resource $resourceId", e)
            Result.failure(e)
        }
    }
}
