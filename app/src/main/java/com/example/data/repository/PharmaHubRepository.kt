package com.example.data.repository

import com.example.data.local.InitialPharmaData
import com.example.data.local.PharmaHubDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class PharmaHubRepository(private val dao: PharmaHubDao) {

    suspend fun seedDatabaseIfEmpty() {
        val existingResources = dao.getAllResources().firstOrNull()
        if (existingResources.isNullOrEmpty()) {
            dao.insertResources(InitialPharmaData.sampleResources)
            dao.insertDrugs(InitialPharmaData.sampleDrugs)
            dao.insertDecks(InitialPharmaData.sampleDecks)
            dao.insertFlashcards(InitialPharmaData.sampleCards)
            dao.insertQuizzes(InitialPharmaData.sampleQuizzes)
            dao.insertQuizQuestions(InitialPharmaData.sampleQuizQuestions)
            dao.insertPosts(InitialPharmaData.samplePosts)
            dao.insertChatMessages(InitialPharmaData.sampleChatMessages)
            dao.updateAnalytics(InitialPharmaData.sampleAnalytics)
        }
    }

    // Resources
    fun getAllResources(): Flow<List<ResourceEntity>> = dao.getAllResources()
    fun getBookmarkedResources(): Flow<List<ResourceEntity>> = dao.getBookmarkedResources()
    fun getResourcesBySemester(semester: Int): Flow<List<ResourceEntity>> = dao.getResourcesBySemester(semester)
    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>> = dao.getResourcesBySubject(subject)

    suspend fun toggleBookmark(id: String, currentStatus: Boolean) {
        dao.setBookmark(id, !currentStatus)
    }

    suspend fun recordDownload(id: String) {
        dao.incrementDownloads(id)
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
        author: String = "Rishi Pandit"
    ) {
        val newResource = ResourceEntity(
            id = "res_${UUID.randomUUID().toString().take(8)}",
            title = title,
            subject = subject,
            semester = semester,
            course = course,
            university = "National College of Pharmacy",
            author = author,
            uploadDate = "Just now",
            fileType = fileType.uppercase(),
            fileSize = fileSize,
            tags = tags,
            rating = 5.0f,
            reviewCount = 1,
            views = 1,
            downloads = 0,
            isBookmarked = false,
            description = description,
            isVerified = true
        )
        dao.insertResource(newResource)
    }

    suspend fun deleteResource(id: String) {
        dao.deleteResource(id)
    }

    // Drugs
    fun getAllDrugs(): Flow<List<DrugEntity>> = dao.getAllDrugs()
    fun searchDrugs(query: String): Flow<List<DrugEntity>> = dao.searchDrugs(query)
    suspend fun toggleDrugBookmark(id: String, currentStatus: Boolean) {
        dao.setDrugBookmark(id, !currentStatus)
    }

    // Flashcards
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
                ReviewRating.AGAIN -> 60_000L // 1 min
                ReviewRating.HARD -> 43_200_000L // 12 hrs
                ReviewRating.GOOD -> 86_400_000L * 2 // 2 days
                ReviewRating.EASY -> 86_400_000L * 5 // 5 days
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

    suspend fun addCardToDeck(
        deckId: String,
        subject: String,
        front: String,
        back: String,
        formula: String,
        pearls: String
    ) {
        val newCard = FlashcardEntity(
            id = "card_${UUID.randomUUID().toString().take(8)}",
            deckId = deckId,
            subject = subject,
            frontText = front,
            backText = back,
            formulaOrStructure = formula,
            clinicalPearls = pearls,
            boxLevel = 1
        )
        dao.insertFlashcard(newCard)
    }

    // Quizzes
    fun getAllQuizzes(): Flow<List<QuizEntity>> = dao.getAllQuizzes()
    fun getQuizQuestions(quizId: String): Flow<List<QuizQuestionEntity>> = dao.getQuestionsForQuiz(quizId)

    suspend fun recordQuizResult(quizId: String, scorePercent: Int) {
        dao.recordQuizScore(quizId, scorePercent)
    }

    // Community
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
        val newPost = CommunityPostEntity(
            id = "post_${UUID.randomUUID().toString().take(8)}",
            postType = postType,
            title = title,
            body = body,
            authorName = "Rishi Pandit",
            authorRole = "Pharm.D Scholar",
            authorUniversity = "National College of Pharmacy",
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

    // Chat
    fun getChatMessages(roomId: String): Flow<List<ChatMessageEntity>> = dao.getChatMessages(roomId)

    suspend fun sendChatMessage(roomId: String, text: String, attachmentTitle: String = "") {
        val msg = ChatMessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderName = "Rishi Pandit",
            senderRole = "Pharm.D Scholar",
            messageText = text,
            timestamp = "Just now",
            isSelf = true,
            attachmentTitle = attachmentTitle
        )
        dao.insertChatMessage(msg)
    }

    // Analytics
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
