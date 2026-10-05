package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.PharmaHubTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PharmaHubViewModel

data class NavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    viewModel: PharmaHubViewModel
) {
    val themeConfig by viewModel.themeConfig.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // 4 Core Destinations: Home, Notes, AI, Account
    val navItems = listOf(
        NavItem(AppScreen.HOME, "Home", Icons.Default.Home),
        NavItem(AppScreen.RESOURCES, "Notes", Icons.AutoMirrored.Filled.MenuBook),
        NavItem(AppScreen.AI_ASSISTANT, "AI", Icons.Default.AutoAwesome),
        NavItem(AppScreen.AUTH, "Account", Icons.Default.AccountCircle)
    )

    // Back navigation handling
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        viewModel.popBack()
    }

    PharmaHubTheme(themeConfig = themeConfig) {
        Scaffold(
            bottomBar = {
                // Bottom Navigation is visible for the main 4 tabs; hidden during full-screen document viewing
                if (currentScreen != AppScreen.DOCUMENT_VIEWER) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        navItems.forEach { item ->
                            val isSelected = when (item.screen) {
                                AppScreen.AUTH -> currentScreen == AppScreen.AUTH || currentScreen == AppScreen.PROFILE
                                else -> currentScreen == item.screen
                            }

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentScreen != item.screen) {
                                        viewModel.navigateTo(item.screen)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openDocument(it) },
                        onOpenNotes = { viewModel.navigateTo(AppScreen.RESOURCES) },
                        onOpenAiWithQuery = { query ->
                            viewModel.navigateTo(AppScreen.AI_ASSISTANT)
                            viewModel.searchAndResolveDrug(query)
                        }
                    )
                    AppScreen.RESOURCES -> ResourceLibraryScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openDocument(it) }
                    )
                    AppScreen.AI_ASSISTANT -> AiAssistantScreen(
                        viewModel = viewModel
                    )
                    AppScreen.AUTH, AppScreen.PROFILE -> AuthScreen(
                        viewModel = viewModel,
                        onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN) },
                        onOpenNote = { viewModel.openDocument(it) }
                    )
                    AppScreen.DOCUMENT_VIEWER -> DocumentViewerScreen(
                        viewModel = viewModel
                    )
                    AppScreen.ADMIN -> AdminModerationScreen(
                        viewModel = viewModel
                    )
                    else -> HomeScreen(
                        viewModel = viewModel,
                        onOpenNote = { viewModel.openDocument(it) },
                        onOpenNotes = { viewModel.navigateTo(AppScreen.RESOURCES) },
                        onOpenAiWithQuery = { query ->
                            viewModel.navigateTo(AppScreen.AI_ASSISTANT)
                            viewModel.searchAndResolveDrug(query)
                        }
                    )
                }
            }
        }
    }
}
