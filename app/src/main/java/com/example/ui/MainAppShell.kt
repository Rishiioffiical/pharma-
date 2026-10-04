package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.screens.*
import com.example.ui.theme.PharmaHubTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel
import kotlinx.coroutines.launch

data class DrawerItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    viewModel: PharmaHubViewModel
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val resources by viewModel.allResources.collectAsState()
    val drugs by viewModel.allDrugs.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var isSearchPaletteOpen by remember { mutableStateOf(false) }

    // Intercept back presses using BackHandler
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.popBack()
        }
    }

    val primaryNavItems = listOf(
        Pair(AppScreen.HOME, Icons.Default.Home to "Home"),
        Pair(AppScreen.RESOURCES, Icons.Default.LibraryBooks to "Resources"),
        Pair(AppScreen.FLASHCARDS, Icons.Default.Style to "Cards"),
        Pair(AppScreen.QUIZ, Icons.Default.Quiz to "Quiz"),
        Pair(AppScreen.COMMUNITY, Icons.Default.Forum to "Community")
    )

    val allDrawerItems = listOf(
        DrawerItem(AppScreen.HOME, "Home Dashboard", Icons.Default.Home),
        DrawerItem(AppScreen.RESOURCES, "Academic Library", Icons.Default.LibraryBooks),
        DrawerItem(AppScreen.SUBJECTS, "Subject Explorer", Icons.Default.MenuBook),
        DrawerItem(AppScreen.EXPLORE, "Semester Catalog", Icons.Default.Layers),
        DrawerItem(AppScreen.FLASHCARDS, "Flashcards (SM-2)", Icons.Default.Style),
        DrawerItem(AppScreen.QUIZ, "Quiz & GPAT Mock", Icons.Default.Quiz),
        DrawerItem(AppScreen.COMMUNITY, "Academic Community", Icons.Default.Forum),
        DrawerItem(AppScreen.CHAT, "Study Groups Chat", Icons.Default.Chat),
        DrawerItem(AppScreen.DRUG_LIBRARY, "Drug Monographs", Icons.Default.Medication),
        DrawerItem(AppScreen.AI_ASSISTANT, "AI Study Tutor", Icons.Default.AutoAwesome, "AI"),
        DrawerItem(AppScreen.ANALYTICS, "Study Analytics", Icons.Default.Insights),
        DrawerItem(AppScreen.ADMIN, "Admin & Moderation", Icons.Default.AdminPanelSettings),
        DrawerItem(AppScreen.PROFILE, "Profile & Theme Engine", Icons.Default.Settings)
    )

    PharmaHubTheme(themeConfig = themeConfig) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(310.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Drawer Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalPharmacy,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PHARMAHUB",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Academic Workspace",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Drawer Navigation List
                        allDrawerItems.forEach { item ->
                            val isSelected = currentScreen == item.screen
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        )
                                        if (item.badge != null) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = item.badge,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                selected = isSelected,
                                onClick = {
                                    viewModel.navigateTo(item.screen)
                                    coroutineScope.launch { drawerState.close() }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .testTag("drawer_item_${item.screen.name}")
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "PHARMAHUB v2.4 • PCI Accredited",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = currentScreen.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchPaletteOpen = true }) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Global Search")
                            }
                            IconButton(onClick = { viewModel.navigateTo(AppScreen.PROFILE) }) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings & Theme")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        primaryNavItems.forEach { (screen, iconAndLabel) ->
                            val (icon, label) = iconAndLabel
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(screen) },
                                modifier = Modifier.testTag("bottom_nav_${screen.name}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onOpenSearch = { isSearchPaletteOpen = true }
                        )
                        AppScreen.RESOURCES -> ResourceLibraryScreen(viewModel = viewModel)
                        AppScreen.FLASHCARDS -> FlashcardsScreen(viewModel = viewModel)
                        AppScreen.QUIZ -> QuizScreen(viewModel = viewModel)
                        AppScreen.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                        AppScreen.CHAT -> ChatScreen(viewModel = viewModel)
                        AppScreen.SUBJECTS -> SubjectsScreen(viewModel = viewModel)
                        AppScreen.EXPLORE -> ExploreSemesterScreen(viewModel = viewModel)
                        AppScreen.DRUG_LIBRARY -> DrugKnowledgeScreen(viewModel = viewModel)
                        AppScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                        AppScreen.ANALYTICS -> ProgressAnalyticsScreen(viewModel = viewModel)
                        AppScreen.ADMIN -> AdminModerationScreen(viewModel = viewModel)
                        AppScreen.PROFILE -> ProfileSettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // Global Command Palette Dialog
        CommandPaletteDialog(
            isOpen = isSearchPaletteOpen,
            onDismiss = { isSearchPaletteOpen = false },
            resources = resources,
            drugs = drugs,
            onSelectResource = { res ->
                viewModel.navigateTo(AppScreen.RESOURCES)
            },
            onSelectDrug = { drug ->
                viewModel.navigateTo(AppScreen.DRUG_LIBRARY)
            }
        )
    }
}
