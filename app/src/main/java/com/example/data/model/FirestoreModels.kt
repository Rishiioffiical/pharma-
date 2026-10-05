package com.example.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Data model for User profiles stored in Cloud Firestore (/users/{userId}).
 */
@IgnoreExtraProperties
data class FirestoreUser(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("displayName") @set:PropertyName("displayName") var displayName: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = "STUDENT",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "ACTIVE",
    @get:PropertyName("course") @set:PropertyName("course") var course: String = "B.Pharm",
    @get:PropertyName("semester") @set:PropertyName("semester") var semester: Int = 1,
    @get:PropertyName("university") @set:PropertyName("university") var university: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("lastLoginAt") @set:PropertyName("lastLoginAt") var lastLoginAt: Long = System.currentTimeMillis()
)

/**
 * Data model for Academic Resources stored in Cloud Firestore (/resources/{resourceId}).
 */
@IgnoreExtraProperties
data class FirestoreResource(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("subject") @set:PropertyName("subject") var subject: String = "",
    @get:PropertyName("semester") @set:PropertyName("semester") var semester: Int = 1,
    @get:PropertyName("course") @set:PropertyName("course") var course: String = "B.Pharm",
    @get:PropertyName("fileType") @set:PropertyName("fileType") var fileType: String = "PDF",
    @get:PropertyName("fileSize") @set:PropertyName("fileSize") var fileSize: String = "",
    @get:PropertyName("tags") @set:PropertyName("tags") var tags: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("downloadUrl") @set:PropertyName("downloadUrl") var downloadUrl: String = "",
    @get:PropertyName("storagePath") @set:PropertyName("storagePath") var storagePath: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "PENDING_MODERATION",
    @get:PropertyName("uploaderId") @set:PropertyName("uploaderId") var uploaderId: String = "",
    @get:PropertyName("uploaderEmail") @set:PropertyName("uploaderEmail") var uploaderEmail: String = "",
    @get:PropertyName("rejectionReason") @set:PropertyName("rejectionReason") var rejectionReason: String = "",
    @get:PropertyName("moderatorNotes") @set:PropertyName("moderatorNotes") var moderatorNotes: String = "",
    @get:PropertyName("downloads") @set:PropertyName("downloads") var downloads: Int = 0,
    @get:PropertyName("views") @set:PropertyName("views") var views: Int = 0,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis()
)
