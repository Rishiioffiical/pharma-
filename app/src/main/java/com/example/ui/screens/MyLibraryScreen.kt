package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadTaskEntity
import com.example.data.model.ResourceEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.PharmaHubViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLibraryScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val completedDownloads by viewModel.completedDownloads.collectAsState()
    val bookmarkedResources by viewModel.bookmarkedResources.collectAsState()
    val allResources by viewModel.allResources.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Downloads, 1 = Bookmarks, 2 = Recent
    val tabs = listOf("Downloads", "Bookmarks", "Recently Opened")

    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "My Academic Library",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Offline study materials, saved bookmarks & reading history",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index; statusMessage = null },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal)
                                    val count = when (index) {
                                        0 -> completedDownloads.size
                                        1 -> bookmarkedResources.size
                                        else -> allResources.take(3).size
                                    }
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (index == 0) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "$count",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = statusMessage ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // TAB 0: DOWNLOADS (OFFLINE FILES)
        if (activeTab == 0) {
            if (completedDownloads.isEmpty()) {
                item {
                    EmptyLibraryPlaceholder(
                        icon = Icons.Default.DownloadDone,
                        title = "No Offline Downloads Yet",
                        description = "When you download lecture notes or exam papers, they will be stored securely here for zero-data reading without an internet connection."
                    )
                }
            } else {
                items(completedDownloads) { task ->
                    val matchingResource = allResources.find { it.id == task.resourceId }
                    DownloadFileItem(
                        task = task,
                        onOpen = {
                            if (matchingResource != null) {
                                viewModel.openDocument(matchingResource)
                            } else {
                                Toast.makeText(context, "Opening ${task.fileName}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteOfflineResource(task.resourceId)
                            statusMessage = "Removed '${task.fileName}' from offline storage."
                        }
                    )
                }
            }
        }

        // TAB 1: BOOKMARKS
        if (activeTab == 1) {
            if (bookmarkedResources.isEmpty()) {
                item {
                    EmptyLibraryPlaceholder(
                        icon = Icons.Default.BookmarkBorder,
                        title = "No Bookmarks Saved",
                        description = "Tap the bookmark icon on any monograph, lecture notes, or question paper to save it for quick review."
                    )
                }
            } else {
                items(bookmarkedResources) { res ->
                    BookmarkResourceItem(
                        resource = res,
                        onOpen = { viewModel.openDocument(res) },
                        onRemove = { viewModel.toggleResourceBookmark(res) }
                    )
                }
            }
        }

        // TAB 2: RECENTLY OPENED
        if (activeTab == 2) {
            val recents = allResources.take(5)
            if (recents.isEmpty()) {
                item {
                    EmptyLibraryPlaceholder(
                        icon = Icons.Default.History,
                        title = "No Recent Reading History",
                        description = "Your recently viewed pharmaceutical monographs and documents will appear here for fast resumption."
                    )
                }
            } else {
                items(recents) { res ->
                    RecentResourceItem(
                        resource = res,
                        onOpen = { viewModel.openDocument(res) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadFileItem(
    task: DownloadTaskEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(task.updatedAt) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.updatedAt))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = task.resourceTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Text(
                        text = "${task.fileName} • ${"%.1f MB".format(task.fileSizeBytes / (1024f * 1024f))} • $dateStr",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Row {
                Button(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Open", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun BookmarkResourceItem(
    resource: ResourceEntity,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${resource.course} Sem ${resource.semester} • ${resource.subject} • ${resource.fileType} (${resource.fileSize})",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Row {
                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Remove Bookmark", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onOpen) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Open")
                }
            }
        }
    }
}

@Composable
fun RecentResourceItem(
    resource: ResourceEntity,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${resource.subject} • ${resource.fileType}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Button(
                onClick = onOpen,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Continue", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun EmptyLibraryPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            )
        }
    }
}
