package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmaHubDao {

    // --- Users & RBAC ---
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET role = :role WHERE id = :id")
    suspend fun updateUserRole(id: String, role: UserRole)

    @Query("UPDATE users SET status = :status WHERE id = :id")
    suspend fun updateUserStatus(id: String, status: UserAccountStatus)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // --- Resources & Moderation ---
    @Query("SELECT * FROM resources WHERE status = 'APPROVED' ORDER BY views DESC")
    fun getAllApprovedResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY uploadDate DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE status = 'PENDING_REVIEW' ORDER BY uploadDate DESC")
    fun getPendingModerationResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE uploaderId = :uploaderId ORDER BY uploadDate DESC")
    fun getResourcesByUploader(uploaderId: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE isBookmarked = 1 AND status = 'APPROVED'")
    fun getBookmarkedResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE isLocalOfflineAvailable = 1")
    fun getOfflineResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE semester = :semester AND status = 'APPROVED'")
    fun getResourcesBySemester(semester: Int): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE subject LIKE '%' || :subject || '%' AND status = 'APPROVED'")
    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity)

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Query("UPDATE resources SET status = :status, moderatorNotes = :notes WHERE id = :id")
    suspend fun updateResourceStatus(id: String, status: ResourceStatus, notes: String = "")

    @Query("UPDATE resources SET status = 'REJECTED', rejectionReason = :reason, moderatorNotes = :notes WHERE id = :id")
    suspend fun rejectResource(id: String, reason: String, notes: String)

    @Query("UPDATE resources SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE resources SET downloads = downloads + 1 WHERE id = :id")
    suspend fun incrementDownloads(id: String)

    @Query("UPDATE resources SET isLocalOfflineAvailable = :isOffline, localFilePath = :path WHERE id = :id")
    suspend fun setResourceOffline(id: String, isOffline: Boolean, path: String)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResource(id: String)

    // --- Content Reports & Security ---
    @Query("SELECT * FROM resource_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ResourceReportEntity>>

    @Query("SELECT * FROM resource_reports WHERE status = 'OPEN' ORDER BY timestamp DESC")
    fun getOpenReports(): Flow<List<ResourceReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ResourceReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ResourceReportEntity>)

    @Query("UPDATE resource_reports SET status = :status, resolutionNotes = :notes, resolvedBy = :resolvedBy WHERE id = :id")
    suspend fun updateReportStatus(id: String, status: ReportStatus, notes: String, resolvedBy: String)

    // --- Immutable Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>)

    // --- Background Download Manager ---
    @Query("SELECT * FROM download_tasks ORDER BY updatedAt DESC")
    fun getAllDownloadTasks(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id")
    suspend fun getDownloadTaskById(id: String): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    fun getCompletedDownloads(): Flow<List<DownloadTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadTask(task: DownloadTaskEntity)

    @Query("""
        UPDATE download_tasks 
        SET bytesDownloaded = :bytes, progressPercent = :percent, speedKbps = :speed, updatedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun updateDownloadProgress(id: String, bytes: Long, percent: Int, speed: Float, timestamp: Long)

    @Query("UPDATE download_tasks SET status = :status, errorMessage = :error, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: DownloadStatus, error: String = "", timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun deleteDownloadTask(id: String)

    // --- System Governance Settings ---
    @Query("SELECT * FROM system_settings WHERE id = 'global_settings'")
    fun getSystemSettings(): Flow<AppSystemSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemSettings(settings: AppSystemSettingsEntity)

    // --- Drugs ---
    @Query("SELECT * FROM drugs ORDER BY name ASC")
    fun getAllDrugs(): Flow<List<DrugEntity>>

    @Query("SELECT * FROM drugs WHERE id = :id")
    suspend fun getDrugById(id: String): DrugEntity?

    @Query("SELECT * FROM drugs WHERE name LIKE '%' || :query || '%' OR genericName LIKE '%' || :query || '%' OR brandNames LIKE '%' || :query || '%' OR drugClass LIKE '%' || :query || '%'")
    fun searchDrugs(query: String): Flow<List<DrugEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugs(drugs: List<DrugEntity>)

    @Query("UPDATE drugs SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setDrugBookmark(id: String, isBookmarked: Boolean)

    // --- Flashcards ---
    @Query("SELECT * FROM flashcard_decks ORDER BY title ASC")
    fun getDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<FlashcardDeckEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity)

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    // --- Quizzes ---
    @Query("SELECT * FROM quizzes")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId")
    fun getQuestionsForQuiz(quizId: String): Flow<List<QuizQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>)

    @Query("UPDATE quizzes SET bestScore = :bestScore, totalAttempts = totalAttempts + 1 WHERE id = :quizId")
    suspend fun recordQuizScore(quizId: String, bestScore: Int)

    // --- Community ---
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Query("UPDATE community_posts SET upvotes = upvotes + :delta, isUpvoted = :isUpvoted WHERE id = :id")
    suspend fun toggleUpvote(id: String, delta: Int, isUpvoted: Boolean)

    @Query("DELETE FROM community_posts WHERE id = :id")
    suspend fun deletePost(id: String)

    // --- Chat ---
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getChatMessages(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    // --- Analytics ---
    @Query("SELECT * FROM study_analytics WHERE id = 'current_user_stats'")
    fun getAnalytics(): Flow<StudyAnalyticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAnalytics(analytics: StudyAnalyticsEntity)
}
