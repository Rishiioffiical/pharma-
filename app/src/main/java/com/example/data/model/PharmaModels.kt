package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val tags: String, // Comma separated
    val rating: Float,
    val reviewCount: Int,
    val views: Int,
    val downloads: Int,
    val isBookmarked: Boolean = false,
    val description: String = "",
    val downloadUrl: String = "",
    val isVerified: Boolean = true,
    val reportCount: Int = 0
)

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
)

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
