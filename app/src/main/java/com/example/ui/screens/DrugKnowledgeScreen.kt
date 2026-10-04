package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DrugEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun DrugKnowledgeScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val allDrugs by viewModel.allDrugs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedDrugClass by remember { mutableStateOf("All") }
    var selectedDrugDetail by remember { mutableStateOf<DrugEntity?>(null) }

    val drugClasses = listOf("All", "Antidiabetic", "Statin", "PPI", "Antibiotic", "ACE Inhibitor", "Beta-2 Agonist")

    val filteredDrugs = remember(allDrugs, searchQuery, selectedDrugClass) {
        allDrugs.filter { drug ->
            val matchQuery = searchQuery.isBlank() ||
                    drug.name.contains(searchQuery, ignoreCase = true) ||
                    drug.genericName.contains(searchQuery, ignoreCase = true) ||
                    drug.brandNames.contains(searchQuery, ignoreCase = true) ||
                    drug.drugClass.contains(searchQuery, ignoreCase = true)

            val matchClass = if (selectedDrugClass == "All") true else drug.drugClass.contains(selectedDrugClass, ignoreCase = true)
            matchQuery && matchClass
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Drug Knowledge Module",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Clinical pharmacology monographs, SAR, and ADME profiles",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Prominent Medical Educational Disclaimer
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = "Medical Disclaimer",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Educational information only. This platform does not diagnose, prescribe, or replace professional medical or pharmacotherapeutic advice.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by drug, brand, or class...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drug_search_field")
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(drugClasses) { cls ->
                    PillChip(
                        text = cls,
                        selected = selectedDrugClass == cls,
                        onClick = { selectedDrugClass = cls }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Drug Items
        items(filteredDrugs) { drug ->
            DrugCatalogCard(
                drug = drug,
                onClick = { selectedDrugDetail = drug },
                onBookmark = { viewModel.toggleDrugBookmark(drug) },
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }

    // Detailed Drug Monograph Modal
    if (selectedDrugDetail != null) {
        val drug = selectedDrugDetail!!
        Dialog(onDismissRequest = { selectedDrugDetail = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(20.dp)) {
                    item {
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
                                    text = drug.drugClass,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(onClick = { selectedDrugDetail = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = drug.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Generic: ${drug.genericName} • Brands: ${drug.brandNames}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        DrugDetailSection(
                            title = "Mechanism of Action (MOA)",
                            icon = Icons.Default.Biotech,
                            content = drug.mechanismOfAction
                        )
                        DrugDetailSection(
                            title = "Therapeutic Indications",
                            icon = Icons.Default.CheckCircle,
                            content = drug.indications
                        )
                        DrugDetailSection(
                            title = "Contraindications & Warnings",
                            icon = Icons.Default.Warning,
                            content = drug.contraindications,
                            color = Color(0xFFEF4444)
                        )
                        DrugDetailSection(
                            title = "Adverse Effects",
                            icon = Icons.Default.Sick,
                            content = drug.adverseEffects
                        )
                        DrugDetailSection(
                            title = "Drug-Drug Interactions",
                            icon = Icons.Default.SwapHoriz,
                            content = drug.interactions
                        )
                        DrugDetailSection(
                            title = "Dosage Forms & Storage",
                            icon = Icons.Default.Inventory2,
                            content = "${drug.dosageForms}\nStorage: ${drug.storageInstructions}"
                        )

                        if (drug.highYieldGpatFacts.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🎯 GPAT High-Yield Exam Facts:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = drug.highYieldGpatFacts,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { selectedDrugDetail = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Monograph", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrugCatalogCard(
    drug: DrugEntity,
    onClick: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("drug_card_${drug.id}"),
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = drug.drugClass,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onBookmark) {
                    Icon(
                        imageVector = if (drug.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (drug.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = drug.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Brands: ${drug.brandNames}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = drug.mechanismOfAction,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Half-life: ${drug.halfLife}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                )
                Text(
                    text = "View Monograph →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun DrugDetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        )
    }
}
