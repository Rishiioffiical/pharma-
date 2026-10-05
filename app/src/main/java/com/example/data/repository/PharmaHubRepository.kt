package com.example.data.repository

import android.content.Context
import com.example.data.local.InitialPharmaData
import com.example.data.local.PharmaHubDao
import com.example.data.model.*
import com.example.service.DownloadManagerService
import kotlinx.coroutines.flow.*
import java.util.UUID

class PharmaHubRepository(
    private val dao: PharmaHubDao,
    private val context: Context? = null
) {
    val downloadManager: DownloadManagerService? by lazy {
        context?.let { DownloadManagerService(it, dao) }
    }

    // Current Authenticated User Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    suspend fun seedDatabaseIfEmpty() {
        val existingResources = dao.getAllResources().firstOrNull()
        if (existingResources.isNullOrEmpty()) {
            dao.insertUsers(InitialPharmaData.sampleUsers)
            dao.insertResources(InitialPharmaData.sampleResources)
            dao.insertResources(InitialPharmaData.samplePendingResources)
            dao.insertReports(InitialPharmaData.sampleReports)
            dao.insertAuditLogs(InitialPharmaData.sampleAuditLogs)
            dao.insertSystemSettings(InitialPharmaData.sampleSystemSettings)
            dao.insertDrugs(InitialPharmaData.sampleDrugs)
            dao.insertDecks(InitialPharmaData.sampleDecks)
            dao.insertFlashcards(InitialPharmaData.sampleCards)
            dao.insertQuizzes(InitialPharmaData.sampleQuizzes)
            dao.insertQuizQuestions(InitialPharmaData.sampleQuizQuestions)
            dao.insertPosts(InitialPharmaData.samplePosts)
            dao.insertChatMessages(InitialPharmaData.sampleChatMessages)
            dao.updateAnalytics(InitialPharmaData.sampleAnalytics)
        }

        // Auto-initialize default student session if not yet signed in
        if (_currentUser.value == null) {
            val defaultStudent = dao.getUserByEmail("rishi.pandit@pharmahub.edu")
                ?: dao.getAllUsers().firstOrNull()?.firstOrNull()
            _currentUser.value = defaultStudent
        }
    }

    // --- Authentication & Session Management ---

    suspend fun signIn(email: String, role: UserRole? = null): Result<UserEntity> {
        val user = dao.getUserByEmail(email.trim().lowercase())
            ?: UserEntity(
                id = "usr_${UUID.randomUUID().toString().take(8)}",
                email = email.trim().lowercase(),
                displayName = email.substringBefore("@").replace(".", " ").capitalize(),
                role = role ?: UserRole.STUDENT,
                status = UserAccountStatus.ACTIVE
            ).also { dao.insertUser(it) }

        if (user.status == UserAccountStatus.BANNED) {
            return Result.failure(IllegalStateException("This account has been permanently banned for terms violation."))
        }
        if (user.status == UserAccountStatus.SUSPENDED) {
            return Result.failure(IllegalStateException("This account is temporarily suspended. Contact campus admin."))
        }

        val updated = user.copy(lastLoginAt = System.currentTimeMillis())
        dao.insertUser(updated)
        _currentUser.value = updated

        logAudit(
            action = "USER_LOGIN",
            targetId = updated.id,
            targetType = "USER",
            result = "SUCCESS",
            details = "User signed in as ${updated.role.name}"
        )
        return Result.success(updated)
    }

    suspend fun register(
        name: String,
        email: String,
        course: String,
        semester: Int,
        university: String,
        requestedRole: UserRole = UserRole.STUDENT
    ): Result<UserEntity> {
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }

        val newUser = UserEntity(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            email = email.trim().lowercase(),
            displayName = name.trim(),
            role = requestedRole,
            status = UserAccountStatus.ACTIVE,
            course = course,
            semester = semester,
            university = university,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser

        logAudit(
            action = "USER_REGISTER",
            targetId = newUser.id,
            targetType = "USER",
            result = "SUCCESS",
            details = "New registration for ${newUser.displayName} (${newUser.course})"
        )
        return Result.success(newUser)
    }

    suspend fun switchActiveUserRole(role: UserRole) {
        val cur = _currentUser.value ?: return
        val updated = cur.copy(role = role)
        dao.updateUserRole(cur.id, role)
        _currentUser.value = updated

        logAudit(
            action = "SWITCH_USER_ROLE",
            targetId = cur.id,
            targetType = "USER",
            result = "SUCCESS",
            details = "Role changed to ${role.name}"
        )
    }

    suspend fun signOut() {
        val cur = _currentUser.value
        if (cur != null) {
            logAudit(
                action = "USER_LOGOUT",
                targetId = cur.id,
                targetType = "USER",
                result = "SUCCESS",
                details = "User logged out"
            )
        }
        _currentUser.value = null
    }

    // --- User Administration & RBAC ---

    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canManageUsers()) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can modify user roles."))
        }
        dao.updateUserRole(userId, newRole)
        logAudit(
            action = "UPDATE_USER_ROLE",
            targetId = userId,
            targetType = "USER",
            result = "SUCCESS",
            details = "Role updated to ${newRole.name}"
        )
        return Result.success(Unit)
    }

    suspend fun updateUserStatus(userId: String, newStatus: UserAccountStatus): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canManageUsers()) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can change user account status."))
        }
        dao.updateUserStatus(userId, newStatus)
        logAudit(
            action = "UPDATE_USER_STATUS",
            targetId = userId,
            targetType = "USER",
            result = "SUCCESS",
            details = "Status changed to ${newStatus.name}"
        )
        return Result.success(Unit)
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || actor.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("Unauthorized: Only Super Administrators can delete user accounts."))
        }
        dao.deleteUser(userId)
        logAudit(
            action = "DELETE_USER",
            targetId = userId,
            targetType = "USER",
            result = "SUCCESS",
            details = "Account permanently deleted"
        )
        return Result.success(Unit)
    }

    // --- Resource Library & Moderation Workflow ---

    val allApprovedResources: Flow<List<ResourceEntity>> = dao.getAllApprovedResources()
    val allResources: Flow<List<ResourceEntity>> = dao.getAllResources()
    val pendingModerationResources: Flow<List<ResourceEntity>> = dao.getPendingModerationResources()
    val bookmarkedResources: Flow<List<ResourceEntity>> = dao.getBookmarkedResources()
    val offlineResources: Flow<List<ResourceEntity>> = dao.getOfflineResources()

    fun getResourcesBySemester(semester: Int): Flow<List<ResourceEntity>> = dao.getResourcesBySemester(semester)
    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>> = dao.getResourcesBySubject(subject)

    suspend fun recordDownload(resourceId: String) {
        dao.incrementDownloads(resourceId)
    }

    suspend fun toggleBookmark(id: String, currentStatus: Boolean) {
        dao.setBookmark(id, !currentStatus)
    }

    suspend fun submitResource(
        title: String,
        subject: String,
        semester: Int,
        course: String,
        fileType: String,
        fileSize: String,
        tags: String,
        description: String,
        copyrightLicense: String = "Educational Fair Use / CC-BY-NC 4.0"
    ): Result<ResourceEntity> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Authentication required to submit resources."))

        // Super Admin or Content Admin can publish directly; normal student uploads must undergo moderation
        val initialStatus = if (user.role.canPublishDirectly()) ResourceStatus.APPROVED else ResourceStatus.PENDING_REVIEW

        val newResource = ResourceEntity(
            id = "res_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            subject = subject.trim(),
            semester = semester,
            course = course,
            university = user.university,
            author = user.displayName,
            authorAvatarUrl = user.avatarUrl,
            uploadDate = "Just now",
            fileType = fileType.uppercase(),
            fileSize = fileSize,
            tags = tags.trim(),
            description = description.trim(),
            status = initialStatus,
            uploaderId = user.id,
            uploaderEmail = user.email,
            copyrightLicense = copyrightLicense,
            isVerified = initialStatus == ResourceStatus.APPROVED
        )

        dao.insertResource(newResource)

        logAudit(
            action = if (initialStatus == ResourceStatus.APPROVED) "DIRECT_PUBLISH_RESOURCE" else "SUBMIT_RESOURCE_PENDING",
            targetId = newResource.id,
            targetType = "RESOURCE",
            result = "SUCCESS",
            details = "Title: ${newResource.title}, Status: ${newResource.status.name}"
        )

        return Result.success(newResource)
    }

    suspend fun uploadResource(
        title: String,
        subject: String,
        semester: Int,
        course: String,
        fileType: String,
        fileSize: String,
        tags: String,
        description: String,
        copyrightLicense: String = "Educational Fair Use / CC-BY-NC 4.0"
    ): Result<ResourceEntity> {
        return submitResource(
            title = title,
            subject = subject,
            semester = semester,
            course = course,
            fileType = fileType,
            fileSize = fileSize,
            tags = tags,
            description = description,
            copyrightLicense = copyrightLicense
        )
    }

    suspend fun approveResource(resourceId: String, notes: String = ""): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canModerateContent()) {
            return Result.failure(SecurityException("Unauthorized: Content moderation permission required."))
        }
        dao.updateResourceStatus(resourceId, ResourceStatus.APPROVED, notes)
        logAudit(
            action = "APPROVE_RESOURCE",
            targetId = resourceId,
            targetType = "RESOURCE",
            result = "SUCCESS",
            details = "Approved by ${actor.displayName}. Notes: $notes"
        )
        return Result.success(Unit)
    }

    suspend fun rejectResource(resourceId: String, reason: String, notes: String = ""): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canModerateContent()) {
            return Result.failure(SecurityException("Unauthorized: Content moderation permission required."))
        }
        dao.rejectResource(resourceId, reason, notes)
        logAudit(
            action = "REJECT_RESOURCE",
            targetId = resourceId,
            targetType = "RESOURCE",
            result = "SUCCESS",
            details = "Rejected by ${actor.displayName}. Reason: $reason"
        )
        return Result.success(Unit)
    }

    suspend fun deleteResource(id: String): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canModerateContent()) {
            return Result.failure(SecurityException("Unauthorized: You do not have permission to delete resources."))
        }
        dao.deleteResource(id)
        logAudit(
            action = "DELETE_RESOURCE",
            targetId = id,
            targetType = "RESOURCE",
            result = "SUCCESS",
            details = "Resource permanently deleted by ${actor.displayName}"
        )
        return Result.success(Unit)
    }

    // --- Content Reporting ---

    val allReports: Flow<List<ResourceReportEntity>> = dao.getAllReports()
    val openReports: Flow<List<ResourceReportEntity>> = dao.getOpenReports()

    suspend fun reportResource(
        resourceId: String,
        resourceTitle: String,
        reason: ReportReason,
        details: String
    ): Result<Unit> {
        val user = _currentUser.value
            ?: return Result.failure(IllegalStateException("Please sign in to report a resource."))

        val report = ResourceReportEntity(
            resourceId = resourceId,
            resourceTitle = resourceTitle,
            reporterId = user.id,
            reporterEmail = user.email,
            reason = reason,
            details = details.trim(),
            timestamp = System.currentTimeMillis(),
            status = ReportStatus.OPEN
        )
        dao.insertReport(report)
        logAudit(
            action = "REPORT_RESOURCE",
            targetId = resourceId,
            targetType = "REPORT",
            result = "SUCCESS",
            details = "Reported for ${reason.name} by ${user.email}"
        )
        return Result.success(Unit)
    }

    suspend fun resolveReport(reportId: String, status: ReportStatus, notes: String): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canModerateContent()) {
            return Result.failure(SecurityException("Unauthorized: Moderator permissions required to resolve reports."))
        }
        dao.updateReportStatus(reportId, status, notes, actor.displayName)
        logAudit(
            action = "RESOLVE_REPORT",
            targetId = reportId,
            targetType = "REPORT",
            result = "SUCCESS",
            details = "Status changed to ${status.name} with notes: $notes"
        )
        return Result.success(Unit)
    }

    // --- Background Downloads & Offline Access ---

    val allDownloadTasks: Flow<List<DownloadTaskEntity>> = dao.getAllDownloadTasks()
    val completedDownloads: Flow<List<DownloadTaskEntity>> = dao.getCompletedDownloads()

    fun startDownload(resource: ResourceEntity) {
        downloadManager?.startDownload(resource)
    }

    fun pauseDownload(taskId: String) {
        downloadManager?.pauseDownload(taskId)
    }

    fun cancelDownload(taskId: String) {
        downloadManager?.cancelDownload(taskId)
    }

    fun deleteOfflineResource(resourceId: String) {
        downloadManager?.deleteOfflineFile(resourceId)
    }

    // --- Audit Logging ---

    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogs()

    private suspend fun logAudit(
        action: String,
        targetId: String,
        targetType: String,
        result: String,
        details: String
    ) {
        val actor = _currentUser.value
        val log = AuditLogEntity(
            actorId = actor?.id ?: "anonymous",
            actorEmail = actor?.email ?: "system@pharmahub.edu",
            actorRole = actor?.role?.name ?: "GUEST",
            action = action,
            targetId = targetId,
            targetType = targetType,
            result = result,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        dao.insertAuditLog(log)
    }

    // --- System Governance Settings ---

    val systemSettings: Flow<AppSystemSettingsEntity?> = dao.getSystemSettings()

    suspend fun updateSystemSettings(settings: AppSystemSettingsEntity): Result<Unit> {
        val actor = _currentUser.value
        if (actor == null || !actor.role.canModifySystemSettings()) {
            return Result.failure(SecurityException("Unauthorized: Only Super Administrators can update system governance settings."))
        }
        dao.insertSystemSettings(settings)
        logAudit(
            action = "SYSTEM_SETTINGS_UPDATE",
            targetId = settings.id,
            targetType = "SYSTEM",
            result = "SUCCESS",
            details = "MaintenanceMode=${settings.maintenanceMode}, AllowRegistration=${settings.allowRegistration}"
        )
        return Result.success(Unit)
    }

    // --- Drugs ---
    fun getAllDrugs(): Flow<List<DrugEntity>> = dao.getAllDrugs()
    fun searchDrugs(query: String): Flow<List<DrugEntity>> = dao.searchDrugs(query)
    suspend fun toggleDrugBookmark(id: String, currentStatus: Boolean) {
        dao.setDrugBookmark(id, !currentStatus)
    }

    // --- Flashcards ---
    fun getDecks(): Flow<List<FlashcardDeckEntity>> = dao.getDecks()
    fun getCardsForDeck(deckId: String): Flow<List<FlashcardEntity>> = dao.getCardsForDeck(deckId)

    suspend fun reviewFlashcard(card: FlashcardEntity, rating: ReviewRating) {
        val nextLevel = when (rating) {
            ReviewRating.AGAIN -> 1
            ReviewRating.HARD -> maxOf(1, card.boxLevel - 1)
            ReviewRating.GOOD -> minOf(5, card.boxLevel + 1)
            ReviewRating.EASY -> minOf(5, card.boxLevel + 2)
        }
        val updated = card.copy(
            boxLevel = nextLevel,
            nextReviewTimestamp = System.currentTimeMillis() + when (rating) {
                ReviewRating.AGAIN -> 60_000L
                ReviewRating.HARD -> 43_200_000L
                ReviewRating.GOOD -> 86_400_000L * 2
                ReviewRating.EASY -> 86_400_000L * 5
            }
        )
        dao.updateFlashcard(updated)
    }

    suspend fun createDeck(title: String, subject: String, semester: Int, colorHex: Long) {
        val newDeck = FlashcardDeckEntity(
            id = "deck_${UUID.randomUUID().toString().take(8)}",
            title = title,
            subject = subject,
            semester = semester,
            cardCount = 0,
            colorHex = colorHex
        )
        dao.insertDecks(listOf(newDeck))
    }

    // --- Quizzes ---
    fun getAllQuizzes(): Flow<List<QuizEntity>> = dao.getAllQuizzes()
    fun getQuizQuestions(quizId: String): Flow<List<QuizQuestionEntity>> = dao.getQuestionsForQuiz(quizId)

    suspend fun recordQuizResult(quizId: String, scorePercent: Int) {
        dao.recordQuizScore(quizId, scorePercent)
    }

    // --- Community ---
    fun getAllPosts(): Flow<List<CommunityPostEntity>> = dao.getAllPosts()

    suspend fun togglePostUpvote(postId: String, currentUpvoted: Boolean) {
        val delta = if (currentUpvoted) -1 else 1
        dao.toggleUpvote(postId, delta, !currentUpvoted)
    }

    suspend fun createPost(
        postType: String,
        title: String,
        body: String,
        tags: String,
        pollOptions: String = ""
    ) {
        val user = _currentUser.value
        val newPost = CommunityPostEntity(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            postType = postType,
            title = title,
            body = body,
            authorName = user?.displayName ?: "Rishi Pandit",
            authorRole = user?.role?.displayName ?: "Pharm.D Scholar",
            authorUniversity = user?.university ?: "National College of Pharmacy",
            timestamp = "Just now",
            tags = tags,
            upvotes = 1,
            isUpvoted = true,
            commentsCount = 0,
            pollOptionsJson = pollOptions
        )
        dao.insertPost(newPost)
    }

    suspend fun deletePost(id: String) {
        dao.deletePost(id)
    }

    // --- Chat ---
    fun getChatMessages(roomId: String): Flow<List<ChatMessageEntity>> = dao.getChatMessages(roomId)

    suspend fun sendChatMessage(roomId: String, text: String, attachmentTitle: String = "") {
        val user = _currentUser.value
        val msg = ChatMessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderName = user?.displayName ?: "Rishi Pandit",
            senderRole = user?.role?.displayName ?: "Student",
            messageText = text,
            timestamp = "Just now",
            isSelf = true,
            attachmentTitle = attachmentTitle
        )
        dao.insertChatMessage(msg)
    }

    // --- Analytics ---
    fun getAnalytics(): Flow<StudyAnalyticsEntity?> = dao.getAnalytics()

    suspend fun addStudySession(minutes: Int, xpEarned: Int) {
        val current = dao.getAnalytics().firstOrNull() ?: InitialPharmaData.sampleAnalytics
        val updated = current.copy(
            totalStudyHours = current.totalStudyHours + (minutes / 60f),
            xpEarned = current.xpEarned + xpEarned
        )
        dao.updateAnalytics(updated)
    }
}
