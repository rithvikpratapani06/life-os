package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.HabitEntity
import com.example.data.repository.DayHabitSummary
import com.example.ui.LifeOsViewModel
import com.example.ui.components.AppGuideSheet
import com.example.ui.components.HabitEditorSheet
import com.example.ui.components.ManageCategoriesDialog
import com.example.ui.components.PinLockScreen
import com.example.ui.components.QuickLogDialog
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LifeOsTheme
import com.example.util.DateHelper
import kotlinx.coroutines.launch

enum class LifeOsTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    HABITS("Habits", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    FINANCE("Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    PLANNER("Planner", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    INSIGHTS("Insights", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LifeOsViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LifeOsTheme(
                themeMode = uiState.themeMode,
                accentPalette = uiState.accentPalette
            ) {
                LaunchedEffect(uiState.screenSecurityEnabled) {
                    if (uiState.screenSecurityEnabled) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                if (uiState.isPinEnabled && !uiState.isUnlocked) {
                    PinLockScreen(onVerifyPin = { pin -> viewModel.verifyPin(pin) })
                } else {
                    LifeOsApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LifeOsApp(viewModel: LifeOsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by rememberSaveable { mutableStateOf(LifeOsTab.HOME) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var showHabitEditor by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var showAppGuide by remember { mutableStateOf(false) }
    var quickLogTarget by remember { mutableStateOf<Pair<DayHabitSummary, String>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbarHostState.showSnackbar(
                if (granted) "Notifications enabled" else "Notifications permission denied"
            )
        }
    }

    LaunchedEffect(uiState.stressReminderEnabled, uiState.stressReminderHour, uiState.stressReminderMinute) {
        com.example.notifications.ReminderScheduler.syncStressReminder(
            context,
            uiState.stressReminderEnabled,
            uiState.stressReminderHour,
            uiState.stressReminderMinute
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showSettings) {
        BackHandler { showSettings = false }
    } else if (currentTab != LifeOsTab.HOME) {
        BackHandler { currentTab = LifeOsTab.HOME }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar {
                        LifeOsTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail {
                        LifeOsTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }

                val contentModifier = Modifier
                    .weight(1f)
                    .widthIn(max = 720.dp)
                    .align(Alignment.Top)

                if (showSettings) {
                    SettingsScreen(
                        uiState = uiState,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetAccentPalette = { viewModel.setAccentPalette(it) },
                        onSetUserName = { viewModel.setUserName(it) },
                        onSetCompactMode = { viewModel.setCompactMode(it) },
                        onSetDayCutoffHour = { viewModel.setDayCutoffHour(it) },
                        onExportJson = { callback -> viewModel.exportData(callback) },
                        onClearAllData = { viewModel.clearAllData() },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Notifications are enabled on this Android version")
                                }
                            }
                        },
                        onOpenGuide = { showAppGuide = true },
                        onSetAppPin = { pin -> viewModel.setAppPin(pin) },
                        onDisableAppPin = { viewModel.disableAppPin() },
                        onSetScreenSecurity = { enabled -> viewModel.setScreenSecurityEnabled(enabled) },
                        onSetStressReminder = { enabled, h, m ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.setStressReminder(enabled, h, m)
                        },
                        onBack = { showSettings = false },
                        modifier = contentModifier
                    )
                } else {
                    when (currentTab) {
                        LifeOsTab.HOME -> HomeScreen(
                            uiState = uiState,
                            onQuickToggleBoolean = { summary ->
                                viewModel.logHabit(summary.habit, uiState.logicalToday, 1.0)
                            },
                            onQuickNoBoolean = { summary ->
                                viewModel.logHabit(summary.habit, uiState.logicalToday, 0.0, isExplicitNo = true)
                            },
                            onLogTwiceDailySlot = { summary, slot, isDone, isSkip ->
                                viewModel.logHabit(
                                    habit = summary.habit,
                                    date = uiState.logicalToday,
                                    value = if (isDone) 1.0 else 0.0,
                                    isSkip = isSkip,
                                    isExplicitNo = !isDone && !isSkip,
                                    slot = slot
                                )
                            },
                            onOpenQuickLog = { summary ->
                                quickLogTarget = summary to uiState.logicalToday
                            },
                            onOpenCreateHabit = {
                                editingHabit = null
                                showHabitEditor = true
                            },
                            onOpenGuide = { showAppGuide = true },
                            onOpenSettings = { showSettings = true },
                            onLogStress = { level -> viewModel.logStress(level, "", "") },
                            modifier = contentModifier
                        )
                        LifeOsTab.HABITS -> HabitsScreen(
                            uiState = uiState,
                            onCreateHabit = {
                                editingHabit = null
                                showHabitEditor = true
                            },
                            onEditHabit = { habit ->
                                editingHabit = habit
                                showHabitEditor = true
                            },
                            onChangeStatus = { habit, status ->
                                viewModel.setHabitStatus(habit, status)
                            },
                            onDeleteHabit = { id ->
                                viewModel.deleteHabit(id)
                            },
                            onManageCategories = { showCategoryManager = true },
                            modifier = contentModifier
                        )
                        LifeOsTab.FINANCE -> FinanceScreen(
                            uiState = uiState,
                            onAddExpense = { date, amount, cat, title, method, note ->
                                viewModel.addExpense(date, amount, cat, title, method, note)
                            },
                            onDeleteExpense = { id -> viewModel.deleteExpense(id) },
                            modifier = contentModifier
                        )
                        LifeOsTab.PLANNER -> PlannerScreen(
                            uiState = uiState,
                            onSelectDate = { date -> viewModel.selectPlannerDate(date) },
                            onQuickToggleBoolean = { summary ->
                                viewModel.logHabit(summary.habit, uiState.selectedPlannerDate, 1.0)
                            },
                            onQuickNoBoolean = { summary ->
                                viewModel.logHabit(summary.habit, uiState.selectedPlannerDate, 0.0, isExplicitNo = true)
                            },
                            onLogTwiceDailySlot = { summary, slot, isDone, isSkip ->
                                viewModel.logHabit(
                                    habit = summary.habit,
                                    date = uiState.selectedPlannerDate,
                                    value = if (isDone) 1.0 else 0.0,
                                    isSkip = isSkip,
                                    isExplicitNo = !isDone && !isSkip,
                                    slot = slot
                                )
                            },
                            onOpenQuickLog = { summary ->
                                quickLogTarget = summary to uiState.selectedPlannerDate
                            },
                            modifier = contentModifier
                        )
                        LifeOsTab.INSIGHTS -> InsightsScreen(
                            uiState = uiState,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }

    if (showHabitEditor) {
        HabitEditorSheet(
            existingHabit = editingHabit,
            categories = uiState.categories,
            logicalToday = uiState.logicalToday,
            onDismiss = { showHabitEditor = false },
            onSave = { habit ->
                if (habit.reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.saveHabit(habit)
            }
        )
    }

    if (showCategoryManager) {
        ManageCategoriesDialog(
            categories = uiState.categories,
            onDismiss = { showCategoryManager = false },
            onAddCategory = { name, color, icon -> viewModel.addCategory(name, color, icon) },
            onDeleteCategory = { cat -> viewModel.deleteCategory(cat) }
        )
    }

    if (showAppGuide) {
        AppGuideSheet(onDismiss = { showAppGuide = false })
    }

    quickLogTarget?.let { (targetSummary, dateStr) ->
        val liveSummary = (if (dateStr == uiState.logicalToday) uiState.todaySummaries else uiState.plannerSummaries)
            .firstOrNull { it.habit.id == targetSummary.habit.id } ?: targetSummary
        QuickLogDialog(
            summary = liveSummary,
            dateLabel = DateHelper.formatReadable(dateStr),
            onDismiss = { quickLogTarget = null },
            onLogValue = { value, note ->
                viewModel.logHabit(liveSummary.habit, dateStr, value, note = note, isSkip = false)
            },
            onExplicitNo = { note ->
                viewModel.logHabit(liveSummary.habit, dateStr, 0.0, note = note, isExplicitNo = true)
            },
            onSkip = { note ->
                viewModel.logHabit(liveSummary.habit, dateStr, 0.0, note = note, isSkip = true)
            },
            onClearDay = {
                viewModel.clearHabitDayLogs(liveSummary.habit.id, dateStr)
            },
            onLogSlot = { slot, isDone, isSkip ->
                viewModel.logHabit(
                    habit = liveSummary.habit,
                    date = dateStr,
                    value = if (isDone) 1.0 else 0.0,
                    isSkip = isSkip,
                    isExplicitNo = !isDone && !isSkip,
                    slot = slot
                )
            }
        )
    }
}
