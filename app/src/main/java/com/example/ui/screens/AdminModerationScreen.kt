package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.*
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PillChip
import com.example.ui.components.ResourceUploadDialog
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModerationScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allResources by viewModel.allResources.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    // Access Control Guard
    val hasAdminAccess = currentUser?.role?.canAccessAdmin() == true

    if (!hasAdminAccess) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Administrator Access Required",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "This terminal contains system-level governance tools, content moderation workflows, user management, and security audit logs. You are currently browsing as ${currentUser?.role?.displayName ?: "Guest"}.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                            Text("Return Home")
                        }

                        Button(
                            onClick = {
                                viewModel.navigateTo(AppScreen.AUTH)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("admin_login_auth_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sign In")
                        }
                    }
                }
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resources", "Users & Roles", "Reports", "Curriculum", "Audit Logs")

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var resourceFilter by remember { mutableStateOf("Pending") } // Pending, Approved, Rejected, All
    var isDirectUploadOpen by remember { mutableStateOf(false) }

    // Dialog States
    var editingResource by remember { mutableStateOf<ResourceEntity?>(null) }
    var rejectingResource by remember { mutableStateOf<ResourceEntity?>(null) }
    var rejectionReason by remember { mutableStateOf("Copyright or Licensing Infringement") }
    var rejectionNotes by remember { mutableStateOf("") }
    var deletingResourceConfirm by remember { mutableStateOf<ResourceEntity?>(null) }
    var changingUserRole by remember { mutableStateOf<UserEntity?>(null) }
    var selectedNewRole by remember { mutableStateOf(UserRole.STUDENT) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { isDirectUploadOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 76.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = "Admin Upload")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish Resource", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Admin Identity Header Card
            item {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Administrator Control Center",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${currentUser?.displayName} (${currentUser?.role?.displayName})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        // Switch persona for demonstration
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppScreen.AUTH) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Switch Account", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val pendingCount = allResources.count { it.status == ResourceStatus.PENDING_REVIEW }
                        val approvedCount = allResources.count { it.status == ResourceStatus.APPROVED }
                        val openReportsCount = allReports.count { it.status == ReportStatus.OPEN }

                        AdminStatSnippet("Pending Review", "$pendingCount", Color(0xFFF59E0B))
                        AdminStatSnippet("Approved Library", "$approvedCount", Color(0xFF10B981))
                        AdminStatSnippet("Open Reports", "$openReportsCount", Color(0xFFEF4444))
                        AdminStatSnippet("Registered Users", "${allUsers.size}", Color(0xFF38BDF8))
                    }
                }
            }

            // Feedback Banner
            if (statusMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = statusMessage ?: "", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }

            // Navigation Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index; statusMessage = null },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            // TAB 0: RESOURCES MANAGEMENT
            if (selectedTab == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Pending", "Approved", "Rejected", "All").forEach { filter ->
                            PillChip(
                                text = filter,
                                selected = resourceFilter == filter,
                                onClick = { resourceFilter = filter }
                            )
                        }
                    }
                }

                val filtered = allResources.filter { res ->
                    when (resourceFilter) {
                        "Pending" -> res.status == ResourceStatus.PENDING_REVIEW
                        "Approved" -> res.status == ResourceStatus.APPROVED
                        "Rejected" -> res.status == ResourceStatus.REJECTED
                        else -> true
                    }
                }

                if (filtered.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No resources matching '$resourceFilter' filter.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                } else {
                    items(filtered) { res ->
                        AdminResourceCard(
                            resource = res,
                            onApprove = {
                                viewModel.approveResource(res.id, "Approved by platform administrator")
                                statusMessage = "Approved '${res.title.take(24)}...' and published to student library."
                            },
                            onReject = {
                                rejectingResource = res
                            },
                            onEdit = {
                                editingResource = res
                            },
                            onTogglePublish = {
                                viewModel.togglePublishStatus(res)
                                statusMessage = "Toggled status for '${res.title.take(24)}...'"
                            },
                            onDelete = {
                                deletingResourceConfirm = res
                            }
                        )
                    }
                }
            }

            // TAB 1: USERS & RBAC MANAGEMENT
            if (selectedTab == 1) {
                item {
                    Text(
                        text = "User Directory & Access Control (${allUsers.size} Users)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(allUsers) { user ->
                    AdminUserCard(
                        user = user,
                        onChangeRole = {
                            changingUserRole = user
                            selectedNewRole = user.role
                        },
                        onToggleStatus = {
                            val newStatus = if (user.status == UserAccountStatus.ACTIVE) UserAccountStatus.SUSPENDED else UserAccountStatus.ACTIVE
                            viewModel.updateUserStatus(user.id, newStatus)
                            statusMessage = "Status for ${user.displayName} updated to ${newStatus.displayName}"
                        },
                        onDeleteUser = {
                            viewModel.deleteUser(user.id)
                            statusMessage = "User ${user.displayName} deleted."
                        }
                    )
                }
            }

            // TAB 2: REPORTS MANAGEMENT
            if (selectedTab == 2) {
                item {
                    Text(
                        text = "Reported Content & Academic Integrity Inquiries",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (allReports.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Zero active content reports. Community standards are well-maintained.",
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(allReports) { report ->
                        AdminReportCard(
                            report = report,
                            onResolve = {
                                viewModel.resolveReport(report.id, ReportStatus.RESOLVED, "Resolved by administrator action")
                                statusMessage = "Report on '${report.resourceTitle}' marked as RESOLVED."
                            },
                            onDismiss = {
                                viewModel.resolveReport(report.id, ReportStatus.DISMISSED, "Dismissed as false positive")
                                statusMessage = "Report dismissed."
                            }
                        )
                    }
                }
            }

            // TAB 3: CURRICULUM MANAGEMENT
            if (selectedTab == 3) {
                item {
                    CurriculumOverviewCard()
                }
            }

            // TAB 4: AUDIT LOGS & TELEMETRY
            if (selectedTab == 4) {
                item {
                    Text(
                        text = "Immutable Security Audit Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Cryptographically signed trace of administrative and user actions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                items(auditLogs.take(50)) { log ->
                    AdminAuditLogItem(log = log)
                }
            }
        }
    }

    // Direct Admin Upload Dialog
    if (isDirectUploadOpen) {
        ResourceUploadDialog(
            isOpen = isDirectUploadOpen,
            onDismiss = { isDirectUploadOpen = false },
            onUpload = { title, subject, semester, course, fileType, fileSize, tags, desc ->
                viewModel.uploadResource(
                    title = title,
                    subject = subject,
                    semester = semester,
                    course = course,
                    fileType = fileType,
                    fileSize = fileSize,
                    tags = tags,
                    description = desc
                )
                statusMessage = "Admin published '$title' directly to library."
            }
        )
    }

    // Edit Resource Dialog
    editingResource?.let { res ->
        var editTitle by remember { mutableStateOf(res.title) }
        var editSubject by remember { mutableStateOf(res.subject) }
        var editSemester by remember { mutableIntStateOf(res.semester) }
        var editTags by remember { mutableStateOf(res.tags) }
        var editDesc by remember { mutableStateOf(res.description) }

        Dialog(onDismissRequest = { editingResource = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit Academic Resource Metadata",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Semester: Sem $editSemester", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = editSemester.toFloat(),
                        onValueChange = { editSemester = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTags,
                        onValueChange = { editTags = it },
                        label = { Text("Tags (comma separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingResource = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val updated = res.copy(
                                title = editTitle.trim(),
                                subject = editSubject.trim(),
                                semester = editSemester,
                                tags = editTags.trim(),
                                description = editDesc.trim()
                            )
                            viewModel.updateResource(updated)
                            statusMessage = "Updated metadata for '${updated.title}'."
                            editingResource = null
                        }) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Reject Dialog
    rejectingResource?.let { res ->
        Dialog(onDismissRequest = { rejectingResource = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Reject Submission",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rejecting '${res.title}' prevents it from being published. Specify a feedback reason for the uploader.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val reasons = listOf(
                        "Copyright or Licensing Infringement",
                        "Factually Incorrect / Dangerous Medical Info",
                        "Incomplete Notes / Corrupted File",
                        "Spam or Duplicate Submission"
                    )

                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rejectionReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = rejectionReason == reason, onClick = { rejectionReason = reason })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rejectionNotes,
                        onValueChange = { rejectionNotes = it },
                        label = { Text("Moderator Notes (optional)") },
                        placeholder = { Text("e.g. Please re-upload with proper chapter 4 references.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { rejectingResource = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.rejectResource(res.id, rejectionReason, rejectionNotes)
                                statusMessage = "Rejected '${res.title}'. Reason recorded in audit trail."
                                rejectingResource = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Confirm Rejection")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    deletingResourceConfirm?.let { res ->
        AlertDialog(
            onDismissRequest = { deletingResourceConfirm = null },
            title = { Text("Delete Resource Permanently?") },
            text = { Text("Are you sure you want to permanently delete '${res.title}'? This action cannot be undone and will delete all cached offline files.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteResource(res.id)
                        statusMessage = "Permanently deleted '${res.title}'."
                        deletingResourceConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingResourceConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Role Change Dialog
    changingUserRole?.let { user ->
        Dialog(onDismissRequest = { changingUserRole = null }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Modify User Role & Privileges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Assign access level for ${user.displayName} (${user.email})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    UserRole.values().forEach { role ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNewRole = role }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedNewRole == role, onClick = { selectedNewRole = role })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(text = role.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Security Level: ${role.level}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { changingUserRole = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.updateUserRole(user.id, selectedNewRole)
                            statusMessage = "Updated ${user.displayName}'s role to ${selectedNewRole.displayName}."
                            changingUserRole = null
                        }) {
                            Text("Update Role")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatSnippet(label: String, value: String, accent: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = accent))
    }
}

@Composable
fun AdminResourceCard(
    resource: ResourceEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (resource.status) {
                        ResourceStatus.APPROVED -> Color(0xFF10B981).copy(alpha = 0.15f)
                        ResourceStatus.PENDING_REVIEW -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ResourceStatus.REJECTED -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = resource.status.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (resource.status) {
                                ResourceStatus.APPROVED -> Color(0xFF10B981)
                                ResourceStatus.PENDING_REVIEW -> Color(0xFFF59E0B)
                                ResourceStatus.REJECTED -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }

                Text(
                    text = "${resource.fileType} • ${resource.fileSize}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "${resource.course} Sem ${resource.semester} • ${resource.subject} • By ${resource.author} (${resource.uploaderEmail})",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            )

            if (resource.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rejection Reason: ${resource.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (resource.status == ResourceStatus.PENDING_REVIEW) {
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onTogglePublish,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (resource.status == ResourceStatus.APPROVED) "Unpublish" else "Publish")
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserEntity,
    onChangeRole: () -> Unit,
    onToggleStatus: () -> Unit,
    onDeleteUser: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = user.role.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    text = "${user.email} • ${user.university}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Status: ${user.status.displayName} • ${user.course} Sem ${user.semester}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (user.status == UserAccountStatus.ACTIVE) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                )
            }

            Row {
                IconButton(onClick = onChangeRole) {
                    Icon(imageVector = Icons.Default.ManageAccounts, contentDescription = "Change Role", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onToggleStatus) {
                    Icon(
                        imageVector = if (user.status == UserAccountStatus.ACTIVE) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = "Toggle Status",
                        tint = if (user.status == UserAccountStatus.ACTIVE) Color(0xFFF59E0B) else Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminReportCard(
    report: ResourceReportEntity,
    onResolve: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FLAGGED: ${report.reason.displayName}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                )
                Text(
                    text = report.status.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Target Resource: ${report.resourceTitle}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text(text = "Details: ${report.details}", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Text(text = "Reporter: ${report.reporterEmail}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onResolve,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Resolve & Takedown", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dismiss Report", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun CurriculumOverviewCard() {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Accredited Pharmacy Curricula Structure",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "PCI (Pharmacy Council of India) and International Academic Standard Mapping",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val curricula = listOf(
            Triple("Bachelor of Pharmacy (B.Pharm)", "8 Semesters • 4 Years", "Pharmaceutics, Pharmacology, MedChem, Pharmacognosy"),
            Triple("Doctor of Pharmacy (Pharm.D)", "12 Semesters • 6 Years", "Clinical Pharmacy, Pharmacotherapeutics, Hospital Practice"),
            Triple("Master of Pharmacy (M.Pharm)", "4 Semesters • 2 Years", "Advanced Biopharmaceutics, Regulatory Affairs, SAR")
        )

        curricula.forEach { (name, duration, subjects) ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = duration, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
                    }
                    Text(text = subjects, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogItem(log: AuditLogEntity) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (log.result == "SUCCESS") Color(0xFF10B981) else Color(0xFFEF4444))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                Text(
                    text = "${log.actorEmail} (${log.actorRole}) • ${log.details}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}
