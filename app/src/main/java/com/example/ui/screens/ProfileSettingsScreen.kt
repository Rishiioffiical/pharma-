package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.model.PharmacyCourse
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PharmaSectionHeader
import com.example.ui.components.PillChip
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.PharmaHubViewModel

@Composable
fun ProfileSettingsScreen(
    viewModel: PharmaHubViewModel,
    modifier: Modifier = Modifier
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val course by viewModel.selectedCourse.collectAsState()
    val semester by viewModel.selectedSemester.collectAsState()

    var userName by remember { mutableStateOf("Rishi Pandit") }
    var university by remember { mutableStateOf("National College of Pharmacy") }
    var bio by remember { mutableStateOf("Pharm.D candidate passionate about Clinical Pharmacokinetics, Therapeutic Drug Monitoring (TDM), and GPAT prep.") }

    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isPublicProfile by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
    ) {
        item {
            Text(
                text = "Academic Profile & Settings",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Manage your pharmacy credentials, theme engine, and privacy",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Avatar Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RP",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${course.name.replace("_", ".")} • Sem $semester",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = university,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Dynamic Theme Engine Section
        item {
            PharmaSectionHeader(
                title = "Dynamic Theme Engine",
                subtitle = "Choose from 12 custom scientific color systems"
            )
        }

        // Dark / Light Mode Switch
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (themeConfig.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (themeConfig.isDark) "Dark Futuristic Mode" else "Clean Light Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Switch(
                        checked = themeConfig.isDark,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    )
                }
            }
        }

        // 12 Presets Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                val presets = ThemePreset.values()
                // Render in rows of 2
                for (i in presets.indices step 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val p1 = presets[i]
                        val isP1Selected = themeConfig.preset == p1
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setThemePreset(p1) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isP1Selected) 2.dp else 1.dp,
                                if (isP1Selected) p1.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(p1.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = p1.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isP1Selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isP1Selected) p1.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        if (i + 1 < presets.size) {
                            val p2 = presets[i + 1]
                            val isP2Selected = themeConfig.preset == p2
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setThemePreset(p2) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isP2Selected) 2.dp else 1.dp,
                                    if (isP2Selected) p2.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(p2.primary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p2.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isP2Selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isP2Selected) p2.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Privacy & Security Controls
        item {
            PharmaSectionHeader(
                title = "Privacy & Notifications",
                subtitle = "Fine-grained control over academic data visibility"
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Public Academic Profile", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Allow peers in study groups to view your shared notes and streak", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
                        }
                        Switch(checked = isPublicProfile, onCheckedChange = { isPublicProfile = it })
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Study Reminders & Quizzes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Receive notifications when spaced-repetition flashcards are due", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp))
                        }
                        Switch(checked = isNotificationEnabled, onCheckedChange = { isNotificationEnabled = it })
                    }
                }
            }
        }
    }
}
