package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmaHubDao {

    // Resources
    @Query("SELECT * FROM resources ORDER BY views DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE isBookmarked = 1")
    fun getBookmarkedResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE semester = :semester")
    fun getResourcesBySemester(semester: Int): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE subject LIKE '%' || :subject || '%'")
    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity)

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Query("UPDATE resources SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE resources SET downloads = downloads + 1 WHERE id = :id")
    suspend fun incrementDownloads(id: String)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResource(id: String)

    // Drugs
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

    // Flashcards
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

    // Quizzes
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

    // Community
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

    // Chat
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getChatMessages(roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    // Analytics
    @Query("SELECT * FROM study_analytics WHERE id = 'current_user_stats'")
    fun getAnalytics(): Flow<StudyAnalyticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAnalytics(analytics: StudyAnalyticsEntity)
}
