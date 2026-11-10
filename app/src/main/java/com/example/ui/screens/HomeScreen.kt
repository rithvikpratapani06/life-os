package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.util.Calendar
import com.example.data.model.LogStatus
import com.example.data.model.StressLevel
import com.example.data.model.TrackingType
import com.example.data.repository.DayHabitSummary
import com.example.ui.LifeOsUiState
import com.example.ui.components.DailyProgressRing
import com.example.ui.components.DayHabitCard
import com.example.ui.theme.MonoMetricMedium
import com.example.util.DateHelper
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    uiState: LifeOsUiState,
    onQuickToggleBoolean: (DayHabitSummary) -> Unit,
    onQuickNoBoolean: (DayHabitSummary) -> Unit,
    onLogTwiceDailySlot: (DayHabitSummary, String, Boolean, Boolean) -> Unit,
    onOpenQuickLog: (DayHabitSummary) -> Unit,
    onOpenCreateHabit: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogStress: (StressLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayItems = uiState.todaySummaries
    val completedCount = todayItems.count { it.status == LogStatus.COMPLETED }
    val totalCount = todayItems.size
    val progress = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    val bestStreak = todayItems.maxOfOrNull { it.currentStreak } ?: 0
    val haptic = LocalHapticFeedback.current
    val itemSpacing = if (uiState.compactMode) 10.dp else 16.dp

    val greeting = androidx.compose.runtime.remember(uiState.userName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val base = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        if (uiState.userName.isNotBlank()) "$base, ${uiState.userName}" else base
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateHelper.formatReadable(uiState.logicalToday),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenGuide,
                        modifier = Modifier.testTag("open_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "App Guide & Intro",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onOpenCreateHabit,
                        modifier = Modifier.testTag("home_add_habit_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Habit")
                    }
                }
            }
        }

        // Hero Overview Card
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        DailyProgressRing(
                            progress = progress,
                            completed = completedCount,
                            total = totalCount
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = when {
                                    totalCount == 0 -> "No habits scheduled today"
                                    completedCount == totalCount -> "All daily habits complete!"
                                    else -> "${totalCount - completedCount} remaining today"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniMetricChip(
                                    label = "Best Streak",
                                    value = "${bestStreak}d"
                                )
                                MiniMetricChip(
                                    label = "7d Rate",
                                    value = "${(uiState.weeklyInsights.completionRate * 100).roundToInt()}%"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Stress Check-In Card
        item {
            val todayStress = uiState.stressAnalytics.todayStress
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (todayStress == null) "How is your stress level today?" else "Today's Stress: ${todayStress.level.label}",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StressLevel.entries.forEach { level ->
                            FilterChip(
                                selected = todayStress?.level == level,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onLogStress(level)
                                },
                                label = { Text(level.label) }
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Reminders Strip
        if (uiState.upcomingReminders.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.upcomingReminders.forEach { habit ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val timeLabel = if (habit.secondReminderEnabled) {
                                        String.format(
                                            Locale.US,
                                            "%02d:%02d & %02d:%02d",
                                            habit.reminderHour,
                                            habit.reminderMinute,
                                            habit.secondReminderHour,
                                            habit.secondReminderMinute
                                        )
                                    } else {
                                        String.format(Locale.US, "%02d:%02d", habit.reminderHour, habit.reminderMinute)
                                    }
                                    Text(
                                        text = timeLabel,
                                        style = MonoMetricMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Habits Section
        item {
            Text(
                text = "Scheduled Today",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (todayItems.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Build your daily rhythm",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Create your first habit to start tracking streaks, partial progress, and weekly insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onOpenCreateHabit) {
                            Text("Create First Habit")
                        }
                    }
                }
            }
        } else {
            items(todayItems, key = { it.habit.id }) { summary ->
                DayHabitCard(
                    summary = summary,
                    onQuickAction = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (summary.habit.trackingType == TrackingType.BOOLEAN) {
                            if (summary.targetValue > 1.0 && summary.status == LogStatus.COMPLETED) {
                                onOpenQuickLog(summary)
                            } else {
                                onQuickToggleBoolean(summary)
                            }
                        } else {
                            onOpenQuickLog(summary)
                        }
                    },
                    onQuickNo = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQuickNoBoolean(summary)
                    },
                    onLogSlot = { slot, isDone, isSkip ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLogTwiceDailySlot(summary, slot, isDone, isSkip)
                    },
                    onOpenDetails = { onOpenQuickLog(summary) }
                )
            }
        }

        // Compact Weekly Summary Card
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("7-Day Consistency", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${uiState.weeklyInsights.completedCount} completed • ${uiState.weeklyInsights.missedCount} missed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${(uiState.weeklyInsights.completionRate * 100).roundToInt()}%",
                        style = MonoMetricMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MonoMetricMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
