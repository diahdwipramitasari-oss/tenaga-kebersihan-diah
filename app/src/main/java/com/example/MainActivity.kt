package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiAnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OfficersScreen
import com.example.ui.screens.PublicFeedbackScreen
import com.example.ui.screens.QrManagerScreen
import com.example.ui.theme.SileniTheme
import com.example.ui.viewmodel.SileniViewModel

enum class SileniDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD("dashboard", R.string.nav_dashboard, Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    OFFICERS("officers", R.string.nav_officers, Icons.Rounded.Group, Icons.Outlined.Group),
    QR_MANAGER("qr_manager", R.string.nav_qr_manager, Icons.Rounded.QrCode2, Icons.Outlined.QrCode2),
    FEEDBACK("feedback", R.string.nav_feedback, Icons.Rounded.RateReview, Icons.Outlined.RateReview),
    ANALYTICS("analytics", R.string.nav_analytics, Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SileniTheme {
                SileniApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SileniApp(
    viewModel: SileniViewModel = viewModel(factory = SileniViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SileniDestination.DASHBOARD.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_full_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${state.activePersona.displayName} • ${state.activePersona.subtitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        SileniDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                modifier = Modifier.testTag("nav_bar_${dest.route}"),
                                icon = {
                                    NavIconWithAlertBadge(
                                        dest = dest,
                                        selected = selected,
                                        unresolvedCount = state.dashboardSummary.unresolvedAlerts,
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Spacer(Modifier.height(12.dp))
                        SileniDestination.entries.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                modifier = Modifier.testTag("nav_rail_${dest.route}"),
                                icon = {
                                    NavIconWithAlertBadge(
                                        dest = dest,
                                        selected = selected,
                                        unresolvedCount = state.dashboardSummary.unresolvedAlerts,
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SileniDestination.DASHBOARD.route,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 900.dp),
                    ) {
                        composable(SileniDestination.DASHBOARD.route) {
                            DashboardScreen(
                                state = state,
                                onSelectPersona = viewModel::selectPersona,
                                onSetFeedbackFilter = viewModel::setFeedbackFilter,
                                onResolveFeedback = viewModel::markFeedbackResolved,
                                onNavigateToFeedback = { navigateTo(SileniDestination.FEEDBACK.route) },
                                onNavigateToQr = { navigateTo(SileniDestination.QR_MANAGER.route) },
                            )
                        }
                        composable(SileniDestination.OFFICERS.route) {
                            OfficersScreen(
                                state = state,
                                onToggleTask = viewModel::toggleTaskCompleted,
                                onResolveFeedback = viewModel::markFeedbackResolved,
                                onReassignRoomZone = viewModel::reassignRoomZone,
                            )
                        }
                        composable(SileniDestination.QR_MANAGER.route) {
                            QrManagerScreen(
                                state = state,
                                onAddRoom = viewModel::addRoom,
                                onSimulateScanRoom = { roomId ->
                                    viewModel.selectRoomForFeedback(roomId)
                                    navigateTo(SileniDestination.FEEDBACK.route)
                                },
                            )
                        }
                        composable(SileniDestination.FEEDBACK.route) {
                            PublicFeedbackScreen(
                                state = state,
                                onSelectRoom = viewModel::selectRoomForFeedback,
                                onSubmitFeedback = viewModel::submitFeedback,
                                onBackToDashboard = { navigateTo(SileniDestination.DASHBOARD.route) },
                            )
                        }
                        composable(SileniDestination.ANALYTICS.route) {
                            AiAnalyticsScreen(
                                state = state,
                                onRefreshInsight = viewModel::regenerateAiInsight,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIconWithAlertBadge(
    dest: SileniDestination,
    selected: Boolean,
    unresolvedCount: Int,
) {
    val icon = if (selected) dest.selectedIcon else dest.unselectedIcon
    if (dest == SileniDestination.OFFICERS && unresolvedCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text("$unresolvedCount")
                }
            },
        ) {
            Icon(icon, contentDescription = stringResource(dest.labelRes))
        }
    } else {
        Icon(icon, contentDescription = stringResource(dest.labelRes))
    }
}
