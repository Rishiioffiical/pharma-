package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FlashcardDeckEntity
import com.example.data.model.ReviewRating
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun FlashcardsScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val decks by viewModel.flashcardDecks.collectAsState()
    val activeDeck by viewModel.activeDeck.collectAsState()
    val activeCards by viewModel.activeDeckCards.collectAsState()
    val currentCardIndex by viewModel.activeCardIndex.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()

    var isCreateDeckOpen by remember { mutableStateOf(false) }

    if (activeDeck != null && activeCards.isNotEmpty()) {
        // Active Study Mode with 3D Flip
        ActiveFlashcardStudyView(
            deck = activeDeck!!,
            cards = activeCards,
            currentIndex = currentCardIndex,
            isFlipped = isFlipped,
            onFlip = { viewModel.flipCard() },
            onRating = { rating ->
                val currentCard = activeCards.getOrNull(currentCardIndex)
                if (currentCard != null) {
                    viewModel.rateFlashcard(currentCard, rating)
                }
            },
            onExit = { viewModel.startDeckStudy(activeDeck!!) /* reload */ },
            onClose = {
                // Return to decks view
                viewModel.startDeckStudy(activeDeck!!)
            },
            modifier = modifier
        )
    } else {
        // Decks Gallery View
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Flashcard System",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Spaced repetition (SM-2) for active recall",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    IconButton(
                        onClick = { isCreateDeckOpen = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Deck", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Leitner Spaced Repetition Active",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Reviews are spaced over 1, 2, and 5-day intervals based on difficulty feedback to optimize long-term memory consolidation.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            item {
                PharmaSectionHeader(
                    title = "Available Decks",
                    subtitle = "Tap any deck to start a study sprint"
                )
            }

            items(decks.size) { index ->
                val deck = decks[index]
                DeckItemCard(
                    deck = deck,
                    onClick = { viewModel.startDeckStudy(deck) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }

    if (isCreateDeckOpen) {
        CreateDeckDialog(
            onDismiss = { isCreateDeckOpen = false },
            onCreate = { title, subject, sem, color ->
                viewModel.createFlashcardDeck(title, subject, sem, color)
                isCreateDeckOpen = false
            }
        )
    }
}

@Composable
fun DeckItemCard(
    deck: FlashcardDeckEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("flashcard_deck_${deck.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(deck.colorHex).copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(deck.colorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Style,
                    contentDescription = null,
                    tint = Color(deck.colorHex),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${deck.subject} • Sem ${deck.semester}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = {
                            if (deck.cardCount > 0) deck.masteredCount.toFloat() / deck.cardCount else 0f
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Color(deck.colorHex),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${deck.cardCount} cards",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Start Study",
                tint = Color(deck.colorHex),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ActiveFlashcardStudyView(
    deck: FlashcardDeckEntity,
    cards: List<com.example.data.model.FlashcardEntity>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onRating: (ReviewRating) -> Unit,
    onExit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCard = cards.getOrElse(currentIndex) { cards.first() }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlipAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = deck.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "Card ${currentIndex + 1} of ${cards.size} • Box Level ${currentCard.boxLevel}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
            TextButton(onClick = onExit) {
                Text("Decks", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / cards.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3D Flipping Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable(onClick = onFlip)
                .testTag("flashcard_flip_area")
        ) {
            if (rotation <= 90f) {
                // Front Side
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = currentCard.subject.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            text = currentCard.frontText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tap anywhere to flip card",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }
                }
            } else {
                // Back Side (mirrored so text is not reversed)
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "HIGH-YIELD RATIONALE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = currentCard.backText,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 22.sp
                                )
                            )

                            if (currentCard.formulaOrStructure.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Formula / Mechanism:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        )
                                        Text(
                                            text = currentCard.formulaOrStructure,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                        )
                                    }
                                }
                            }

                            if (currentCard.clinicalPearls.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "💡 Clinical Pearl: ${currentCard.clinicalPearls}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Tap to flip back",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spaced Repetition Buttons: Again, Hard, Good, Easy
        Text(
            text = "Rate Recall Accuracy (SM-2 Interval Scheduling):",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onRating(ReviewRating.AGAIN) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Again", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Text("1 min", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f)))
                }
            }

            Button(
                onClick = { onRating(ReviewRating.HARD) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hard", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Text("12 hrs", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f)))
                }
            }

            Button(
                onClick = { onRating(ReviewRating.GOOD) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Good", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Text("2 days", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f)))
                }
            }

            Button(
                onClick = { onRating(ReviewRating.EASY) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Easy", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    Text("5 days", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f)))
                }
            }
        }
    }
}

@Composable
fun CreateDeckDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, subject: String, semester: Int, colorHex: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Pharmacology") }
    var semester by remember { mutableIntStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Create Flashcard Deck",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Deck Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, subject, semester, 0xFF00E5A3)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Create Deck", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
