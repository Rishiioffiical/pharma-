package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResourceEntity
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PharmaHubViewModel,
    onOpenNote: (ResourceEntity) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenAiWithQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allResources by viewModel.allResources.collectAsState()
    val bookmarkedResources by viewModel.bookmarkedResources.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    val quickAiQueries = listOf("Paracetamol", "Metformin", "Ibuprofen", "Amoxicillin", "Omeprazole", "Beta Blockers")

    // Continue Reading: most recent bookmarked or first available note
    val continueReadingNote = remember(bookmarkedResources, allResources) {
        bookmarkedResources.firstOrNull() ?: allResources.firstOrNull()
    }

    // Featured Notes: first 3 high-yield notes
    val featuredNotes = remember(allResources) {
        allResources.take(3)
    }

    // Recent Notes: next 4 notes
    val recentNotes = remember(allResources) {
        allResources.drop(3).take(4).ifEmpty { allResources.take(4) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. App Header: PHARMAHUB
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PHARMAHUB",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Academic Pharmacy Notes & AI Drug Platform",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // 2. Search Notes / Drugs
        item {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search notes, subjects, or drugs (e.g. Metformin)...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchInput.isNotBlank()) {
                        IconButton(onClick = { searchInput = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchInput.isNotBlank()) {
                        val clean = searchInput.trim().lowercase()
                        if (clean.contains("paracet") || clean.contains("metform") || clean.contains("ibup") ||
                            clean.contains("amox") || clean.contains("omep") || clean.contains("aspirin") ||
                            clean.contains("drug") || clean.contains("mechanism")
                        ) {
                            onOpenAiWithQuery(searchInput.trim())
                        } else {
                            viewModel.noteSearchQuery.value = searchInput.trim()
                            onOpenNotes()
                        }
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input")
            )
        }

        // 3. Quick AI Search Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick AI Search",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickAiQueries) { drugName ->
                        SuggestionChip(
                            onClick = { onOpenAiWithQuery(drugName) },
                            label = { Text(drugName, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // 4. Continue Reading
        continueReadingNote?.let { note ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Continue Reading",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Card(
                        onClick = { onOpenNote(note) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_continue_reading_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${note.subject} • Semester ${note.semester}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onOpenNote(note) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Featured Notes
        if (featuredNotes.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onOpenNotes) {
                        Text("View all", fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    featuredNotes.forEach { note ->
                        NoteListItem(
                            note = note,
                            onOpen = { onOpenNote(note) },
                            onBookmarkToggle = { viewModel.toggleResourceBookmark(note) }
                        )
                    }
                }
            }
        }

        // 6. Recent Notes
        if (recentNotes.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Notes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onOpenNotes) {
                        Text("Browse", fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentNotes.forEach { note ->
                        NoteListItem(
                            note = note,
                            onOpen = { onOpenNote(note) },
                            onBookmarkToggle = { viewModel.toggleResourceBookmark(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteListItem(
    note: ResourceEntity,
    onOpen: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (note.fileType.uppercase()) {
                    "PDF" -> Color(0xFFEF4444).copy(alpha = 0.12f)
                    "PPTX" -> Color(0xFFF97316).copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                }
            ) {
                Text(
                    text = note.fileType.uppercase(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (note.fileType.uppercase()) {
                            "PDF" -> Color(0xFFEF4444)
                            "PPTX" -> Color(0xFFF97316)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${note.subject} • Sem ${note.semester} • ${note.fileSize}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )
            }
            IconButton(onClick = onBookmarkToggle) {
                Icon(
                    imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
