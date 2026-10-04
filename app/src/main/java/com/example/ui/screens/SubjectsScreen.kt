package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

data class SubjectInfo(
    val name: String,
    val code: String,
    val description: String,
    val icon: ImageVector,
    val progress: Float,
    val notesCount: Int,
    val cardsCount: Int,
    val color: Color
)

@Composable
fun SubjectsScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val subjects = remember {
        listOf(
            SubjectInfo("Pharmacology", "BP404T", "Mechanisms of drug action, pharmacokinetics, and clinical pharmacotherapeutics", Icons.Default.Biotech, 0.85f, 24, 48, Color(0xFF00E5A3)),
            SubjectInfo("Pharmaceutics", "BP103T", "Dosage form design, formulation sciences, bioavailability and industrial processes", Icons.Default.Science, 0.72f, 18, 36, Color(0xFF38BDF8)),
            SubjectInfo("Medicinal Chemistry", "BP501T", "Structure-Activity Relationships (SAR), chemical synthesis, and receptor binding", Icons.Default.BubbleChart, 0.64f, 16, 28, Color(0xFF8B5CF6)),
            SubjectInfo("Pharmacognosy", "BP305T", "Phytochemistry, botanical extracts, secondary metabolites, and herbal tech", Icons.Default.Spa, 0.90f, 14, 22, Color(0xFF10B981)),
            SubjectInfo("Biopharmaceutics", "BP604T", "Dissolution dynamics, compartmental ADME modeling, and IVIVC correlations", Icons.Default.Speed, 0.58f, 12, 18, Color(0xFFF59E0B)),
            SubjectInfo("Pharmaceutical Analysis", "BP102T", "UV-Vis, HPLC, NMR, Mass Spectrometry, and titrimetric quality validation", Icons.Default.Analytics, 0.80f, 15, 25, Color(0xFFFB7185)),
            SubjectInfo("Pharmacy Practice", "BP703T", "Hospital pharmacy, clinical rounds, ADR monitoring, and patient counseling", Icons.Default.LocalHospital, 0.75f, 10, 16, Color(0xFF06B6D4))
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
                    text = "Subject Explorer",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Pharmacy Council of India (PCI) core academic syllabus",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(subjects) { sub ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.navigateTo(AppScreen.RESOURCES) },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, sub.color.copy(alpha = 0.35f))
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
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(sub.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = sub.icon, contentDescription = null, tint = sub.color, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = sub.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Code: ${sub.code}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                        Text(
                            text = "${(sub.progress * 100).toInt()}% Done",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = sub.color)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sub.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { sub.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = sub.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "📄 ${sub.notesCount} Notes", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(text = "🃏 ${sub.cardsCount} Flashcards", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                        Text(
                            text = "Explore Module →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}
