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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PharmaceuticalDrug
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugKnowledgeScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val pharmaDrugs by viewModel.pharmaceuticalDrugs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedClassification by remember { mutableStateOf("All") }
    var selectedDrugDetail by remember { mutableStateOf<PharmaceuticalDrug?>(null) }
    var isAddDrugDialogOpen by remember { mutableStateOf(false) }

    val classifications = remember(pharmaDrugs) {
        listOf("All") + pharmaDrugs.map { it.classification }.distinct().sorted()
    }

    val filteredDrugs = remember(pharmaDrugs, searchQuery, selectedClassification) {
        pharmaDrugs.filter { drug ->
            val matchQuery = searchQuery.isBlank() ||
                    drug.name.contains(searchQuery, ignoreCase = true) ||
                    drug.genericName.contains(searchQuery, ignoreCase = true) ||
                    drug.brandName.contains(searchQuery, ignoreCase = true) ||
                    drug.classification.contains(searchQuery, ignoreCase = true) ||
                    drug.indications.contains(searchQuery, ignoreCase = true) ||
                    drug.sideEffects.contains(searchQuery, ignoreCase = true)

            val matchClass = if (selectedClassification == "All") true else drug.classification.contains(selectedClassification, ignoreCase = true)
            matchQuery && matchClass
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDrugDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("add_pharmaceutical_drug_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Drug")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Drug", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Pharmaceutical Drugs Library",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Local Room database repository for drug classifications, indications & side effects",
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
                        placeholder = { Text("Search by name, classification, indication, or side effect...") },
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
                    items(classifications) { cls ->
                        PillChip(
                            text = cls,
                            selected = selectedClassification == cls,
                            onClick = { selectedClassification = cls }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Drug Count
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredDrugs.size} Drugs in Library",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Drug Items
            items(filteredDrugs) { drug ->
                PharmaceuticalDrugCard(
                    drug = drug,
                    onClick = { selectedDrugDetail = drug },
                    onFavorite = { viewModel.togglePharmaceuticalDrugFavorite(drug) },
                    onDelete = { viewModel.deletePharmaceuticalDrug(drug.id) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }

    // Add Pharmaceutical Drug Dialog
    if (isAddDrugDialogOpen) {
        AddPharmaceuticalDrugDialog(
            onDismiss = { isAddDrugDialogOpen = false },
            onAdd = { name, classification, indications, sideEffects, generic, brand, contra, moa, dose ->
                viewModel.addPharmaceuticalDrug(
                    name = name,
                    classification = classification,
                    indications = indications,
                    sideEffects = sideEffects,
                    genericName = generic,
                    brandName = brand,
                    contraindications = contra,
                    mechanismOfAction = moa,
                    dosage = dose
                )
                isAddDrugDialogOpen = false
            }
        )
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
                                    text = drug.classification,
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
                        if (drug.brandName.isNotBlank() || drug.genericName.isNotBlank()) {
                            Text(
                                text = "Generic: ${drug.genericName.ifEmpty { drug.name }} • Brands: ${drug.brandName.ifEmpty { "N/A" }}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        DrugDetailSection(
                            title = "Drug Classification",
                            icon = Icons.Default.Category,
                            content = drug.classification
                        )
                        DrugDetailSection(
                            title = "Approved Indications",
                            icon = Icons.Default.CheckCircle,
                            content = drug.indications
                        )
                        DrugDetailSection(
                            title = "Side Effects & Adverse Reactions",
                            icon = Icons.Default.Sick,
                            content = drug.sideEffects,
                            color = Color(0xFFEF4444)
                        )
                        if (drug.mechanismOfAction.isNotBlank()) {
                            DrugDetailSection(
                                title = "Mechanism of Action (MOA)",
                                icon = Icons.Default.Biotech,
                                content = drug.mechanismOfAction
                            )
                        }
                        if (drug.contraindications.isNotBlank()) {
                            DrugDetailSection(
                                title = "Contraindications",
                                icon = Icons.Default.Warning,
                                content = drug.contraindications,
                                color = Color(0xFFEF4444)
                            )
                        }
                        if (drug.dosage.isNotBlank()) {
                            DrugDetailSection(
                                title = "Standard Dosage & Administration",
                                icon = Icons.Default.Medication,
                                content = drug.dosage
                            )
                        }
                        if (drug.dosageForms.isNotBlank()) {
                            DrugDetailSection(
                                title = "Dosage Forms",
                                icon = Icons.Default.Inventory2,
                                content = drug.dosageForms
                            )
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
fun PharmaceuticalDrugCard(
    drug: PharmaceuticalDrug,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pharmaceutical_drug_${drug.id}"),
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
                        text = drug.classification,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFavorite) {
                        Icon(
                            imageVector = if (drug.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (drug.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Drug",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = drug.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (drug.brandName.isNotBlank()) {
                Text(
                    text = "Brands: ${drug.brandName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indications
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Indications: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = drug.indications,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Side Effects
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Side Effects: ",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                )
                Text(
                    text = drug.sideEffects,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (drug.dosage.isNotBlank()) "Dosage: ${drug.dosage.take(24)}..." else "Local Library",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                )
                Text(
                    text = "View Details →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun AddPharmaceuticalDrugDialog(
    onDismiss: () -> Unit,
    onAdd: (
        name: String,
        classification: String,
        indications: String,
        sideEffects: String,
        generic: String,
        brand: String,
        contra: String,
        moa: String,
        dosage: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf("") }
    var indications by remember { mutableStateOf("") }
    var sideEffects by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var brandName by remember { mutableStateOf("") }
    var contraindications by remember { mutableStateOf("") }
    var mechanismOfAction by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Text(
                        text = "Add Pharmaceutical Drug",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Store new drug record in local Room database library",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Drug Name *") },
                        placeholder = { Text("e.g. Ciprofloxacin Hydrochloride") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = classification,
                        onValueChange = { classification = it },
                        label = { Text("Classification *") },
                        placeholder = { Text("e.g. Fluoroquinolone Antibacterial") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = indications,
                        onValueChange = { indications = it },
                        label = { Text("Indications *") },
                        placeholder = { Text("e.g. Complicated UTIs, infectious diarrhea, anthrax") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sideEffects,
                        onValueChange = { sideEffects = it },
                        label = { Text("Side Effects *") },
                        placeholder = { Text("e.g. Tendinitis/tendon rupture, QT prolongation, GI upset") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mechanismOfAction,
                        onValueChange = { mechanismOfAction = it },
                        label = { Text("Mechanism of Action (Optional)") },
                        placeholder = { Text("e.g. Inhibits bacterial DNA gyrase and topoisomerase IV") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Standard Dosage (Optional)") },
                        placeholder = { Text("e.g. 250mg - 500mg twice daily") },
                        singleLine = true,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    errorMessage = "Please enter drug name"
                                    return@Button
                                }
                                if (classification.isBlank()) {
                                    errorMessage = "Please enter drug classification"
                                    return@Button
                                }
                                if (indications.isBlank()) {
                                    errorMessage = "Please enter indications"
                                    return@Button
                                }
                                if (sideEffects.isBlank()) {
                                    errorMessage = "Please enter side effects"
                                    return@Button
                                }

                                onAdd(
                                    name,
                                    classification,
                                    indications,
                                    sideEffects,
                                    genericName,
                                    brandName,
                                    contraindications,
                                    mechanismOfAction,
                                    dosage
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Save to Library", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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

