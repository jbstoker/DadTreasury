package com.stokstylez.dadtreasury.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stokstylez.dadtreasury.data.DadTreasuryRepository
import com.stokstylez.dadtreasury.data.SettingsRepository
import com.stokstylez.dadtreasury.security.PinLockManager
import com.stokstylez.dadtreasury.ui.screens.*
import com.stokstylez.dadtreasury.ui.theme.LocalSemanticTokens

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TASKS = "tasks"
    const val TASK_DETAIL = "task/{taskId}"
    const val WALLET = "wallet"
    const val CALENDAR = "calendar"
    const val CHAT = "chat"
    const val LIBRARY = "library"
    const val LOCATION = "location"
    const val PAIRING = "pairing"
    const val DIAGNOSTICS = "diagnostics"
    const val CONNECT_PARENTS = "connect_parents"
    const val SETTINGS = "settings"
    const val PIN_SETUP = "pin_setup"

    fun taskDetail(taskId: String) = "task/$taskId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DadTreasuryApp(
    repository: DadTreasuryRepository,
    settingsRepository: SettingsRepository,
    role: String?,
    onboardingDone: Boolean,
    pinLockManager: PinLockManager? = null,
) {
    val navController = rememberNavController()
    val tokens = LocalSemanticTokens.current

    val startDestination = when {
        !onboardingDone -> Routes.ONBOARDING
        else -> Routes.HOME
    }

    Scaffold(
        containerColor = tokens.background,
        bottomBar = {
            if (onboardingDone) {
                val items = listOf(
                    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home),
                    BottomNavItem(Routes.TASKS, "Tasks", Icons.Filled.Checklist),
                    BottomNavItem(Routes.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet),
                    BottomNavItem(Routes.CALENDAR, "Calendar", Icons.Filled.DateRange),
                    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBar(
                    containerColor = tokens.surface,
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (item.route == Routes.HOME) {
                                    // Pop back to the dashboard instead of navigating,
                                    // which would restore a stale backstack (Wallet on top).
                                    navController.popBackStack(Routes.HOME, inclusive = false)
                                } else {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        settingsRepository = settingsRepository,
                        onDone = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        repository = repository,
                        role = role,
                        onNavigate = { route -> navController.navigate(route) },
                    )
                }
                composable(Routes.TASKS) {
                    TasksScreen(
                        repository = repository,
                        role = role,
                        onTaskClick = { taskId -> navController.navigate(Routes.taskDetail(taskId)) },
                    )
                }
                composable(Routes.TASK_DETAIL) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                    TaskDetailScreen(
                        repository = repository,
                        taskId = taskId,
                        role = role,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.WALLET) {
                    WalletScreen(repository = repository)
                }
                composable(Routes.CALENDAR) {
                    CalendarScreen(repository = repository)
                }
                composable(Routes.CHAT) {
                    ChatScreen(repository = repository, role = role)
                }
                composable(Routes.LIBRARY) {
                    LibraryScreen(repository = repository)
                }
                composable(Routes.LOCATION) {
                    LocationScreen(repository = repository)
                }
                composable(Routes.PAIRING) {
                    PairingScreen(repository = repository, role = role)
                }
                composable(Routes.DIAGNOSTICS) {
                    DiagnosticsScreen(repository = repository)
                }
                composable(Routes.CONNECT_PARENTS) {
                    ConnectParentsScreen(repository = repository)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        pinLockManager = pinLockManager,
                        onNavigatePinSetup = { navController.navigate(Routes.PIN_SETUP) },
                    )
                }
                composable(Routes.PIN_SETUP) {
                    PinSetupScreen(
                        pinLockManager = pinLockManager ?: PinLockManager(LocalContext.current.applicationContext),
                        mode = if (pinLockManager?.isPinEnabled == true) PinSetupMode.CHANGE else PinSetupMode.SETUP,
                        onDone = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}