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
import com.example.data.model.CommunityPostEntity
import com.example.data.model.PostType
import com.example.ui.components.PillChip
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun CommunityScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.communityPosts.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var isCreatePostOpen by remember { mutableStateOf(false) }

    val filterTypes = listOf("All", "QUESTION", "STUDY_TIP", "POLL", "RESOURCE", "DISCUSSION")

    val filteredPosts = remember(posts, selectedFilter) {
        if (selectedFilter == "All") posts
        else posts.filter { it.postType == selectedFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatePostOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 76.dp)
                    .testTag("create_post_fab")
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Create Post")
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
            item {
                Column {
                    Text(
                        text = "Academic Community",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Collaborate with fellow pharmacy students, scholars & faculty",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTypes) { fType ->
                        PillChip(
                            text = when (fType) {
                                "All" -> "All Posts"
                                "QUESTION" -> "Questions"
                                "STUDY_TIP" -> "Study Tips"
                                "POLL" -> "Polls"
                                "RESOURCE" -> "Resources"
                                else -> "Discussions"
                            },
                            selected = selectedFilter == fType,
                            onClick = { selectedFilter = fType }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            items(filteredPosts) { post ->
                CommunityPostCard(
                    post = post,
                    onUpvote = { viewModel.togglePostUpvote(post) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    if (isCreatePostOpen) {
        CreatePostDialog(
            onDismiss = { isCreatePostOpen = false },
            onCreate = { pType, title, body, tags, pollOpts ->
                viewModel.createPost(pType, title, body, tags, pollOpts)
                isCreatePostOpen = false
            }
        )
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPostEntity,
    onUpvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top author and type
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
                        Text(
                            text = post.authorName.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${post.authorRole} • ${post.timestamp}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (post.postType) {
                        "QUESTION" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        "STUDY_TIP" -> Color(0xFF10B981).copy(alpha = 0.15f)
                        "POLL" -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = post.postType.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (post.postType) {
                                "QUESTION" -> Color(0xFFEF4444)
                                "STUDY_TIP" -> Color(0xFF10B981)
                                "POLL" -> Color(0xFF8B5CF6)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            )

            // Interactive Poll UI
            if (post.postType == "POLL" && post.pollOptionsJson.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val options = post.pollOptionsJson.split(";")
                val votes = post.pollVotesJson.split(";").mapNotNull { it.toIntOrNull() }
                val totalVotes = votes.sum().coerceAtLeast(1)

                var selectedVoteIndex by remember { mutableStateOf<Int?>(null) }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEachIndexed { idx, opt ->
                        val count = votes.getOrElse(idx) { 0 } + (if (selectedVoteIndex == idx) 1 else 0)
                        val pct = ((count.toFloat() / (totalVotes + if (selectedVoteIndex != null) 1 else 0)) * 100).toInt()

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVoteIndex = idx },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (selectedVoteIndex == idx) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(pct / 100f)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = opt, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                                    Text(text = "$pct%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom actions: Upvote & Comments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onUpvote)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Upvote",
                        tint = if (post.isUpvoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${post.upvotes}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (post.isUpvoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentsCount} replies", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onCreate: (postType: String, title: String, body: String, tags: String, pollOptions: String) -> Unit
) {
    var postType by remember { mutableStateOf("QUESTION") }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Pharmacology, GPAT") }
    var pollOptions by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Create Community Post",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("QUESTION", "STUDY_TIP", "POLL", "DISCUSSION").forEach { pt ->
                        PillChip(
                            text = when (pt) {
                                "QUESTION" -> "Question"
                                "STUDY_TIP" -> "Study Tip"
                                "POLL" -> "Poll"
                                else -> "Discussion"
                            },
                            selected = postType == pt,
                            onClick = { postType = pt }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Post Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Post Content *") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                if (postType == "POLL") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pollOptions,
                        onValueChange = { pollOptions = it },
                        label = { Text("Poll Options (semicolon separated)") },
                        placeholder = { Text("Option A; Option B; Option C") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && body.isNotBlank()) {
                                onCreate(postType, title, body, tags, pollOptions)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Post", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
