package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun AiAssistantScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.aiMessages.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val presetPrompts = listOf(
        "Explain Bioavailability vs Volume of Distribution",
        "Summarize SAR of Beta-Lactam Antibiotics",
        "Explain Digoxin Toxicity & Hypokalemia mechanism",
        "Pediatric dose calculations using Young's Rule"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        text = "PharmaHub AI Tutor",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Academic pharmacology and pharmaceutical sciences consultations",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetPrompts) { prompt ->
                    PillChip(
                        text = prompt,
                        selected = false,
                        onClick = { viewModel.sendAiPrompt(prompt) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                AiMessageBubble(message = msg)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Synthesizing textbook citations & pharmacodynamics...",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about pharmacology, ADME, SAR...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input_field")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val promptToSend = inputText
                            inputText = ""
                            viewModel.sendAiPrompt(promptToSend)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("ai_assistant_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AiMessageBubble(message: AiChatMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                )

                if (message.keyPoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Key Clinical Pearls:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    message.keyPoints.forEach { point ->
                        Text(
                            text = "• $point",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (message.citations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📚 Citations:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    message.citations.forEach { cit ->
                        Text(
                            text = cit,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
