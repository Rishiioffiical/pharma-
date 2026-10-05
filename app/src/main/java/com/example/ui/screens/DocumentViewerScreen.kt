package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.service.PdfDocumentManager
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.PharmaHubViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resource = viewModel.openedResource.collectAsState().value
    val downloadTasks by viewModel.allDownloadTasks.collectAsState()

    BackHandler {
        viewModel.closeDocument()
    }

    if (resource == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No document selected", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { viewModel.closeDocument() }) {
                    Text("Return to Library")
                }
            }
        }
        return
    }

    val isPdf = resource.fileType.equals("PDF", ignoreCase = true)
    val currentTask = downloadTasks.find { it.resourceId == resource.id }
    val isDownloaded = currentTask?.status == DownloadStatus.COMPLETED || resource.isLocalOfflineAvailable

    val pdfManager = remember { PdfDocumentManager(context) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentPage by remember { mutableIntStateOf(1) } // 1-based index
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }

    val coroutineScope = rememberCoroutineScope()

    // Initialize real PDF file on mount if format is PDF
    LaunchedEffect(resource.id) {
        if (isPdf) {
            isLoadingPage = true
            try {
                val file = pdfManager.getOrCreatePdfFile(resource)
                pdfFile = file
                val count = pdfManager.getPdfPageCount(file)
                totalPages = count.coerceAtLeast(1)
                val bitmap = pdfManager.renderPdfPage(file, 0)
                currentPageBitmap = bitmap
                currentPage = 1
                isLoadingPage = false
            } catch (e: Exception) {
                loadError = "Unable to load document: ${e.localizedMessage}"
                isLoadingPage = false
            }
        }
    }

    // Function to navigate pages authoritatively
    fun loadPage(targetPage: Int) {
        val file = pdfFile ?: return
        val clamped = targetPage.coerceIn(1, totalPages)
        currentPage = clamped
        isLoadingPage = true

        coroutineScope.launch {
            val bitmap = pdfManager.renderPdfPage(file, clamped - 1)
            currentPageBitmap = bitmap
            isLoadingPage = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = resource.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (resource.fileType.uppercase()) {
                                    "PDF" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                    "PPTX", "PPT" -> Color(0xFFF97316).copy(alpha = 0.15f)
                                    "DOCX", "DOC" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = resource.fileType.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (resource.fileType.uppercase()) {
                                            "PDF" -> Color(0xFFEF4444)
                                            "PPTX", "PPT" -> Color(0xFFF97316)
                                            "DOCX", "DOC" -> Color(0xFF3B82F6)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPdf) "Page $currentPage of $totalPages • ${resource.fileSize}" else resource.fileSize,
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            if (isDownloaded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• OFFLINE SAVED",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeDocument() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Document")
                    }
                },
                actions = {
                    // External App Opener
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val uri = Uri.parse("https://pharmahub.edu/resources/${resource.id}")
                                setDataAndType(uri, when (resource.fileType.uppercase()) {
                                    "PDF" -> "application/pdf"
                                    "PPTX" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                    "PPT" -> "application/vnd.ms-powerpoint"
                                    "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    "DOC" -> "application/msword"
                                    "XLSX" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    else -> "*/*"
                                })
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(Intent.createChooser(intent, "Open with application"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No external viewer app installed for ${resource.fileType}", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in External App")
                    }

                    // Bookmark Resource
                    IconButton(onClick = { viewModel.toggleResourceBookmark(resource) }) {
                        Icon(
                            imageVector = if (resource.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (resource.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Active download progress
                    if (currentTask != null && currentTask.status == DownloadStatus.DOWNLOADING) {
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading File: ${currentTask.progressPercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "%.1f KB/s".format(currentTask.speedKbps),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { currentTask.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Bottom Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPdf) {
                            // Page Navigation Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { loadPage(currentPage - 1) },
                                    enabled = currentPage > 1
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
                                }

                                Text(
                                    text = "$currentPage / $totalPages",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = { loadPage(currentPage + 1) },
                                    enabled = currentPage < totalPages
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
                                }
                            }

                            // Zoom Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.75f) },
                                    enabled = zoomLevel > 0.75f
                                ) {
                                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                                }

                                Text(
                                    text = "${(zoomLevel * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                IconButton(
                                    onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.5f) },
                                    enabled = zoomLevel < 2.5f
                                ) {
                                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In")
                                }
                            }
                        } else {
                            // Non-PDF status text
                            Text(
                                text = "${resource.fileType.uppercase()} Document",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Robust Download Button (No text wrapping)
                        if (isDownloaded) {
                            FilledTonalButton(
                                onClick = {
                                    Toast.makeText(context, "File is saved offline in My Library", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saved", maxLines = 1, softWrap = false)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.startDownload(resource)
                                    Toast.makeText(context, "Download started for ${resource.title}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("viewer_download_action_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Download",
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isPdf) {
                // Page Slider for fast navigation
                if (totalPages > 1) {
                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { target ->
                            loadPage(target.toInt())
                        },
                        valueRange = 1f..totalPages.toFloat(),
                        steps = (totalPages - 2).coerceAtLeast(0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                // Native PDF Page Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (isLoadingPage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (loadError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = loadError ?: "Failed to open document", color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(onClick = { loadPage(currentPage) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else if (currentPageBitmap != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(scaleX = zoomLevel, scaleY = zoomLevel)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                            shadowElevation = 6.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Image(
                                bitmap = currentPageBitmap!!.asImageBitmap(),
                                contentDescription = "Page $currentPage of $totalPages",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                // Non-PDF Document Handler (PPTX, DOCX, XLSX)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (resource.fileType.uppercase()) {
                                            "PPTX", "PPT" -> Color(0xFFF97316).copy(alpha = 0.15f)
                                            "DOCX", "DOC" -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (resource.fileType.uppercase()) {
                                        "PPTX", "PPT" -> Icons.Default.Slideshow
                                        "DOCX", "DOC" -> Icons.Default.Description
                                        else -> Icons.Default.InsertDriveFile
                                    },
                                    contentDescription = null,
                                    tint = when (resource.fileType.uppercase()) {
                                        "PPTX", "PPT" -> Color(0xFFF97316)
                                        "DOCX", "DOC" -> Color(0xFF3B82F6)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = resource.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "File Format: ${resource.fileType.uppercase()} Presentation / Document • ${resource.fileSize}",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "About this file format:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "This file contains presentation slides or formatted document tables. To view native presentation animations or edit content, open it in an external office viewer or download it to your device.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                val uri = Uri.parse("https://pharmahub.edu/resources/${resource.id}")
                                                setDataAndType(uri, "*/*")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Open with"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No external app installed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Externally", maxLines = 1, softWrap = false)
                                }

                                Button(
                                    onClick = {
                                        viewModel.startDownload(resource)
                                        Toast.makeText(context, "Download queued for ${resource.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download", maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
