package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveWorkoutDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogWorkoutScreen
import com.example.ui.screens.WearableSyncScreen
import com.example.ui.screens.WeeklyTrendsScreen
import com.example.ui.screens.WorkoutsHistoryScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD(
        title = "Monitor",
        selectedIcon = Icons.Filled.MonitorHeart,
        unselectedIcon = Icons.Outlined.MonitorHeart,
        testTag = "tab_dashboard"
    ),
    LOG_WORKOUT(
        title = "Log",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        testTag = "tab_log_workout"
    ),
    TRENDS(
        title = "Trends",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "tab_trends"
    ),
    WORKOUTS(
        title = "History",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        testTag = "tab_workouts"
    ),
    WEARABLE(
        title = "Wearable",
        selectedIcon = Icons.Filled.Watch,
        unselectedIcon = Icons.Outlined.Watch,
        testTag = "tab_wearable"
    )
}

@Composable
fun PulseSyncApp(
    viewModel: FitnessViewModel
) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel events (e.g. "Logged 368 kcal", "Connected to PulseWatch")
    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle system back navigation if not on Home/Dashboard
    if (currentTab != AppTab.DASHBOARD) {
        BackHandler {
            currentTab = AppTab.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar"),
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) PulseCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PulseCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PulseCyan,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = PulseCyan,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_navigation"
            ) { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToLog = { currentTab = AppTab.LOG_WORKOUT },
                        onNavigateToWearable = { currentTab = AppTab.WEARABLE },
                        onStartLiveWorkout = { exercise ->
                            viewModel.startLiveWorkout(exercise)
                        },
                        onNavigateToTrends = { currentTab = AppTab.TRENDS }
                    )
                    AppTab.LOG_WORKOUT -> LogWorkoutScreen(
                        viewModel = viewModel,
                        onWorkoutSaved = { currentTab = AppTab.TRENDS }
                    )
                    AppTab.TRENDS -> WeeklyTrendsScreen(
                        viewModel = viewModel,
                        onNavigateToLog = { currentTab = AppTab.LOG_WORKOUT }
                    )
                    AppTab.WORKOUTS -> WorkoutsHistoryScreen(
                        viewModel = viewModel,
                        onNavigateToLog = { currentTab = AppTab.LOG_WORKOUT },
                        onNavigateToTrends = { currentTab = AppTab.TRENDS }
                    )
                    AppTab.WEARABLE -> WearableSyncScreen(
                        viewModel = viewModel
                    )
                }
            }

            // Live Workout Modal overlay if an active session is started
            LiveWorkoutDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.cancelLiveWorkout() }
            )
        }
    }
}
