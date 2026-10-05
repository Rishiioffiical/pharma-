package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Platform User Roles following the principle of least privilege.
 */
enum class UserRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Administrator", 100),
    ADMIN("Platform Admin", 80),
    CONTENT_ADMIN("Content Admin", 60),
    MODERATOR("Academic Moderator", 50),
    CONTRIBUTOR("Verified Educator / Contributor", 30),
    PHARMACIST("Licensed Pharmacist", 20),
    STUDENT("Pharmacy Student", 10),
    GUEST("Guest Visitor", 0);

    fun canAccessAdmin(): Boolean = this == SUPER_ADMIN || this == ADMIN || this == CONTENT_ADMIN || this == MODERATOR
    fun canManageUsers(): Boolean = this == SUPER_ADMIN || this == ADMIN
    fun canModerateContent(): Boolean = this == SUPER_ADMIN || this == ADMIN || this == CONTENT_ADMIN || this == MODERATOR
    fun canPublishDirectly(): Boolean = this == SUPER_ADMIN || this == ADMIN || this == CONTENT_ADMIN
    fun canViewAuditLogs(): Boolean = this == SUPER_ADMIN || this == ADMIN
    fun canModifySystemSettings(): Boolean = this == SUPER_ADMIN
}

/**
 * Account statuses for security and moderation.
 */
enum class UserAccountStatus(val displayName: String) {
    ACTIVE("Active"),
    PENDING_VERIFICATION("Pending Verification"),
    SUSPENDED("Account Suspended"),
    BANNED("Permanently Banned")
}

/**
 * Authenticated User record.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole = UserRole.STUDENT,
    val status: UserAccountStatus = UserAccountStatus.ACTIVE,
    val university: String = "National College of Pharmacy",
    val course: String = "B.Pharm",
    val semester: Int = 5,
    val isEmailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val uploadCount: Int = 0,
    val downloadCount: Int = 0,
    val avatarUrl: String = ""
)

/**
 * Lifecycle status of academic resources.
 * User uploads are strictly PENDING_REVIEW until approved by an admin or moderator.
 */
enum class ResourceStatus(val displayName: String) {
    PENDING_REVIEW("Pending Moderation"),
    APPROVED("Approved & Published"),
    REJECTED("Rejected"),
    ARCHIVED("Archived")
}

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val semester: Int,
    val course: String = "B.Pharm",
    val university: String,
    val author: String,
    val authorAvatarUrl: String = "",
    val uploadDate: String,
    val fileType: String, // PDF, DOCX, PPTX
    val fileSize: String,
    val fileSizeBytes: Long = 4194304L, // Default ~4MB
    val tags: String, // Comma separated
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val views: Int = 0,
    val downloads: Int = 0,
    val isBookmarked: Boolean = false,
    val description: String = "",
    val downloadUrl: String = "",
    val storagePath: String = "",
    val status: ResourceStatus = ResourceStatus.APPROVED,
    val uploaderId: String = "sys_admin",
    val uploaderEmail: String = "admin@pharmahub.edu",
    val rejectionReason: String = "",
    val moderatorNotes: String = "",
    val copyrightLicense: String = "Educational Fair Use / CC-BY-NC 4.0",
    val checksumSha256: String = "",
    val isVerified: Boolean = true,
    val reportCount: Int = 0,
    val isLocalOfflineAvailable: Boolean = false,
    val localFilePath: String = ""
)

/**
 * Reasons why a student or faculty might report a resource.
 */
enum class ReportReason(val displayName: String) {
    COPYRIGHT_INFRINGEMENT("Copyright or Licensing Infringement"),
    INCORRECT_INFORMATION("Factually Incorrect / Dangerous Medical Info"),
    MALWARE_OR_SUSPICIOUS("Malicious File or Suspicious Content"),
    SPAM_OR_DUPLICATE("Spam or Duplicate Submission"),
    INAPPROPRIATE_CONTENT("Inappropriate or Defamatory Content"),
    WRONG_CATEGORY("Wrong Subject or Misleading Category"),
    OTHER("Other Violation")
}

enum class ReportStatus(val displayName: String) {
    OPEN("Open"),
    UNDER_REVIEW("Under Review"),
    RESOLVED("Resolved (Action Taken)"),
    DISMISSED("Dismissed (False Report)")
}

@Entity(tableName = "resource_reports")
data class ResourceReportEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val resourceId: String,
    val resourceTitle: String,
    val reporterId: String,
    val reporterEmail: String,
    val reason: ReportReason,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.OPEN,
    val resolutionNotes: String = "",
    val resolvedBy: String = ""
)

/**
 * Immutable security audit action log.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val actorId: String,
    val actorEmail: String,
    val actorRole: String,
    val action: String, // e.g., "APPROVE_RESOURCE", "REJECT_RESOURCE", "SUSPEND_USER", "LOGIN", "SETTINGS_UPDATE"
    val targetId: String,
    val targetType: String, // "RESOURCE", "USER", "REPORT", "SYSTEM"
    val result: String = "SUCCESS", // "SUCCESS", "DENIED", "FAILED"
    val details: String = ""
)

/**
 * Status of background file downloads.
 */
enum class DownloadStatus(val displayName: String) {
    IDLE("Idle"),
    DOWNLOADING("Downloading"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

@Entity(tableName = "download_tasks")
data class DownloadTaskEntity(
    @PrimaryKey val id: String, // typically matches resourceId
    val resourceId: String,
    val resourceTitle: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val bytesDownloaded: Long = 0L,
    val progressPercent: Int = 0,
    val speedKbps: Float = 0f,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val partFilePath: String = "",
    val finalFilePath: String = "",
    val checksum: String = "",
    val errorMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Platform system governance settings.
 */
@Entity(tableName = "system_settings")
data class AppSystemSettingsEntity(
    @PrimaryKey val id: String = "global_settings",
    val maintenanceMode: Boolean = false,
    val maintenanceNotice: String = "PharmaHub is undergoing scheduled infrastructure upgrades. Student services will resume shortly.",
    val allowRegistration: Boolean = true,
    val maxUploadSizeBytes: Long = 52428800L, // 50 MB
    val allowedFileExtensions: String = "pdf,docx,pptx,txt",
    val aiDailyLimitPerUser: Int = 40,
    val aiServiceEnabled: Boolean = true,
    val downloadsEnabled: Boolean = true
)

/**
 * Entity representing a pharmaceutical drug in the local database library.
 */
@Entity(tableName = "pharmaceutical_drugs")
data class PharmaceuticalDrug(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val genericName: String = "",
    val brandName: String = "",
    val classification: String,
    val indications: String,
    val sideEffects: String,
    val contraindications: String = "",
    val mechanismOfAction: String = "",
    val dosage: String = "",
    val dosageForms: String = "",
    val precautions: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Course programs available in Pharmacy education.
 */
enum class PharmacyCourse(val displayName: String, val semesters: Int) {
    B_PHARM("Bachelor of Pharmacy (B.Pharm)", 8),
    PHARM_D("Doctor of Pharmacy (Pharm.D)", 12),
    M_PHARM("Master of Pharmacy (M.Pharm)", 4)
}

/**
 * Resource categories supported by PharmaHub.
 */
enum class ResourceType(val displayName: String) {
    NOTES("Lecture Notes"),
    QUESTION_PAPERS("Previous Year Papers"),
    PRACTICAL_RECORD("Practical & Lab Manuals"),
    ASSIGNMENTS("Assignments & Solutions"),
    PRESENTATIONS("Presentations & Slides"),
    RESEARCH_PAPERS("Research & Review Papers"),
    CLINICAL_CASES("Clinical Case Studies"),
    REVISION_GUIDE("High-Yield Revision Guide")
}

/**
 * Community post types.
 */
enum class PostType(val displayName: String) {
    QUESTION("Academic Question"),
    DISCUSSION("Subject Discussion"),
    RESOURCE("Shared Resource"),
    POLL("Interactive Poll"),
    ANNOUNCEMENT("Exam / Campus Announcement"),
    STUDY_TIP("Study & GPAT Tip")
}

/**
 * Flashcard review rating for spaced-repetition (SuperMemo / Leitner SM-2).
 */
enum class ReviewRating {
    AGAIN, // 1 min / review today
    HARD,  // 12 hours
    GOOD,  // 1-3 days
    EASY   // 5-7 days
}

@Entity(tableName = "drugs")
data class DrugEntity(
    @PrimaryKey val id: String,
    val name: String,
    val genericName: String,
    val brandNames: String, // Comma separated
    val drugClass: String,
    val mechanismOfAction: String,
    val indications: String,
    val contraindications: String,
    val adverseEffects: String,
    val interactions: String,
    val dosageForms: String,
    val storageInstructions: String,
    val pregnancyCategory: String = "Category B",
    val halfLife: String = "4-6 hours",
    val highYieldGpatFacts: String = "",
    val isBookmarked: Boolean = false
) {
    val classification: String get() = drugClass
    val sideEffects: String get() = adverseEffects
}

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val semester: Int,
    val cardCount: Int,
    val masteredCount: Int = 0,
    val colorHex: Long = 0xFF00E5A3,
    val iconName: String = "pill"
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val subject: String,
    val frontText: String,
    val backText: String,
    val formulaOrStructure: String = "",
    val clinicalPearls: String = "",
    val boxLevel: Int = 1, // Leitner box 1 to 5
    val nextReviewTimestamp: Long = 0L,
    val isFavorite: Boolean = false,
    val difficulty: String = "Medium"
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val questionText: String,
    val options: String, // Pipe separated '|'
    val correctIndex: Int,
    val explanation: String,
    val subject: String,
    val difficulty: String = "Medium",
    val imageRes: String = ""
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val semester: Int,
    val questionCount: Int,
    val durationMinutes: Int = 15,
    val difficulty: String = "Medium",
    val highYieldTopic: String = "",
    val bestScore: Int? = null,
    val totalAttempts: Int = 0
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String,
    val postType: String,
    val title: String,
    val body: String,
    val authorName: String,
    val authorRole: String = "Pharm.D Scholar",
    val authorUniversity: String = "National College of Pharmacy",
    val timestamp: String,
    val tags: String,
    val upvotes: Int = 0,
    val isUpvoted: Boolean = false,
    val commentsCount: Int = 0,
    val isBookmarked: Boolean = false,
    val pollOptionsJson: String = "", // JSON or semicolon separated
    val pollVotesJson: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val roomId: String, // e.g. "group_pharmacology", "group_gpat", "direct_rishi"
    val senderName: String,
    val senderRole: String,
    val messageText: String,
    val timestamp: String,
    val isSelf: Boolean,
    val attachmentTitle: String = "",
    val attachmentType: String = "",
    val reactionEmoji: String = ""
)

@Entity(tableName = "study_analytics")
data class StudyAnalyticsEntity(
    @PrimaryKey val id: String = "current_user_stats",
    val streakDays: Int = 7,
    val totalStudyHours: Float = 34.5f,
    val xpEarned: Int = 1420,
    val userLevel: Int = 5,
    val quizzesCompleted: Int = 28,
    val quizAccuracyPercent: Int = 84,
    val flashcardsMastered: Int = 156,
    val resourcesShared: Int = 12,
    val weakArea: String = "Pharmacokinetics (Clearance & Vd)",
    val recommendedTopic: String = "Renal Clearance and First-Pass Metabolism"
)
