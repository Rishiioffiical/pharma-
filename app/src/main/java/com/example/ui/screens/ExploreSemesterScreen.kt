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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PharmacyCourse
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun ExploreSemesterScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentCourse by viewModel.selectedCourse.collectAsState()
    val currentSem by viewModel.selectedSemester.collectAsState()

    val semesterSubjectsMap = remember {
        mapOf(
            1 to listOf("Human Anatomy & Physiology I", "Pharmaceutical Analysis I", "Pharmaceutics I", "Pharmaceutical Inorganic Chemistry"),
            2 to listOf("Human Anatomy & Physiology II", "Pharmaceutical Organic Chemistry I", "Biochemistry", "Pathophysiology"),
            3 to listOf("Pharmaceutical Organic Chemistry II", "Physical Pharmaceutics I", "Pharmaceutical Microbiology", "Pharmaceutical Engineering"),
            4 to listOf("Pharmaceutical Organic Chemistry III", "Medicinal Chemistry I", "Physical Pharmaceutics II", "Pharmacology I", "Pharmacognosy I"),
            5 to listOf("Medicinal Chemistry II", "Industrial Pharmacy I", "Pharmacology II", "Pharmacognosy and Phytochemistry II", "Pharmaceutical Jurisprudence"),
            6 to listOf("Medicinal Chemistry III", "Pharmacology III", "Herbal Drug Technology", "Biopharmaceutics & Pharmacokinetics", "Pharmaceutical Biotechnology", "Quality Assurance"),
            7 to listOf("Instrumental Methods of Analysis", "Industrial Pharmacy II", "Pharmacy Practice", "Novel Drug Delivery Systems"),
            8 to listOf("Biostatistics & Research Methodology", "Social & Preventive Pharmacy", "Pharma Marketing Management", "Regulatory Science")
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
                    text = "Semester Academic Map",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Personalize by course and active academic semester",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Course Selector
        item {
            Text(
                text = "Academic Program",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PharmacyCourse.values().forEach { c ->
                    PillChip(
                        text = c.displayName.split(" ")[0] + " (${c.name.replace("_", ".")})",
                        selected = currentCourse == c,
                        onClick = { viewModel.selectedCourse.value = c }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Semester Selector Grid
        item {
            Text(
                text = "Select Semester",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..currentCourse.semesters).toList()) { sem ->
                    val isSelected = currentSem == sem
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.selectedSemester.value = sem }
                            .size(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SEM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "$sem",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Subjects for selected semester
        item {
            Text(
                text = "Syllabus Subjects for Semester $currentSem",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val currentSubs = semesterSubjectsMap[currentSem] ?: listOf("Pharmacology", "Pharmaceutics", "Medicinal Chemistry")
        items(currentSubs) { subName ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.navigateTo(AppScreen.RESOURCES) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = subName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
