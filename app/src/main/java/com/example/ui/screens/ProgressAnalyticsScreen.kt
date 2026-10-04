package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.components.StatBadge
import com.example.ui.components.StudyHoursWeeklyChart
import com.example.ui.viewmodel.PharmaHubViewModel

data class AchievementBadge(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isUnlocked: Boolean,
    val color: Color
)

@Composable
fun ProgressAnalyticsScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.studyAnalytics.collectAsState()

    val badges = remember {
        listOf(
            AchievementBadge("Pharmacology Master", "Solved over 50 ADME & pharmacokinetics problems", Icons.Default.Biotech, true, Color(0xFF00E5A3)),
            AchievementBadge("7-Day Study Streak", "Maintained daily study consistency for 7+ consecutive days", Icons.Default.LocalFireDepartment, true, Color(0xFFF59E0B)),
            AchievementBadge("Top Contributor", "Shared verified lecture notes downloaded over 100 times", Icons.Default.CloudUpload, true, Color(0xFF38BDF8)),
            AchievementBadge("Quiz Ace", "Scored 100% on a full-length GPAT mock examination", Icons.Default.EmojiEvents, true, Color(0xFF8B5CF6)),
            AchievementBadge("Flashcard Titan", "Mastered 150+ spaced-repetition cards in Box 5", Icons.Default.Style, true, Color(0xFFEC4899)),
            AchievementBadge("Drug Encyclopedia", "Explored 25+ drug monographs and clinical interactions", Icons.Default.Medication, false, Color(0xFF94A3B8))
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Academic Analytics",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Your real-time study velocity and mastery metrics",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats Overview Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${analytics?.streakDays ?: 8} Days",
                    label = "Current Streak",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    icon = Icons.Default.AccessTime,
                    value = "${analytics?.totalStudyHours ?: 42f}h",
                    label = "Total Hours",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    icon = Icons.Default.Bolt,
                    value = "${analytics?.xpEarned ?: 1850}",
                    label = "Total XP",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Weekly Study Chart Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Weekly Study Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Consistent daily study hours vs weekly target",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                StudyHoursWeeklyChart()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Diagnostic Focus Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Study Prescription:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Weak area: ${analytics?.weakArea ?: "Pharmacokinetics"}\n• Recommended focus: ${analytics?.recommendedTopic ?: "Renal Clearance and First-Pass Metabolism"}\n• Next milestone: Complete 15 flashcards to unlock Level 7.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Badges & Achievements
        item {
            PharmaSectionHeader(
                title = "Earned Badges & Honors",
                subtitle = "Academic milestones unlocked through study sprints"
            )
        }

        items(badges.size) { idx ->
            val b = badges[idx]
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (b.isUnlocked) b.color.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (b.isUnlocked) b.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = b.icon, contentDescription = null, tint = if (b.isUnlocked) b.color else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = b.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = b.description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    if (b.isUnlocked) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Unlocked", tint = b.color, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
