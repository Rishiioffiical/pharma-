package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.ActiveQuizState
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun QuizScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val quizzes by viewModel.allQuizzes.collectAsState()
    val activeQuizState by viewModel.activeQuizState.collectAsState()

    if (activeQuizState != null) {
        val state = activeQuizState!!
        if (state.isCompleted) {
            // Quiz Results & Diagnostic Analysis Screen
            QuizResultsView(
                state = state,
                onRetake = { viewModel.startQuiz(state.quiz) },
                onClose = { viewModel.closeActiveQuiz() },
                onNavigateToFlashcards = { viewModel.navigateTo(AppScreen.FLASHCARDS) },
                modifier = modifier
            )
        } else {
            // Active Quiz Question View
            ActiveQuizQuestionView(
                state = state,
                onSelectOption = { viewModel.selectQuizOption(it) },
                onSubmitAnswer = { viewModel.submitCurrentQuestionAnswer() },
                onNextQuestion = { viewModel.nextQuizQuestion() },
                onExit = { viewModel.closeActiveQuiz() },
                modifier = modifier
            )
        }
    } else {
        // Quizzes Catalog
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Pharmacy Quiz Engine",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "GPAT, NIPER, and university examination simulated tests",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exam-Simulation Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Timed tests with instant negative marking calculations (-0.25) and detailed textbook explanations.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                PharmaSectionHeader(
                    title = "Available Mock Tests",
                    subtitle = "Select a test to challenge your recall"
                )
            }

            items(quizzes) { quiz ->
                QuizItemCard(
                    quiz = quiz,
                    onStart = { viewModel.startQuiz(quiz) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuizItemCard(
    quiz: QuizEntity,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onStart)
            .testTag("quiz_card_${quiz.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${quiz.subject} • Sem ${quiz.semester}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (quiz.difficulty) {
                        "Hard" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = quiz.difficulty,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (quiz.difficulty) {
                                "Hard" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (quiz.highYieldTopic.isNotBlank()) {
                Text(
                    text = "High-Yield Focus: ${quiz.highYieldTopic}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${quiz.questionCount} Questions", style = MaterialTheme.typography.labelSmall)

                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${quiz.durationMinutes} mins", style = MaterialTheme.typography.labelSmall)
                }

                if (quiz.bestScore != null) {
                    Text(
                        text = "Best: ${quiz.bestScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Start", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveQuizQuestionView(
    state: ActiveQuizState,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val question = state.questions[state.currentIndex]
    val optionsList = remember(question) { question.options.split("|") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${state.currentIndex + 1} of ${state.questions.size}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onExit) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Quiz")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Question Navigator Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(state.questions.size) { i ->
                val isAnswered = state.userAnswers.containsKey(i)
                val isCur = state.currentIndex == i
                Surface(
                    shape = CircleShape,
                    color = when {
                        isCur -> MaterialTheme.colorScheme.primary
                        isAnswered -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${i + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question Text Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
                modifier = Modifier.padding(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            optionsList.forEachIndexed { optIndex, optionText ->
                val isSelected = state.selectedOptionIndex == optIndex
                val isCorrect = optIndex == question.correctIndex

                val borderColor = when {
                    state.isSubmitted && isCorrect -> Color(0xFF10B981)
                    state.isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                }

                val bgColor = when {
                    state.isSubmitted && isCorrect -> Color(0xFF10B981).copy(alpha = 0.15f)
                    state.isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isSubmitted) { onSelectOption(optIndex) },
                    shape = RoundedCornerShape(14.dp),
                    color = bgColor,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + optIndex)}. ",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.weight(1f)
                        )
                        if (state.isSubmitted) {
                            if (isCorrect) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            } else if (isSelected) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }

            // High-Yield Rationale Explanation (shown after submission)
            if (state.isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 High-Yield Textbook Explanation:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (!state.isSubmitted) {
                Button(
                    onClick = onSubmitAnswer,
                    enabled = state.selectedOptionIndex != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Answer", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onNextQuestion,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (state.currentIndex < state.questions.size - 1) "Next Question" else "Finish & View Results",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun QuizResultsView(
    state: ActiveQuizState,
    onRetake: () -> Unit,
    onClose: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    if (state.scorePercent >= 75) Color(0xFF10B981).copy(alpha = 0.2f)
                    else Color(0xFFF59E0B).copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state.scorePercent >= 75) Icons.Default.EmojiEvents else Icons.Default.AutoFixHigh,
                contentDescription = null,
                tint = if (state.scorePercent >= 75) Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (state.scorePercent >= 75) "Outstanding Mastery!" else "Good Effort! Focus on Weak Topics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Final Accuracy: ${state.scorePercent}%",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.weakAreaDetected != null) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎯 AI Diagnostic Analysis",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your weak area identified in this attempt: ${state.weakAreaDetected}.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateToFlashcards,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Review Relevant Flashcards", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Retake Quiz")
            }

            Button(
                onClick = onClose,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Done", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
