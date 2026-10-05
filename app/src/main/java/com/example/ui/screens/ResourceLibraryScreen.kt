package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.data.model.ResourceEntity
import com.example.data.model.ResourceStatus
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PillChip
import com.example.ui.components.ResourceUploadDialog
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceLibraryScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allResources by viewModel.allResources.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val downloadTasks by viewModel.allDownloadTasks.collectAsState()
    val completedDownloads by viewModel.completedDownloads.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Catalog, 1 = My Submissions, 2 = My Downloads
    val tabs = listOf("Academic Catalog", "My Submissions", "My Downloads")

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSemesterFilter by remember { mutableStateOf<Int?>(null) }
    var selectedSort by remember { mutableStateOf("Most Downloaded") }
    var isUploadOpen by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Notes", "PYQs", "Practicals", "Manuals", "Research")

    // Filter ONLY approved resources for public catalog
    val publicApprovedResources = remember(allResources, searchQuery, selectedCategory, selectedSemesterFilter, selectedSort) {
        allResources.filter { res ->
            res.status == ResourceStatus.APPROVED &&
            (searchQuery.isBlank() ||
                res.title.contains(searchQuery, ignoreCase = true) ||
                res.subject.contains(searchQuery, ignoreCase = true) ||
                res.tags.contains(searchQuery, ignoreCase = true)) &&
            (when (selectedCategory) {
                "Notes" -> res.tags.contains("Notes", ignoreCase = true) || res.fileType == "PDF"
                "PYQs" -> res.tags.contains("PYQ", ignoreCase = true) || res.title.contains("Question", ignoreCase = true)
                "Practicals" -> res.tags.contains("Practical", ignoreCase = true) || res.title.contains("Lab", ignoreCase = true)
                "Manuals" -> res.tags.contains("Manual", ignoreCase = true)
                "Research" -> res.tags.contains("Research", ignoreCase = true)
                else -> true
            }) &&
            (selectedSemesterFilter == null || res.semester == selectedSemesterFilter)
        }.sortedWith { a, b ->
            when (selectedSort) {
                "Highest Rated" -> b.rating.compareTo(a.rating)
                "Most Recent" -> b.id.compareTo(a.id)
                else -> b.downloads.compareTo(a.downloads)
            }
        }
    }

    // Filter user's own submissions
    val mySubmissions = remember(allResources, currentUser) {
        allResources.filter { res ->
            currentUser != null && (res.uploaderId == currentUser?.id || res.uploaderEmail == currentUser?.email)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (currentUser == null) {
                        Toast.makeText(context, "Please sign in to upload resources.", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(AppScreen.AUTH)
                    } else {
                        isUploadOpen = true
                    }
                },
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
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Section
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Resource Library",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "Peer-verified notes, question papers, and laboratory records",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        // My Account Shortcut
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.AUTH) }) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.displayName?.take(1)?.uppercase() ?: "G",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector Row
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
                                onClick = { activeTab = index; feedbackMessage = null },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal)
                                        if (index == 1 && mySubmissions.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "${mySubmissions.size}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        if (index == 2 && completedDownloads.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF10B981)
                                            ) {
                                                Text(
                                                    text = "${completedDownloads.size}",
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

            // Notification / Feedback banner
            if (feedbackMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = feedbackMessage ?: "", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }

            // ==================== TAB 0: PUBLIC CATALOG ====================
            if (activeTab == 0) {
                // Search bar
                item {
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
                            .padding(horizontal = 20.dp)
                            .testTag("resource_search_field")
                    )
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
                }

                // Count & Sort Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${publicApprovedResources.size} Approved Materials",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Text(
                            text = "Tap any card to open in Document Viewer",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        )
                    }
                }

                if (publicApprovedResources.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No approved resources match your query.", fontWeight = FontWeight.Bold)
                                Text("Try clearing filters or search terms.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    items(publicApprovedResources) { res ->
                        ResourceCatalogCard(
                            resource = res,
                            onCardClick = {
                                viewModel.openDocument(res)
                            },
                            onBookmarkClick = {
                                viewModel.toggleResourceBookmark(res)
                            },
                            onDownloadClick = {
                                viewModel.startDownload(res)
                                feedbackMessage = "Download queued for '${res.title.take(24)}...' Check 'My Downloads' tab."
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // ==================== TAB 1: MY SUBMISSIONS ====================
            if (activeTab == 1) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Secure Academic Moderation Pipeline",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To protect curriculum accuracy, prevent copyright infringement, and ensure PCI standard alignment, all student uploads undergo faculty review before becoming publicly visible.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                if (currentUser == null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sign In to view your submissions", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(onClick = { viewModel.navigateTo(AppScreen.AUTH) }) {
                                    Text("Go to Sign In")
                                }
                            }
                        }
                    }
                } else if (mySubmissions.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No submissions yet", fontWeight = FontWeight.Bold)
                                Text("Upload your lecture notes or practical records to help peer pharmacy students.", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { isUploadOpen = true }) {
                                    Text("Submit First Document")
                                }
                            }
                        }
                    }
                } else {
                    items(mySubmissions) { res ->
                        MySubmissionCard(
                            resource = res,
                            onOpenClick = { viewModel.openDocument(res) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }

            // ==================== TAB 2: MY DOWNLOADS ====================
            if (activeTab == 2) {
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.OfflinePin, contentDescription = null, tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Offline Academic Storage",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${completedDownloads.size} documents saved on device for zero-data reading",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Downloads Section
                val activeDownloads = downloadTasks.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PAUSED }
                if (activeDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "In-Progress Downloads (${activeDownloads.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    items(activeDownloads) { task ->
                        ActiveDownloadCard(
                            task = task,
                            onPause = { viewModel.pauseDownload(task.id) },
                            onResume = {
                                val res = allResources.find { it.id == task.resourceId }
                                if (res != null) viewModel.startDownload(res)
                            },
                            onCancel = { viewModel.cancelDownload(task.id) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // Completed Offline Files
                if (completedDownloads.isEmpty() && activeDownloads.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.DownloadDone, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No offline documents downloaded yet.", fontWeight = FontWeight.Bold)
                                Text("Browse the catalog and tap 'Download' to read lecture notes without internet.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    items(completedDownloads) { task ->
                        val matchingResource = allResources.find { it.id == task.resourceId }
                        CompletedDownloadCard(
                            task = task,
                            onRead = {
                                if (matchingResource != null) {
                                    viewModel.openDocument(matchingResource)
                                } else {
                                    Toast.makeText(context, "Opening ${task.fileName}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelete = {
                                viewModel.deleteOfflineResource(task.resourceId)
                                feedbackMessage = "Removed ${task.fileName} from offline storage."
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }

    // Upload Resource Dialog
    if (isUploadOpen) {
        ResourceUploadDialog(
            isOpen = isUploadOpen,
            onDismiss = { isUploadOpen = false },
            onUpload = { title, subject, semester, course, fileType, fileSize, tags, desc ->
                viewModel.uploadResource(
                    title = title,
                    subject = subject,
                    semester = semester,
                    course = course,
                    fileType = fileType,
                    fileSize = fileSize,
                    tags = tags,
                    description = desc
                )
                feedbackMessage = if (currentUser?.role?.canPublishDirectly() == true) {
                    "Published directly: '$title' is live!"
                } else {
                    "Submitted: '$title' is in moderation queue and will appear after admin review."
                }
                activeTab = 1 // Switch to My Submissions
            }
        )
    }
}

@Composable
fun ResourceCatalogCard(
    resource: ResourceEntity,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                                fontWeight = FontWeight.Bold,
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
                            text = "${resource.course} • Sem ${resource.semester}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = resource.subject,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "VERIFIED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
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
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${resource.author} • ${resource.fileSize}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Row {
                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (resource.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (resource.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = "Open",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MySubmissionCard(
    resource: ResourceEntity,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (resource.status) {
                        ResourceStatus.APPROVED -> Color(0xFF10B981).copy(alpha = 0.15f)
                        ResourceStatus.PENDING_REVIEW -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ResourceStatus.REJECTED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (resource.status) {
                            ResourceStatus.APPROVED -> "PUBLISHED & VERIFIED"
                            ResourceStatus.PENDING_REVIEW -> "PENDING ADMIN MODERATION"
                            ResourceStatus.REJECTED -> "REJECTED BY MODERATOR"
                            else -> resource.status.displayName
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (resource.status) {
                                ResourceStatus.APPROVED -> Color(0xFF10B981)
                                ResourceStatus.PENDING_REVIEW -> Color(0xFFF59E0B)
                                ResourceStatus.REJECTED -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }

                Text(
                    text = resource.fileType,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = resource.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = "${resource.course} Sem ${resource.semester} • ${resource.subject} • ${resource.fileSize}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            )

            if (resource.status == ResourceStatus.PENDING_REVIEW) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Hidden from general student search until reviewed. Expected moderation turnaround: < 24 hours.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB45309), fontSize = 11.sp),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (resource.status == ResourceStatus.REJECTED && resource.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Rejection Reason: ${resource.rejectionReason}. Please revise according to guidelines.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B), fontSize = 11.sp),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onOpenClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Preview Submission in Reader", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    task: com.example.data.model.DownloadTaskEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.resourceTitle,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${task.progressPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { task.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed: %.1f KB/s • %s".format(task.speedKbps, task.status.displayName),
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                )

                Row {
                    if (task.status == DownloadStatus.DOWNLOADING) {
                        IconButton(onClick = onPause) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = onResume) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedDownloadCard(
    task: com.example.data.model.DownloadTaskEntity,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.resourceTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                Text(
                    text = "${task.fileName} • ${"%.1f MB".format(task.fileSizeBytes / (1024f * 1024f))}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Row {
                Button(
                    onClick = onRead,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Read Offline", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}
