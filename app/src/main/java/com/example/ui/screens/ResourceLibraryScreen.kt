package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DownloadStatus
import com.example.data.model.ResourceEntity
import com.example.data.model.ResourceStatus
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceLibraryScreen(
    viewModel: PharmaHubViewModel,
    onOpenNote: (ResourceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allResources by viewModel.allResources.collectAsState()
    val downloadTasks by viewModel.allDownloadTasks.collectAsState()
    val searchQuery by viewModel.noteSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedNoteCategory.collectAsState()

    var activeNoteForDetail by remember { mutableStateOf<ResourceEntity?>(null) }

    // Curricular categories explicitly specified in prompt
    val categories = listOf(
        "All",
        "B.Pharm",
        "Pharmacology",
        "Pharmaceutics",
        "Pharmaceutical Chemistry",
        "Pharmacognosy",
        "Biopharmaceutics",
        "Pharmacokinetics",
        "Pharmacy Practice",
        "Herbal Drug Technology",
        "Medicinal Chemistry",
        "Previous Year Papers",
        "Practical Notes",
        "GPAT Notes"
    )

    // Robust case-insensitive partial search across Title, Subject, Semester, Keyword, Tag
    val filteredResources = remember(allResources, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()

        allResources.filter { note ->
            val isApproved = note.status == ResourceStatus.APPROVED

            val matchesQuery = if (q.isBlank()) true else {
                note.title.lowercase().contains(q) ||
                note.subject.lowercase().contains(q) ||
                note.tags.lowercase().contains(q) ||
                note.description.lowercase().contains(q) ||
                "semester ${note.semester}".contains(q) ||
                "sem ${note.semester}".contains(q) ||
                note.fileType.lowercase().contains(q)
            }

            val matchesCategory = if (selectedCategory == "All") true else {
                when (selectedCategory) {
                    "B.Pharm" -> note.course.contains("B.Pharm", ignoreCase = true)
                    "Pharmacology" -> note.subject.contains("Pharmacology", ignoreCase = true) || note.tags.contains("Pharmacology", ignoreCase = true)
                    "Pharmaceutics" -> note.subject.contains("Pharmaceutics", ignoreCase = true) || note.tags.contains("Pharmaceutics", ignoreCase = true)
                    "Pharmaceutical Chemistry" -> note.subject.contains("Chemistry", ignoreCase = true) || note.tags.contains("Chemistry", ignoreCase = true)
                    "Pharmacognosy" -> note.subject.contains("Pharmacognosy", ignoreCase = true) || note.tags.contains("Pharmacognosy", ignoreCase = true)
                    "Biopharmaceutics" -> note.subject.contains("Biopharmaceutics", ignoreCase = true) || note.tags.contains("Biopharmaceutics", ignoreCase = true)
                    "Pharmacokinetics" -> note.subject.contains("Pharmacokinetics", ignoreCase = true) || note.tags.contains("Pharmacokinetics", ignoreCase = true) || note.title.contains("Pharmacokinetics", ignoreCase = true)
                    "Pharmacy Practice" -> note.subject.contains("Practice", ignoreCase = true) || note.tags.contains("Practice", ignoreCase = true)
                    "Herbal Drug Technology" -> note.subject.contains("Herbal", ignoreCase = true) || note.tags.contains("Herbal", ignoreCase = true)
                    "Medicinal Chemistry" -> note.subject.contains("Medicinal", ignoreCase = true) || note.tags.contains("Medicinal", ignoreCase = true)
                    "Previous Year Papers" -> note.title.contains("Paper", ignoreCase = true) || note.tags.contains("PYQ", ignoreCase = true)
                    "Practical Notes" -> note.title.contains("Practical", ignoreCase = true) || note.tags.contains("Practical", ignoreCase = true)
                    "GPAT Notes" -> note.tags.contains("GPAT", ignoreCase = true) || note.title.contains("GPAT", ignoreCase = true)
                    else -> note.subject.contains(selectedCategory, ignoreCase = true) || note.tags.contains(selectedCategory, ignoreCase = true)
                }
            }

            isApproved && matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
            .testTag("resource_library_screen")
    ) {
        // 1. Header & Search Input
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Notes & Academic Library",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.noteSearchQuery.value = it },
                placeholder = { Text("Search by title, subject, semester, or tag...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Notes")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.noteSearchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_search_input")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Curricular Categories Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedNoteCategory.value = category },
                    label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Notes List
        if (filteredResources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No notes found matching '$searchQuery'",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing filters or searching for subjects like Pharmacology, Pharmaceutics, or GPAT.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.noteSearchQuery.value = ""
                        viewModel.selectedNoteCategory.value = "All"
                    }) {
                        Text("Reset Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredResources, key = { it.id }) { note ->
                    val task = downloadTasks.find { it.resourceId == note.id }
                    val isOffline = task?.status == DownloadStatus.COMPLETED || note.isLocalOfflineAvailable

                    Card(
                        onClick = { activeNoteForDetail = note },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_card_${note.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (note.fileType.uppercase()) {
                                        "PDF" -> Color(0xFFEF4444).copy(alpha = 0.12f)
                                        "PPTX" -> Color(0xFFF97316).copy(alpha = 0.12f)
                                        "DOCX" -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Text(
                                        text = note.fileType.uppercase(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (note.fileType.uppercase()) {
                                                "PDF" -> Color(0xFFEF4444)
                                                "PPTX" -> Color(0xFFF97316)
                                                "DOCX" -> Color(0xFF3B82F6)
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isOffline) {
                                        Text(
                                            text = "SAVED OFFLINE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleResourceBookmark(note) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${note.subject} • Semester ${note.semester} • ${note.fileSize}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }

    // 4. Resource Detail Dialog (OPEN, DOWNLOAD, BOOKMARK)
    activeNoteForDetail?.let { note ->
        val currentTask = downloadTasks.find { it.resourceId == note.id }
        val isDownloading = currentTask?.status == DownloadStatus.DOWNLOADING
        val isDownloaded = currentTask?.status == DownloadStatus.COMPLETED || note.isLocalOfflineAvailable

        Dialog(onDismissRequest = { activeNoteForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = note.fileType.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(onClick = { activeNoteForDetail = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Subject: ${note.subject}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Semester: ${note.semester} • Course: ${note.course}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "File Size: ${note.fileSize} • Uploaded: ${note.uploadDate}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "Source: ${note.university} (${note.author})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    if (note.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = note.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Actions: OPEN, DOWNLOAD, BOOKMARK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                activeNoteForDetail = null
                                onOpenNote(note)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!isDownloading) {
                                    viewModel.startDownload(note)
                                    Toast.makeText(context, "Download started for ${note.title}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.Check else Icons.Outlined.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDownloaded) "Saved" else if (isDownloading) "Downloading..." else "Download", fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { viewModel.toggleResourceBookmark(note) },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
