package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResourceEntity
import com.example.ui.components.*
import com.example.ui.theme.LocalGlowColor
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun HomeScreen(
    viewModel: PharmaHubViewModel,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.studyAnalytics.collectAsState()
    val resources by viewModel.allResources.collectAsState()
    val highYieldResources = remember(resources) { resources.take(4) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Top Bar / Command Launcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PHARMAHUB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Master Pharmacy",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onOpenSearch,
                        modifier = Modifier
                            .testTag("search_action_button")
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Study Assistant",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Futuristic Hero Section with 3D Rotating Capsule Canvas
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("hero_banner_card"),
                elevation = 6,
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ) {
                // Interactive 3D Canvas
                Capsule3DCanvas(
                    modifier = Modifier.padding(bottom = 8.dp),
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "LEARN BETTER.\nSTUDY SMARTER.",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Pharmacy. Your Knowledge. Your Community. Everything you need to study, collaborate and grow as a pharmacy scholar.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.RESOURCES) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("explore_resources_hero_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryBooks,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explore Resources",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.FLASHCARDS) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("start_studying_hero_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Start Studying",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // 3. Personalized Greeting & Live Academic Stats
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Good Morning, Rishi 👋",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Ready to master Pharmacology today?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${analytics?.streakDays ?: 8} Days",
                        label = "Study Streak",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = Icons.Default.Bolt,
                        value = "${analytics?.xpEarned ?: 1850} XP",
                        label = "Level ${analytics?.userLevel ?: 6}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        icon = Icons.Default.Verified,
                        value = "${analytics?.quizAccuracyPercent ?: 86}%",
                        label = "Quiz Accuracy",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Quick Action Hub (Horizontal Scroll Cards)
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                PharmaSectionHeader(
                    title = "Academic Workspace",
                    subtitle = "Jump into active pharmacy modules",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val modules = listOf(
                        Triple("Drug Library", Icons.Default.Medication, AppScreen.DRUG_LIBRARY),
                        Triple("Quiz Engine", Icons.Default.Quiz, AppScreen.QUIZ),
                        Triple("Flashcards", Icons.Default.Style, AppScreen.FLASHCARDS),
                        Triple("Study Groups", Icons.Default.Forum, AppScreen.CHAT),
                        Triple("Community", Icons.Default.Groups, AppScreen.COMMUNITY),
                        Triple("AI Tutor", Icons.Default.AutoAwesome, AppScreen.AI_ASSISTANT)
                    )

                    items(modules) { (name, icon, targetScreen) ->
                        Surface(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { viewModel.navigateTo(targetScreen) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Weekly Study Analytics & Diagnostic Focus Area
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Study Hours",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "42.0 total hours logged this week",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+18% vs last week",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                StudyHoursWeeklyChart()

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Diagnostic Weak Area Detected",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = analytics?.weakArea ?: "Pharmacokinetics (Clearance & Vd)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.FLASHCARDS) }) {
                        Text("Revise Now", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Exam Countdown Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPAT 2026 Examination",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Target Score: 320+ • 148 Days Remaining",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.QUIZ) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Mock Test", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary))
                    }
                }
            }
        }

        // 7. Recommended High-Yield Resources
        item {
            PharmaSectionHeader(
                title = "High-Yield Resources",
                subtitle = "Handpicked notes and solved papers",
                actionText = "See All",
                onActionClick = { viewModel.navigateTo(AppScreen.RESOURCES) },
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
            )
        }

        items(highYieldResources) { res ->
            ResourceItemRow(
                resource = res,
                onBookmarkClick = { viewModel.toggleResourceBookmark(res) },
                onDownloadClick = { viewModel.recordDownload(res) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ResourceItemRow(
    resource: ResourceEntity,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDownloadClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${resource.subject} • Sem ${resource.semester} • By ${resource.author}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${resource.rating} (${resource.reviewCount})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${resource.downloads} downloads",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    }
}
