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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun ChatScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val selectedRoom by viewModel.selectedChatRoom.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    var inputMessage by remember { mutableStateOf("") }

    val studyRooms = listOf(
        Pair("group_pharmacology", "Pharmacology Squad"),
        Pair("group_gpat", "GPAT Prep 2026"),
        Pair("group_bpharm7", "B.Pharm 7th Sem"),
        Pair("direct_rishi", "Dr. Ray (Office Hours)")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Room Selector Pills
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Study Groups & Secure Chat",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "End-to-end encrypted academic collaboration rooms",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(studyRooms) { (roomId, roomName) ->
                    PillChip(
                        text = roomName,
                        selected = selectedRoom == roomId,
                        onClick = { viewModel.selectChatRoom(roomId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Chat Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageBubble(message = msg)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Input Field
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
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Message this study group...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            val text = inputMessage
                            inputMessage = ""
                            viewModel.sendChatMessage(text)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("chat_send_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isSelf = message.isSelf

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        if (!isSelf) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 290.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isSelf) 16.dp else 4.dp,
                bottomEnd = if (isSelf) 4.dp else 16.dp
            ),
            color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = if (isSelf) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isSelf) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.senderRole,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelf) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                )

                if (message.attachmentTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = message.attachmentTitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = if (isSelf) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
