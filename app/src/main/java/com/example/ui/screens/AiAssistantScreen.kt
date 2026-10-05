package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AiExplanationMode
import com.example.service.DetailedDrugProfile
import com.example.ui.viewmodel.PharmaHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.aiMessages.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    val selectedDrug by viewModel.selectedDrugProfile.collectAsState()
    val isDrugResolving by viewModel.isDrugResolving.collectAsState()
    val drugResolutionError by viewModel.drugResolutionError.collectAsState()
    val currentAiMode by viewModel.aiMode.collectAsState()

    var activeViewTab by remember { mutableIntStateOf(0) } // 0 = Drug Search & Profile, 1 = AI Consultation Chat
    var drugSearchInput by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

    val quickDrugSuggestions = listOf("Paracetamol", "Ibuprofen", "Metformin", "Amoxicillin", "Aspirin", "Omeprazole")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
            .testTag("ai_assistant_screen")
    ) {
        // 1. Header with Mode Selector and Clear Action
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Drug & Pharmacy Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Authoritative pharmacology & clinical retrieval",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.clearAiChat()
                        viewModel.clearSelectedDrugProfile()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs: Drug Profile vs AI Chat
            TabRow(
                selectedTabIndex = activeViewTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) }
            ) {
                Tab(
                    selected = activeViewTab == 0,
                    onClick = { activeViewTab = 0 },
                    text = { Text("Drug Search", fontWeight = if (activeViewTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = activeViewTab == 1,
                    onClick = { activeViewTab = 1 },
                    text = { Text("AI Tutor Chat", fontWeight = if (activeViewTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // VIEW 0: Drug Search & Structured Drug Information Page
        if (activeViewTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Search Bar: "Search a drug, class, mechanism or pharmacy topic..."
                OutlinedTextField(
                    value = drugSearchInput,
                    onValueChange = { drugSearchInput = it },
                    placeholder = { Text("Search a drug (e.g. Metformin, PCM, Tylenol)...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Drug") },
                    trailingIcon = {
                        if (isDrugResolving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (drugSearchInput.isNotBlank()) {
                            IconButton(onClick = {
                                drugSearchInput = ""
                                viewModel.clearSelectedDrugProfile()
                            }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (drugSearchInput.isNotBlank()) {
                            viewModel.searchAndResolveDrug(drugSearchInput.trim())
                        }
                    }),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickDrugSuggestions) { name ->
                        SuggestionChip(
                            onClick = {
                                drugSearchInput = name
                                viewModel.searchAndResolveDrug(name)
                            },
                            label = { Text(name, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Error or Disambiguation State
                drugResolutionError?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // If Drug Profile is resolved, display structured view
                if (selectedDrug != null) {
                    val drug = selectedDrug!!
                    if (drug.isAmbiguous) {
                        // Ambiguity clarification cards
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Multiple drug concepts matched '$drugSearchInput':",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Please select the specific drug concept to view authoritative monograph:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                drug.ambiguousCandidates.forEach { candidate ->
                                    Button(
                                        onClick = {
                                            drugSearchInput = candidate
                                            viewModel.searchAndResolveDrug(candidate)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(candidate)
                                    }
                                }
                            }
                        }
                    } else {
                        // Render full Structured Drug Information Page
                        StructuredDrugInformationView(
                            drug = drug,
                            onAskAiDetailed = {
                                activeViewTab = 1
                                val prompt = "Explain $drug with mechanism of action, clinical indications, and pharmacokinetics."
                                viewModel.sendAiPrompt(prompt, subjectContext = "Pharmacology")
                            }
                        )
                    }
                } else if (!isDrugResolving && drugResolutionError == null) {
                    // Empty placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Enter a drug name to view structured monograph",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Resolves generic names, brand names, synonyms, and typos.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
        } else {
            // VIEW 1: AI Tutor Chat
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Mode Selector: Simple / Detailed / Exam Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode: ${currentAiMode.label}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AiExplanationMode.values().forEach { mode ->
                            val isSel = currentAiMode == mode
                            TextButton(
                                onClick = { viewModel.setAiMode(mode) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (mode) {
                                        AiExplanationMode.SIMPLE -> "Simple"
                                        AiExplanationMode.DETAILED -> "Detailed"
                                        AiExplanationMode.EXAM_MODE -> "Exam"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        AiMessageBubble(msg = msg, onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AI Answer", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        })
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Analyzing pharmacopoeia and clinical literature...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    aiError?.let { err ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.retryLastAiPrompt() }) {
                                        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Retry")
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask a drug mechanism, question, or calculation...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                viewModel.sendAiPrompt(chatInput.trim())
                                chatInput = ""
                            }
                        },
                        enabled = chatInput.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                color = if (chatInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (chatInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StructuredDrugInformationView(
    drug: DetailedDrugProfile,
    onAskAiDetailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Title Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = drug.drugName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Generic: ${drug.genericName}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    )
                    if (drug.brandNames.isNotEmpty()) {
                        Text(
                            text = "Brands: ${drug.brandNames.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Class: ${drug.drugClass}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // 2. Clinical Indications & Uses
        item {
            DrugSectionCard(title = "Clinical Indications / Labeled Uses") {
                drug.indications.forEach { ind ->
                    Text("• $ind", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 3. Mechanism of Action
        item {
            DrugSectionCard(title = "Mechanism of Action & Pharmacological Action") {
                Text(drug.mechanismOfAction, style = MaterialTheme.typography.bodySmall)
                if (drug.pharmacologicalAction.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pharmacological Action: ${drug.pharmacologicalAction}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                }
            }
        }

        // 4. Pharmacokinetics (ADME)
        item {
            DrugSectionCard(title = "Pharmacokinetics (ADME)") {
                Text("• Absorption: ${drug.absorption}", style = MaterialTheme.typography.bodySmall)
                Text("• Distribution: ${drug.distribution}", style = MaterialTheme.typography.bodySmall)
                Text("• Metabolism: ${drug.metabolism}", style = MaterialTheme.typography.bodySmall)
                Text("• Excretion: ${drug.excretion}", style = MaterialTheme.typography.bodySmall)
                Text("• Elimination Half-life (t½): ${drug.halfLife}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }

        // 5. Adverse Effects & Warnings
        item {
            DrugSectionCard(title = "Adverse Effects & Serious Warnings") {
                if (drug.seriousWarnings.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            drug.seriousWarnings.forEach { warn ->
                                Text("⚠️ $warn", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
                Text("Common Side Effects:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                drug.commonAdverseEffects.forEach { eff ->
                    Text("• $eff", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 6. Contraindications & Precautions
        item {
            DrugSectionCard(title = "Contraindications & Clinical Precautions") {
                Text("Contraindications:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                drug.contraindications.forEach { c ->
                    Text("• $c", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Precautions:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                drug.precautions.forEach { p ->
                    Text("• $p", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 7. Interactions
        item {
            DrugSectionCard(title = "Drug & Food Interactions") {
                drug.drugInteractions.forEach { di ->
                    Text("• $di", style = MaterialTheme.typography.bodySmall)
                }
                if (drug.foodInteractions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Food Interactions: ${drug.foodInteractions}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 8. Special Populations & Pregnancy
        item {
            DrugSectionCard(title = "Pregnancy & Lactation") {
                Text(drug.pregnancyLactation, style = MaterialTheme.typography.bodySmall)
            }
        }

        // 9. Dosage Forms & Chemical Data
        item {
            DrugSectionCard(title = "Formulations & Chemical Identifiers") {
                Text("• Dosage Forms: ${drug.dosageForms.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Available Strengths: ${drug.strengths.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Routes: ${drug.routes.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Molecular Formula: ${drug.molecularFormula} (MW: ${drug.molecularWeight})", style = MaterialTheme.typography.bodySmall)
                Text("• PubChem CID: ${drug.pubChemCid} • RxNorm: ${drug.rxNormRxcui}", style = MaterialTheme.typography.bodySmall)
                Text("• Regulatory: ${drug.regulatoryStatus}", style = MaterialTheme.typography.bodySmall)
            }
        }

        // 10. Authoritative Source Citations
        item {
            DrugSectionCard(title = "Authoritative Sources") {
                drug.sources.forEach { src ->
                    Text("✓ ${src.sourceName}: ${src.databaseRecord}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
                }
            }
        }

        // 11. Safety Disclaimer Banner
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DISCLAIMER: Educational pharmacy monograph only. Not intended for direct medical diagnosis, prescribing, or clinical treatment decisions. Consult qualified healthcare professionals.",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Action Button: Ask AI Detailed Question
        item {
            Button(
                onClick = onAskAiDetailed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask AI Detailed Question About ${drug.drugName}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DrugSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

@Composable
fun AiMessageBubble(
    msg: com.example.ui.viewmodel.AiChatMessage,
    onCopy: (String) -> Unit
) {
    val isUser = msg.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (!isUser && msg.citations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sources: ${msg.citations.joinToString("; ")}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    )
                }

                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onCopy(msg.text) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
