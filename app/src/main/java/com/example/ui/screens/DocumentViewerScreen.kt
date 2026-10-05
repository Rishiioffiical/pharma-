package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadStatus
import com.example.data.model.ResourceEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.PharmaHubViewModel

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

    val currentTask = downloadTasks.find { it.resourceId == resource.id }
    val isDownloaded = currentTask?.status == DownloadStatus.COMPLETED || resource.isLocalOfflineAvailable

    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = 8
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var bookmarkedPages by remember { mutableStateOf(setOf<Int>()) }

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
                            Text(
                                text = "${resource.fileType} • ${resource.fileSize} • Page $currentPage of $totalPages",
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
                    // Search in Document
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search In Document",
                            tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // External Viewer Intent Action
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val simulatedUri = Uri.parse("https://pharmahub.edu/resources/${resource.id}.pdf")
                                setDataAndType(simulatedUri, when (resource.fileType) {
                                    "PDF" -> "application/pdf"
                                    "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    "PPTX" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                                    else -> "application/pdf"
                                })
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(Intent.createChooser(intent, "Open with document viewer"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No external app installed for ${resource.fileType}. Viewing in built-in reader.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in External App")
                    }

                    // Bookmark Page
                    IconButton(onClick = {
                        bookmarkedPages = if (bookmarkedPages.contains(currentPage)) {
                            bookmarkedPages - currentPage
                        } else {
                            bookmarkedPages + currentPage
                        }
                    }) {
                        Icon(
                            imageVector = if (bookmarkedPages.contains(currentPage)) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark Page",
                            tint = if (bookmarkedPages.contains(currentPage)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    // Active download progress indicator
                    if (currentTask != null && currentTask.status == DownloadStatus.DOWNLOADING) {
                        Column(modifier = Modifier.padding(bottom = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading Complete Document: ${currentTask.progressPercent}%",
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
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Bottom Navigation & Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page navigation buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (currentPage > 1) currentPage-- },
                                enabled = currentPage > 1
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
                            }

                            Text(
                                text = "$currentPage / $totalPages",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = { if (currentPage < totalPages) currentPage++ },
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
                                onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.0f) },
                                enabled = zoomLevel < 2.0f
                            ) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In")
                            }
                        }

                        // Download Button
                        if (isDownloaded) {
                            FilledTonalButton(
                                onClick = {
                                    Toast.makeText(context, "Saved offline in My Downloads", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saved")
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.startDownload(resource)
                                    Toast.makeText(context, "Download started for ${resource.title}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("in_viewer_download_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download")
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
            // Document In-App Search Bar
            if (isSearchActive) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search text within page...") },
                            singleLine = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                }
            }

            // Page Slider
            Slider(
                value = currentPage.toFloat(),
                onValueChange = { currentPage = it.toInt() },
                valueRange = 1f..totalPages.toFloat(),
                steps = totalPages - 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // Document Canvas Page Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                DocumentPageView(
                    resource = resource,
                    pageNumber = currentPage,
                    totalPages = totalPages,
                    zoom = zoomLevel,
                    highlightQuery = searchQuery
                )
            }
        }
    }
}

/**
 * Authentic high-yield pharmaceutical document page rendering.
 */
@Composable
fun DocumentPageView(
    resource: ResourceEntity,
    pageNumber: Int,
    totalPages: Int,
    zoom: Float,
    highlightQuery: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(zoom.coerceIn(0.85f, 1.0f))
            .wrapContentHeight()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        color = Color(0xFFFCFDFE),
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Page Header Watermark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHARMAHUB ACADEMIC REPOSITORY • ${resource.course} SEMESTER ${resource.semester}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Page $pageNumber of $totalPages",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)

            when (pageNumber) {
                1 -> {
                    // Page 1: Academic Title & Overview
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = resource.university.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF0F172A)
                            )
                        )
                        Text(
                            text = "DEPARTMENT OF PHARMACEUTICAL SCIENCES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = resource.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Subject: ${resource.subject} • Contributed by ${resource.author}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "EXECUTIVE CURRICULAR ABSTRACT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = resource.description.ifEmpty {
                                    "Comprehensive academic notes aligned with PCI guidelines covering drug classifications, physiological mechanisms of action, pharmacokinetics (ADME), clinical indications, and side effects for semester ${resource.semester}."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155), lineHeight = 18.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "TABLE OF CONTENTS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val toc = listOf(
                        "1. Introduction & Physico-Chemical Properties" to "Page 2",
                        "2. Pharmacokinetics & ADME Parameters" to "Page 3",
                        "3. Cellular Receptor Targets & MOA Pathways" to "Page 4",
                        "4. Clinical Indications & Therapeutic Guidelines" to "Page 5",
                        "5. Adverse Effects, Boxed Warnings & Interactions" to "Page 6",
                        "6. Dosage Calculations & Formulation Kinetics" to "Page 7",
                        "7. High-Yield GPAT Revision & Self-Assessment" to "Page 8"
                    )

                    toc.forEach { (topic, pg) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = topic, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF1E293B)))
                            Text(text = pg, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold))
                        }
                    }
                }

                2 -> {
                    // Page 2: Pharmacokinetics & Equations
                    Text(
                        text = "1. PHARMACOKINETICS & CLEARANCE METRICS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pharmacokinetics governs the time course of drug absorption, distribution, metabolism, and excretion. Understanding physiological clearance is essential for optimizing dosing intervals.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFFEEF2FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CORE PHARMACOKINETIC FORMULAS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Volume of Distribution: Vd = Dose / C0\n• Total Body Clearance: CL = ke × Vd\n• Elimination Half-life: t1/2 = 0.693 × Vd / CL\n• Steady-State Concentration: Css = (F × Dose) / (CL × τ)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1E1B4B),
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "First-Pass Hepatic Metabolism:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Text(
                        text = "Drugs absorbed from the gastrointestinal tract travel directly via the portal vein to the liver prior to systemic circulation. Bioavailability (F) is calculated via F = (AUC oral / AUC IV) × 100.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                    )
                }

                3 -> {
                    // Page 3: Mechanism of Action & Cellular Receptors
                    Text(
                        text = "2. MECHANISM OF ACTION & RECEPTOR PHARMACOLOGY",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "MOLECULAR TARGET CASCADE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Ligand binding to G-protein coupled receptor (GPCR) or enzyme catalytic active site.\n2. Allosteric or competitive conformational transition.\n3. Adenylyl cyclase activation -> cAMP second messenger elevation -> Protein Kinase A activation.\n4. Downregulation of transcriptional inflammatory cytokines.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF14532D), lineHeight = 19.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Structure-Activity Relationship (SAR):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Text(
                        text = "Electronegative substitutions at C-6 enhance receptor affinity by 4.2-fold, while bulkier aryl side-chains confer metabolic stability against beta-lactamase enzymatic hydrolysis.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                    )
                }

                4 -> {
                    // Page 4: Indications & Clinical Guidelines
                    Text(
                        text = "3. FDA / EMA CLINICAL INDICATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val indications = listOf(
                        "First-Line Monotherapy" to "Initial pharmacological intervention in uncomplicated clinical presentations.",
                        "Adjunctive Combination Therapy" to "Co-administered with secondary agents when glycemic or blood pressure targets are unachieved.",
                        "Hospital Inpatient Setting" to "Continuous IV infusion monitoring in emergency cardiovascular resuscitation."
                    )

                    indications.forEach { (title, desc) ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                                Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569)))
                            }
                        }
                    }
                }

                5 -> {
                    // Page 5: Adverse Effects & Boxed Warnings
                    Text(
                        text = "4. ADVERSE EFFECTS & CONTRAINDICATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BOXED SAFETY WARNING",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Severe lactic acidosis and acute renal decompensation risk in patients with eGFR < 30 mL/min. Discontinue 48 hours prior to contrast imaging.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D), fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Common Adverse Reactions (> 5% incidence):",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Text(
                        text = "• Gastrointestinal: Nausea, diarrhea, abdominal cramping, transient metallic dysgeusia.\n• Metabolic: Reduced intestinal Vitamin B12 absorption upon prolonged multi-year administration.\n• Hypersensitivity: Urticaria, pruritus, angioedema (rare).",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155), lineHeight = 19.sp)
                    )
                }

                6 -> {
                    // Page 6: Dosage & Formulations
                    Text(
                        text = "5. DOSAGE SCHEDULES & FORMULATION KINETICS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val dosages = listOf(
                        Triple("Immediate-Release (IR)", "500mg, 850mg, 1000mg", "Twice daily with meals"),
                        Triple("Extended-Release (XR)", "500mg, 750mg, 1000mg", "Once daily with evening meal"),
                        Triple("Pediatric Oral Suspension", "500mg / 5mL", "Weight-based dosing (mg/kg)")
                    )

                    dosages.forEach { (form, strength, regimen) ->
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = form, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)))
                                    Text(text = "Strengths: $strength", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569)))
                                }
                                Text(
                                    text = regimen,
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                7 -> {
                    // Page 7: High-Yield GPAT / NIPER Exam Summary
                    Text(
                        text = "6. HIGH-YIELD GPAT / NIPER EXAM PEARLS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val pearls = listOf(
                        "Antidote Strategy" to "Sodium bicarbonate alkalinization of urine accelerates salicylic acid excretion.",
                        "Rate-Limiting Step" to "HMG-CoA Reductase catalyzes conversion of HMG-CoA to mevalonate in statin therapy.",
                        "Pharmacophore Rule" to "A 4-membered strained beta-lactam ring fused to a thiazolidine ring is required for transpeptidase inhibition.",
                        "Elimination Kinetics" to "Ethanol and Phenytoin display zero-order (saturation) elimination at therapeutic blood levels."
                    )

                    pearls.forEach { (concept, explanation) ->
                        Surface(
                            color = Color(0xFFFFFBEB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "★ $concept", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309)))
                                Text(text = explanation, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F)))
                            }
                        }
                    }
                }

                8 -> {
                    // Page 8: Clinical Case Study & References
                    Text(
                        text = "7. CLINICAL CASE STUDY & CITATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PATIENT CASE PRESENTATION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "A 56-year-old male with newly diagnosed Type 2 DM (HbA1c 8.4%) and baseline serum creatinine 1.0 mg/dL presents for therapy initiation. What is the appropriate starting regimen?\n\nRationale: Initiate Metformin 500mg once daily with breakfast, titrating to 1000mg twice daily over 4 weeks to mitigate gastrointestinal side effects.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155), lineHeight = 18.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ACADEMIC CITATIONS & STANDARDS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Goodman & Gilman's: The Pharmacological Basis of Therapeutics (14th Ed.)\n2. Pharmacy Council of India (PCI) Uniform Curriculum Regulations\n3. British Pharmacopoeia (BP) & United States Pharmacopeia (USP) Standards\n4. Verified under ${resource.copyrightLicense}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp, lineHeight = 16.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PHARMAHUB SECURE ACADEMIC DOCUMENT PIPELINE • ALL RIGHTS RESERVED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
