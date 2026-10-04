package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ResourceEntity
import com.example.ui.components.PillChip
import com.example.ui.components.ResourceUploadDialog
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceLibraryScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val allResources by viewModel.allResources.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSemesterFilter by remember { mutableStateOf<Int?>(null) }
    var selectedSort by remember { mutableStateOf("Most Downloaded") }
    var isUploadOpen by remember { mutableStateOf(false) }
    var previewResource by remember { mutableStateOf<ResourceEntity?>(null) }

    val categories = listOf("All", "Notes", "PYQs", "Practicals", "Manuals", "Research")

    val filteredResources = remember(allResources, searchQuery, selectedCategory, selectedSemesterFilter, selectedSort) {
        allResources.filter { res ->
            val matchSearch = searchQuery.isBlank() ||
                    res.title.contains(searchQuery, ignoreCase = true) ||
                    res.subject.contains(searchQuery, ignoreCase = true) ||
                    res.tags.contains(searchQuery, ignoreCase = true)
            val matchCategory = when (selectedCategory) {
                "Notes" -> res.tags.contains("Notes", ignoreCase = true) || res.fileType == "PDF"
                "PYQs" -> res.tags.contains("PYQ", ignoreCase = true) || res.title.contains("Question", ignoreCase = true)
                "Practicals" -> res.tags.contains("Practical", ignoreCase = true) || res.title.contains("Lab", ignoreCase = true)
                "Manuals" -> res.tags.contains("Manual", ignoreCase = true)
                "Research" -> res.tags.contains("Research", ignoreCase = true)
                else -> true
            }
            val matchSemester = selectedSemesterFilter == null || res.semester == selectedSemesterFilter
            matchSearch && matchCategory && matchSemester
        }.sortedWith { a, b ->
            when (selectedSort) {
                "Highest Rated" -> b.rating.compareTo(a.rating)
                "Most Recent" -> b.id.compareTo(a.id)
                else -> b.downloads.compareTo(a.downloads)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isUploadOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("upload_resource_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text(
                        text = "Resource Library",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Peer-verified notes, question papers, and laboratory records",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search title, subject, tags...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resource_search_field")
                    )
                }
            }

            // Category Chips Row
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        PillChip(
                            text = cat,
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Semester Selector Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        PillChip(
                            text = "All Semesters",
                            selected = selectedSemesterFilter == null,
                            onClick = { selectedSemesterFilter = null }
                        )
                    }
                    items((1..8).toList()) { sem ->
                        PillChip(
                            text = "Sem $sem",
                            selected = selectedSemesterFilter == sem,
                            onClick = { selectedSemesterFilter = if (selectedSemesterFilter == sem) null else sem }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Sort & Count Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredResources.size} Resources Available",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sort: ",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        listOf("Most Downloaded", "Highest Rated").forEach { sortOption ->
                            Text(
                                text = sortOption,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selectedSort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSort == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .clickable { selectedSort = sortOption }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Empty state
            if (filteredResources.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notes here yet.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Be the first person to share a study resource for this semester.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                items(filteredResources) { resource ->
                    ResourceDetailedCard(
                        resource = resource,
                        onPreviewClick = { previewResource = resource },
                        onBookmarkClick = { viewModel.toggleResourceBookmark(resource) },
                        onDownloadClick = { viewModel.recordDownload(resource) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Upload Resource Dialog
    ResourceUploadDialog(
        isOpen = isUploadOpen,
        onDismiss = { isUploadOpen = false },
        onUpload = { title, subject, semester, course, fileType, fileSize, tags, description ->
            viewModel.uploadResource(
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
    )

    // Preview Resource Sheet / Dialog
    if (previewResource != null) {
        val res = previewResource!!
        Dialog(onDismissRequest = { previewResource = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${res.fileType} • ${res.fileSize}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(onClick = { previewResource = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = res.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${res.subject} • Semester ${res.semester} • ${res.course}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Academic Abstract & Syllabus Coverage:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (res.description.isNotBlank()) res.description else "Comprehensive topic coverage aligning with Pharmacy Council of India (PCI) syllabus guidelines. Includes worked clinical examples, derivations, and exam questions.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Contributor: ${res.author}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = res.university,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                            Text(
                                text = "Uploaded: ${res.uploadDate} • Rating: ${res.rating} ★ (${res.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.toggleResourceBookmark(res)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (res.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (res.isBookmarked) "Saved" else "Save")
                        }

                        Button(
                            onClick = {
                                viewModel.recordDownload(res)
                                previewResource = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceDetailedCard(
    resource: ResourceEntity,
    onPreviewClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (resource.fileType) {
                                    "PDF" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    "DOCX" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resource.fileType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = when (resource.fileType) {
                                    "PDF" -> Color(0xFFEF4444)
                                    "DOCX" -> Color(0xFF3B82F6)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "${resource.subject} • Sem ${resource.semester}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = resource.fileSize,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (resource.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (resource.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (resource.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${resource.rating}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${resource.downloads}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPreviewClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Preview", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = onDownloadClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
                    }
                }
            }
        }
    }
}
