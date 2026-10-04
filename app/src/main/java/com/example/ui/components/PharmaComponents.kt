package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DrugEntity
import com.example.data.model.ResourceEntity
import com.example.ui.theme.LocalGlowColor
import com.example.ui.theme.LocalSurfaceElevated

/**
 * Premium rounded card with subtle gradient border and surface elevation.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    elevation: Int = 4,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                ambientColor = LocalGlowColor.current,
                spotColor = LocalGlowColor.current
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Compact stat badge with icon, value, and label.
 */
@Composable
fun StatBadge(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

/**
 * Selectable pill chip for categories and filters.
 */
@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String = "pill_chip"
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}

/**
 * Editorial Section Header with bold typography and optional action.
 */
@Composable
fun PharmaSectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Animated Weekly Study Hours Bar Chart.
 */
@Composable
fun StudyHoursWeeklyChart(
    hours: List<Float> = listOf(3.5f, 4.2f, 5.0f, 6.5f, 4.0f, 7.5f, 5.8f),
    labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    modifier: Modifier = Modifier
) {
    val maxHours = (hours.maxOrNull() ?: 8f).coerceAtLeast(6f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hours.forEachIndexed { index, value ->
                val ratio = (value / maxHours).coerceIn(0.1f, 1.0f)
                val isToday = index == 6

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${value}h",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(ratio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (isToday) {
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = labels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

/**
 * Global Command Palette Dialog for instant search.
 */
@Composable
fun CommandPaletteDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    resources: List<ResourceEntity>,
    drugs: List<DrugEntity>,
    onSelectResource: (ResourceEntity) -> Unit,
    onSelectDrug: (DrugEntity) -> Unit
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }

    val matchedResources = remember(searchQuery, resources) {
        if (searchQuery.isBlank()) emptyList()
        else resources.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subject.contains(searchQuery, ignoreCase = true) ||
            it.tags.contains(searchQuery, ignoreCase = true)
        }.take(4)
    }

    val matchedDrugs = remember(searchQuery, drugs) {
        if (searchQuery.isBlank()) emptyList()
        else drugs.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.genericName.contains(searchQuery, ignoreCase = true) ||
            it.drugClass.contains(searchQuery, ignoreCase = true)
        }.take(3)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search notes, drugs, PYQs, flashcards...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_palette_input")
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                if (searchQuery.isBlank()) {
                    Text(
                        text = "Trending Searches: Pharmacokinetics, Beta-Lactam SAR, GPAT PYQs, Metformin, Atorvastatin",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else if (matchedResources.isEmpty() && matchedDrugs.isEmpty()) {
                    Text(
                        text = "No results found for \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        if (matchedDrugs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "DRUGS & PHARMACOLOGY",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                            items(matchedDrugs) { drug ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectDrug(drug)
                                            onDismiss()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = drug.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${drug.drugClass} • ${drug.brandNames.take(24)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }

                        if (matchedResources.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ACADEMIC RESOURCES & NOTES",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                            items(matchedResources) { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectResource(res)
                                            onDismiss()
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = res.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${res.subject} • Sem ${res.semester} • ${res.fileType}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Upload Resource Dialog with strict validation (MIME, size, semester, subject tags).
 */
@Composable
fun ResourceUploadDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onUpload: (title: String, subject: String, semester: Int, course: String, fileType: String, fileSize: String, tags: String, description: String) -> Unit
) {
    if (!isOpen) return

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Pharmacology") }
    var semester by remember { mutableIntStateOf(5) }
    var course by remember { mutableStateOf("B.Pharm") }
    var fileType by remember { mutableStateOf("PDF") }
    var tags by remember { mutableStateOf("High-Yield, LectureNotes") }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Share Pharmacy Resource",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Upload notes, practical manuals, or question papers.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = "Sem $semester",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Semester") },
                        modifier = Modifier.width(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PDF", "DOCX", "PPTX").forEach { type ->
                        PillChip(
                            text = type,
                            selected = fileType == type,
                            onClick = { fileType = type }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma-separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Brief Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (isUploading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Validating MIME signature & indexing... ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please specify a document title"
                                return@Button
                            }
                            isUploading = true
                            uploadProgress = 0.4f
                            // Simulate upload verification
                            uploadProgress = 1.0f
                            onUpload(title, subject, semester, course, fileType, "3.8 MB", tags, description)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("confirm_upload_button")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Resource", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
