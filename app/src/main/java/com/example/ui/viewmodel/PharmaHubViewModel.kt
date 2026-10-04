package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PharmaHubDatabase
import com.example.data.model.*
import com.example.data.repository.PharmaHubRepository
import com.example.service.AiStudyResponse
import com.example.service.GeminiAiStudyService
import com.example.ui.theme.PharmaThemeConfig
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    HOME("Study Space"),
    EXPLORE("Semesters"),
    RESOURCES("Resources"),
    SUBJECTS("Subjects"),
    FLASHCARDS("Flashcards"),
    QUIZ("Quiz Engine"),
    COMMUNITY("Community"),
    CHAT("Study Chat"),
    DRUG_LIBRARY("Drug Library"),
    AI_ASSISTANT("AI Assistant"),
    ANALYTICS("Analytics"),
    ADMIN("Admin & Moderation"),
    PROFILE("Profile")
}

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val citations: List<String> = emptyList(),
    val keyPoints: List<String> = emptyList()
)

data class ActiveQuizState(
    val quiz: QuizEntity,
    val questions: List<QuizQuestionEntity>,
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val userAnswers: Map<Int, Int> = emptyMap(), // question index -> chosen option
    val isCompleted: Boolean = false,
    val scorePercent: Int = 0,
    val weakAreaDetected: String? = null
)

class PharmaHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharmaHubRepository
    private val aiService = GeminiAiStudyService()

    init {
        val db = PharmaHubDatabase.getInstance(application)
        repository = PharmaHubRepository(db.dao())
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Navigation Stack
    private val _screenStack = MutableStateFlow(listOf(AppScreen.HOME))
    val screenStack: StateFlow<List<AppScreen>> = _screenStack.asStateFlow()
    val currentScreen: StateFlow<AppScreen> = _screenStack.map { it.lastOrNull() ?: AppScreen.HOME }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppScreen.HOME)

    fun navigateTo(screen: AppScreen) {
        if (_screenStack.value.lastOrNull() != screen) {
            _screenStack.value = _screenStack.value + screen
        }
    }

    fun popBack(): Boolean {
        if (_screenStack.value.size > 1) {
            _screenStack.value = _screenStack.value.dropLast(1)
            return true
        }
        return false
    }

    // Theme Configuration
    private val _themeConfig = MutableStateFlow(PharmaThemeConfig(preset = ThemePreset.MIDNIGHT, isDark = true))
    val themeConfig: StateFlow<PharmaThemeConfig> = _themeConfig.asStateFlow()

    fun setThemePreset(preset: ThemePreset) {
        _themeConfig.value = _themeConfig.value.copy(preset = preset)
    }

    fun toggleDarkMode(isDark: Boolean) {
        _themeConfig.value = _themeConfig.value.copy(isDark = isDark)
    }

    fun setCustomAccentColor(color: Color?) {
        _themeConfig.value = _themeConfig.value.copy(customAccentColor = color)
    }

    // Academic Profile & Course
    var selectedCourse = MutableStateFlow(PharmacyCourse.B_PHARM)
    var selectedSemester = MutableStateFlow(5)

    // Data Flows from Repository
    val allResources: StateFlow<List<ResourceEntity>> = repository.getAllResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedResources: StateFlow<List<ResourceEntity>> = repository.getBookmarkedResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDrugs: StateFlow<List<DrugEntity>> = repository.getAllDrugs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcardDecks: StateFlow<List<FlashcardDeckEntity>> = repository.getDecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizzes: StateFlow<List<QuizEntity>> = repository.getAllQuizzes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.getAllPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyAnalytics: StateFlow<StudyAnalyticsEntity?> = repository.getAnalytics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Chat Flow
    private val _selectedChatRoom = MutableStateFlow("group_pharmacology")
    val selectedChatRoom: StateFlow<String> = _selectedChatRoom.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _selectedChatRoom.flatMapLatest { roomId ->
        repository.getChatMessages(roomId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectChatRoom(roomId: String) {
        _selectedChatRoom.value = roomId
    }

    fun sendChatMessage(text: String, attachmentTitle: String = "") {
        viewModelScope.launch {
            repository.sendChatMessage(_selectedChatRoom.value, text, attachmentTitle)
        }
    }

    // Flashcard Active Study State
    private val _activeDeck = MutableStateFlow<FlashcardDeckEntity?>(null)
    val activeDeck: StateFlow<FlashcardDeckEntity?> = _activeDeck.asStateFlow()

    val activeDeckCards: StateFlow<List<FlashcardEntity>> = _activeDeck.flatMapLatest { deck ->
        if (deck != null) repository.getCardsForDeck(deck.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var activeCardIndex = MutableStateFlow(0)
    var isCardFlipped = MutableStateFlow(false)

    fun startDeckStudy(deck: FlashcardDeckEntity) {
        _activeDeck.value = deck
        activeCardIndex.value = 0
        isCardFlipped.value = false
        navigateTo(AppScreen.FLASHCARDS)
    }

    fun flipCard() {
        isCardFlipped.value = !isCardFlipped.value
    }

    fun rateFlashcard(card: FlashcardEntity, rating: ReviewRating) {
        viewModelScope.launch {
            repository.reviewFlashcard(card, rating)
            isCardFlipped.value = false
            val currentCards = activeDeckCards.value
            if (activeCardIndex.value < currentCards.size - 1) {
                activeCardIndex.value += 1
            } else {
                activeCardIndex.value = 0
            }
        }
    }

    fun createFlashcardDeck(title: String, subject: String, semester: Int, colorHex: Long) {
        viewModelScope.launch {
            repository.createDeck(title, subject, semester, colorHex)
        }
    }

    // Quiz State
    private val _activeQuizState = MutableStateFlow<ActiveQuizState?>(null)
    val activeQuizState: StateFlow<ActiveQuizState?> = _activeQuizState.asStateFlow()

    fun startQuiz(quiz: QuizEntity) {
        viewModelScope.launch {
            val questions = repository.getQuizQuestions(quiz.id).firstOrNull() ?: emptyList()
            if (questions.isNotEmpty()) {
                _activeQuizState.value = ActiveQuizState(
                    quiz = quiz,
                    questions = questions,
                    currentIndex = 0,
                    selectedOptionIndex = null,
                    isSubmitted = false,
                    userAnswers = emptyMap()
                )
                navigateTo(AppScreen.QUIZ)
            }
        }
    }

    fun selectQuizOption(index: Int) {
        val state = _activeQuizState.value ?: return
        if (!state.isSubmitted) {
            _activeQuizState.value = state.copy(selectedOptionIndex = index)
        }
    }

    fun submitCurrentQuestionAnswer() {
        val state = _activeQuizState.value ?: return
        val chosen = state.selectedOptionIndex ?: return
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return

        val updatedAnswers = state.userAnswers.toMutableMap()
        updatedAnswers[state.currentIndex] = chosen

        _activeQuizState.value = state.copy(
            isSubmitted = true,
            userAnswers = updatedAnswers
        )
    }

    fun nextQuizQuestion() {
        val state = _activeQuizState.value ?: return
        if (state.currentIndex < state.questions.size - 1) {
            val nextIdx = state.currentIndex + 1
            _activeQuizState.value = state.copy(
                currentIndex = nextIdx,
                selectedOptionIndex = state.userAnswers[nextIdx],
                isSubmitted = state.userAnswers.containsKey(nextIdx)
            )
        } else {
            // Calculate final score
            var correctCount = 0
            state.questions.forEachIndexed { i, q ->
                if (state.userAnswers[i] == q.correctIndex) {
                    correctCount++
                }
            }
            val scorePercent = ((correctCount.toFloat() / state.questions.size) * 100).toInt()
            val weak = if (scorePercent < 80) state.quiz.highYieldTopic else null

            _activeQuizState.value = state.copy(
                isCompleted = true,
                scorePercent = scorePercent,
                weakAreaDetected = weak
            )

            viewModelScope.launch {
                repository.recordQuizResult(state.quiz.id, scorePercent)
                repository.addStudySession(minutes = 15, xpEarned = scorePercent * 2)
            }
        }
    }

    fun closeActiveQuiz() {
        _activeQuizState.value = null
    }

    // Community Actions
    fun togglePostUpvote(post: CommunityPostEntity) {
        viewModelScope.launch {
            repository.togglePostUpvote(post.id, post.isUpvoted)
        }
    }

    fun createPost(postType: String, title: String, body: String, tags: String, pollOptions: String = "") {
        viewModelScope.launch {
            repository.createPost(postType, title, body, tags, pollOptions)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
        }
    }

    // Resource Actions
    fun toggleResourceBookmark(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(resource.id, resource.isBookmarked)
        }
    }

    fun recordDownload(resource: ResourceEntity) {
        viewModelScope.launch {
            repository.recordDownload(resource.id)
        }
    }

    fun uploadResource(
        title: String,
        subject: String,
        semester: Int,
        course: String,
        fileType: String,
        fileSize: String,
        tags: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.uploadResource(
                title = title,
                subject = subject,
                semester = semester,
                course = course,
                fileType = fileType,
                fileSize = fileSize,
                tags = tags,
                description = description
            )
        }
    }

    fun deleteResource(resourceId: String) {
        viewModelScope.launch {
            repository.deleteResource(resourceId)
        }
    }

    // Drug Library Actions
    fun toggleDrugBookmark(drug: DrugEntity) {
        viewModelScope.launch {
            repository.toggleDrugBookmark(drug.id, drug.isBookmarked)
        }
    }

    // AI Study Assistant Chat
    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "ai_welcome",
                isUser = false,
                text = "Hello! I am PharmaHub AI, your clinical pharmacology and pharmacy education tutor. Ask me to explain drug mechanisms, clarify pharmacokinetics equations, summarize lecture notes, or generate GPAT revision flashcards.",
                citations = listOf("Goodman & Gilman's Pharmacological Basis of Therapeutics (14th Ed.)"),
                keyPoints = listOf(
                    "All consultations include textbook references.",
                    "Strictly educational – does not provide personal medical advice."
                )
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()
    var isAiGenerating = MutableStateFlow(false)

    fun sendAiPrompt(prompt: String, subjectContext: String = "Pharmacology") {
        if (prompt.isBlank() || isAiGenerating.value) return

        val userMsg = AiChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = prompt
        )
        _aiMessages.value = _aiMessages.value + userMsg
        isAiGenerating.value = true

        viewModelScope.launch {
            val response: AiStudyResponse = aiService.consultAiAssistant(prompt, subjectContext)
            val assistantMsg = AiChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                isUser = false,
                text = response.explanation,
                citations = response.highYieldCitations,
                keyPoints = response.keyPoints
            )
            _aiMessages.value = _aiMessages.value + assistantMsg
            isAiGenerating.value = false
            repository.addStudySession(minutes = 10, xpEarned = 25)
        }
    }
}
