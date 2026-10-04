package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun AdminModerationScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val resources by viewModel.allResources.collectAsState()
    val posts by viewModel.communityPosts.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Admin & Moderation Hub",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
                Text(
                    text = "Role-Based Access Control (RBAC), verification & audit log management",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // System Health Metrics
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Platform Security & Telemetry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "Room SQLite Cache", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = "HEALTHY • SYNCHRONIZED", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF10B981)))
                    }
                    Column {
                        Text(text = "AI Logic Engine", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = "ACTIVE • < 850ms", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "File Upload Validation", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = "MIME VERIFIED", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8)))
                    }
                    Column {
                        Text(text = "Audit Logging", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = "STRICT ENFORCED", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = statusMessage ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Uploaded Resources Moderation Queue
        item {
            PharmaSectionHeader(
                title = "Resource Moderation Queue",
                subtitle = "Approve or remove shared study materials"
            )
        }

        items(resources.take(3)) { res ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = res.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                        Text(text = "By ${res.author} • ${res.subject} • ${res.fileSize}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Row {
                        IconButton(onClick = {
                            viewModel.deleteResource(res.id)
                            statusMessage = "Resource '${res.title.take(20)}...' removed from repository."
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Community Discussions Moderation Queue
        item {
            PharmaSectionHeader(
                title = "Community Content Audit",
                subtitle = "Active student discussions and academic posts"
            )
        }

        items(posts) { post ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = post.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                        Text(text = "By ${post.authorName} • ${post.postType}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    IconButton(onClick = {
                        viewModel.deletePost(post.id)
                        statusMessage = "Post '${post.title.take(20)}...' removed by admin."
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}
