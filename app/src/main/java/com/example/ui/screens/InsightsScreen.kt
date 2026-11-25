package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.HabitEntity
import com.example.data.model.LogStatus
import com.example.data.model.TrackingType
import com.example.data.repository.DayHabitSummary
import com.example.data.repository.LifeOsRepository
import com.example.ui.HabitAnalytics
import com.example.ui.LifeOsUiState
import com.example.ui.components.HabitHeatmapGrid
import com.example.ui.components.InfoTipButton
import com.example.ui.components.StatusPill
import com.example.ui.components.activityKindIcon
import com.example.ui.components.formatCleanNumber
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.habitIconAndColor
import com.example.util.DateHelper
import java.util.Calendar
import kotlin.math.roundToInt

private enum class AnalyticsPeriod(val label: String) {
    WEEKLY("Weekly (7D)"),
    MONTHLY("Monthly (30D)")
}

private enum class DetailPerformanceWindow(val label: String) {
    SEVEN_D("7D"),
    THIRTY_D("30D"),
    ALL("All")
}

private enum class AnalyticsScope(val label: String) {
    OVERALL("Overall"),
    PER_HABIT("Per Habit"),
    ACTIVITIES("Activities"),
    STRESS("Stress")
}

@Composable
fun InsightsScreen(
    uiState: LifeOsUiState,
    onEditHabit: (HabitEntity) -> Unit = {},
    onOpenQuickLog: (DayHabitSummary, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(AnalyticsPeriod.WEEKLY) }
    var scope by remember { mutableStateOf(AnalyticsScope.OVERALL) }
    var selectedHabitId by remember { mutableStateOf<Long?>(null) }

    val activeInsights = if (period == AnalyticsPeriod.WEEKLY) {
        uiState.weeklyInsights
    } else {
        uiState.monthlyInsights
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Analytics & Insights", style = MaterialTheme.typography.headlineMedium)
                    InfoTipButton(
                        title = "Analytics Scopes",
                        explanation = "Overall: Combined habit completion rate. Per Habit: 12-week calendar heatmap and streak per habit. Activities: Frequency counts for occasional events like Cricket, Biryani, or Alcohol. Stress: Weekly and monthly stress trends."
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalyticsScope.entries.forEach { s ->
                        FilterChip(
                            selected = scope == s,
                            onClick = { scope = s },
                            label = { Text(s.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnalyticsPeriod.entries.forEach { p ->
                        FilterChip(
                            selected = period == p,
                            onClick = { period = p },
                            label = { Text(p.label) }
                        )
                    }
                }
            }
        }

        if (scope == AnalyticsScope.OVERALL) {
            // Top KPI Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        label = "Completion",
                        value = "${(activeInsights.completionRate * 100).roundToInt()}%",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Completed",
                        value = "${activeInsights.completedCount}",
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Missed",
                        value = "${activeInsights.missedCount}",
                        accent = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Trend Bar Chart
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (period == AnalyticsPeriod.WEEKLY) "7-Day Consistency" else "30-Day Consistency",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        val barColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            val count = activeInsights.trend.size.coerceAtLeast(1)
                            val slotWidth = size.width / count
                            val barWidth = (slotWidth * 0.58f).coerceAtMost(36.dp.toPx())
                            val maxHeight = size.height

                            activeInsights.trend.forEachIndexed { idx, point ->
                                val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(left, 0f),
                                    size = Size(barWidth, maxHeight),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                val fillHeight = (maxHeight * point.rate).coerceAtLeast(if (point.completedCount > 0) 10f else 0f)
                                if (fillHeight > 0f) {
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(left, maxHeight - fillHeight),
                                        size = Size(barWidth, fillHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                            }
                        }
                        if (period == AnalyticsPeriod.WEEKLY) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                activeInsights.trend.forEach { point ->
                                    Text(
                                        text = point.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // State Breakdown
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StateStat("Completed", activeInsights.completedCount, MaterialTheme.colorScheme.primary)
                        StateStat("Partial", activeInsights.partialCount, MaterialTheme.colorScheme.tertiary)
                        StateStat("Skipped", activeInsights.skippedCount, MaterialTheme.colorScheme.secondary)
                        StateStat("Missed", activeInsights.missedCount, MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Category Performance
            if (activeInsights.categoryBreakdown.isNotEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Category Breakdown", style = MaterialTheme.typography.titleMedium)
                            activeInsights.categoryBreakdown.forEach { item ->
                                val catColor = Color(item.category.colorHex)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.category.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${item.completed}/${item.scheduled} (${(item.rate * 100).roundToInt()}%)",
                                            style = MonoMetricMedium,
                                            color = catColor
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { item.rate },
                                        color = catColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Active Streak Leaderboard
            if (activeInsights.topStreaks.isNotEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Active Streaks", style = MaterialTheme.typography.titleMedium)
                            activeInsights.topStreaks.forEach { (habit, streak) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(habit.name, style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${streak}d",
                                            style = MonoMetricMedium,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (scope == AnalyticsScope.PER_HABIT) {
            // PER-HABIT SEPARATE ANALYSIS + INTERACTIVE CALENDAR & INSIGHT CLUSTER
            if (uiState.perHabitAnalytics.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Create habits to view individual consistency heatmaps, calendars, and deep statistics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedHabitId == null,
                            onClick = { selectedHabitId = null },
                            label = { Text("All Habits") }
                        )
                        uiState.perHabitAnalytics.forEach { analytics ->
                            val (hIcon, hColor) = habitIconAndColor(analytics.habit, analytics.category)
                            FilterChip(
                                selected = selectedHabitId == analytics.habit.id,
                                onClick = { selectedHabitId = analytics.habit.id },
                                label = { Text(analytics.habit.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = hIcon,
                                        contentDescription = null,
                                        tint = hColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                val selectedAnalytics = selectedHabitId?.let { id ->
                    uiState.perHabitAnalytics.firstOrNull { it.habit.id == id }
                }

                if (selectedAnalytics == null) {
                    items(uiState.perHabitAnalytics, key = { it.habit.id }) { analytics ->
                        PerHabitAnalyticsCard(
                            analytics = analytics,
                            period = period,
                            onSelect = { selectedHabitId = analytics.habit.id }
                        )
                    }
                } else {
                    item(key = "detail_${selectedAnalytics.habit.id}") {
                        PerHabitDetailAnalysis(
                            analytics = selectedAnalytics,
                            logicalToday = uiState.logicalToday,
                            onEditHabit = { onEditHabit(selectedAnalytics.habit) },
                            onOpenQuickLog = onOpenQuickLog
                        )
                    }
                }
            }
        } else if (scope == AnalyticsScope.ACTIVITIES) {
            val acts = uiState.activityAnalytics
            val periodTotal = if (period == AnalyticsPeriod.WEEKLY) acts.weeklyTotal else acts.monthlyTotal
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        label = if (period == AnalyticsPeriod.WEEKLY) "7-Day Events" else "30-Day Events",
                        value = "$periodTotal",
                        accent = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Unique Types",
                        value = "${acts.breakdowns.size}",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (acts.breakdowns.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No occasional activities logged yet. Use the Occasional Activities card on Home (e.g., Cricket, Badminton, Biryani, Alcohol) to log them without affecting habit streaks.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (period == AnalyticsPeriod.WEEKLY) "7-Day Frequency" else "30-Day Frequency",
                                style = MaterialTheme.typography.titleMedium
                            )
                            acts.breakdowns.forEach { b ->
                                val count = if (period == AnalyticsPeriod.WEEKLY) b.weeklyCount else b.monthlyCount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = activityKindIcon(b.kind),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(b.name, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = "${b.kind.label} • Last: ${com.example.util.DateHelper.formatReadable(b.lastDate)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${count}x (${b.totalCount} all-time)",
                                        style = MonoMetricMedium,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Recent Activity Log", style = MaterialTheme.typography.titleMedium)
                            acts.recentActivities.take(15).forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.name, style = MaterialTheme.typography.bodyMedium)
                                        if (entry.detail.isNotBlank()) {
                                            Text(
                                                text = entry.detail,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = com.example.util.DateHelper.formatReadable(entry.logDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // STRESS ANALYSIS (WEEKLY & MONTHLY)
            val stress = uiState.stressAnalytics
            val counts = if (period == AnalyticsPeriod.WEEKLY) stress.weeklyCounts else stress.monthlyCounts
            val logs = if (period == AnalyticsPeriod.WEEKLY) stress.weeklyLogs else stress.monthlyLogs
            val avgScore = if (period == AnalyticsPeriod.WEEKLY) stress.weeklyAvgScore else stress.monthlyAvgScore

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        label = "Avg Score (1–4)",
                        value = String.format(java.util.Locale.US, "%.1f", avgScore),
                        accent = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Check-Ins",
                        value = "${logs.size}",
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (period == AnalyticsPeriod.WEEKLY) "7-Day Stress Distribution" else "30-Day Stress Distribution",
                            style = MaterialTheme.typography.titleMedium
                        )
                        val total = logs.size.coerceAtLeast(1)
                        com.example.data.model.StressLevel.entries.forEach { level ->
                            val count = counts[level] ?: 0
                            val ratio = count.toFloat() / total.toFloat()
                            val barColor = when (level) {
                                com.example.data.model.StressLevel.LOW -> MaterialTheme.colorScheme.primary
                                com.example.data.model.StressLevel.MODERATE -> MaterialTheme.colorScheme.tertiary
                                com.example.data.model.StressLevel.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                                com.example.data.model.StressLevel.VERY_HIGH -> MaterialTheme.colorScheme.error
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(level.label, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "$count days (${(ratio * 100).roundToInt()}%)",
                                        style = MonoMetricMedium,
                                        color = barColor
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = barColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }

            if (stress.recentLogs.isNotEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Recent Stress Check-Ins", style = MaterialTheme.typography.titleMedium)
                            stress.recentLogs.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = com.example.util.DateHelper.formatReadable(entry.logDate),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = entry.level.label,
                                        style = MonoMetricMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerHabitDetailAnalysis(
    analytics: HabitAnalytics,
    logicalToday: String,
    onEditHabit: () -> Unit,
    onOpenQuickLog: (DayHabitSummary, String) -> Unit
) {
    val (habitIcon, accentColor) = habitIconAndColor(analytics.habit, analytics.category)
    val todayCal = remember(logicalToday) { DateHelper.parse(logicalToday) }
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }
    var selectedDate by remember(logicalToday) { mutableStateOf(logicalToday) }
    var perfWindow by remember { mutableStateOf(DetailPerformanceWindow.ALL) }

    val monthDays = remember(displayYear, displayMonth) {
        DateHelper.daysInMonth(displayYear, displayMonth)
    }
    val leadingEmpty = remember(displayYear, displayMonth) {
        DateHelper.firstDayOfMonthOffsetSunZero(displayYear, displayMonth)
    }

    val selectedDaySummary = remember(analytics, selectedDate, logicalToday) {
        analytics.daySummariesByDate[selectedDate] ?: LifeOsRepository.evaluateDaySummary(
            habit = analytics.habit,
            category = analytics.category,
            logsForDay = emptyList(),
            allLogsForHabit = emptyList(),
            targetDate = selectedDate,
            logicalToday = logicalToday,
            precomputedStreak = analytics.currentStreak
        )
    }

    val dailySnapshotPercent = remember(selectedDaySummary) {
        val tgt = selectedDaySummary.targetValue.coerceAtLeast(1.0)
        ((selectedDaySummary.totalValue / tgt) * 100.0).coerceIn(0.0, 100.0).roundToInt()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Habit Header + Edit Button
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = habitIcon,
                                contentDescription = analytics.habit.name,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(analytics.habit.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${analytics.category?.name ?: "General"} • ${analytics.habit.scheduleType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor
                        )
                    }
                }
                OutlinedButton(onClick = onEditHabit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        }

        // Select a Date Calendar (Sun - Sat)
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
                Text("Select a Date", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayMonth == 0) {
                            displayMonth = 11
                            displayYear--
                        } else {
                            displayMonth--
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = DateHelper.formatMonthYear(displayYear, displayMonth),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = {
                        if (displayMonth == 11) {
                            displayMonth = 0
                            displayYear++
                        } else {
                            displayMonth++
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val cells: List<String?> = List(leadingEmpty) { null } + monthDays
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until 7) {
                            val dateStr = week.getOrNull(i)
                            if (dateStr == null) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val isSelected = dateStr == selectedDate
                                val daySum = analytics.daySummariesByDate[dateStr]
                                val dayNum = dateStr.takeLast(2).trimStart('0')
                                val bgColor = when {
                                    isSelected -> accentColor
                                    daySum?.status == LogStatus.COMPLETED -> accentColor.copy(alpha = 0.28f)
                                    daySum?.status == LogStatus.PARTIALLY_COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                                    daySum?.status == LogStatus.MISSED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                    daySum?.status == LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.secondaryContainer
                                    dateStr == logicalToday -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                }
                                val fgColor = when {
                                    isSelected -> Color.White
                                    daySum?.status == LogStatus.MISSED -> MaterialTheme.colorScheme.error
                                    daySum?.status == LogStatus.COMPLETED -> accentColor
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .clickable { selectedDate = dateStr },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected || dateStr == logicalToday) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = fgColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Log for Selected Date
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable { onOpenQuickLog(selectedDaySummary, selectedDate) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Log for ${DateHelper.formatMonthDayFull(selectedDate)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = analytics.habit.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${formatCleanNumber(selectedDaySummary.totalValue)} / ${formatCleanNumber(selectedDaySummary.targetValue)}",
                        style = MonoMetricLarge,
                        color = accentColor
                    )
                    StatusPill(selectedDaySummary.status)
                }
            }
        }

        // All-Time Statistics (6-card grid)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("All-Time Statistics", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard("Current Streak", "${analytics.currentStreak}", accentColor, Modifier.weight(1f))
                    KpiCard("Best Streak", "${analytics.bestStreak}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    KpiCard("Completions", "${analytics.allTimeCompleted}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard("Surplus", "${analytics.surplusPercent}%", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    KpiCard("Debt", "${analytics.debtPercent}%", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                    val netText = if (analytics.netBalance > 0) "+${analytics.netBalance}" else "${analytics.netBalance}"
                    val netColor = if (analytics.netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    KpiCard("Net Balance", netText, netColor, Modifier.weight(1f))
                }
            }
        }

        // Performance (7D / 30D / All)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            val (wRate, wDone, wPart, wSkip, wMiss) = when (perfWindow) {
                DetailPerformanceWindow.SEVEN_D -> listOf(
                    analytics.weeklyRate, analytics.weeklyCompleted, analytics.weeklyPartial, analytics.weeklySkipped, analytics.weeklyMissed
                )
                DetailPerformanceWindow.THIRTY_D -> listOf(
                    analytics.monthlyRate, analytics.monthlyCompleted, analytics.monthlyPartial, analytics.monthlySkipped, analytics.monthlyMissed
                )
                DetailPerformanceWindow.ALL -> listOf(
                    analytics.allTimeRate, analytics.allTimeCompleted, analytics.allTimePartial, analytics.allTimeSkipped, analytics.allTimeMissed
                )
            }
            val rateFloat = wRate as Float
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Performance", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailPerformanceWindow.entries.forEach { w ->
                            FilterChip(
                                selected = perfWindow == w,
                                onClick = { perfWindow = w },
                                label = { Text(w.label) }
                            )
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { rateFloat.coerceIn(0f, 1f) },
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StateStat("Done", wDone as Int, accentColor)
                    StateStat("Partial", wPart as Int, MaterialTheme.colorScheme.tertiary)
                    StateStat("Skipped", wSkip as Int, MaterialTheme.colorScheme.secondary)
                    StateStat("Missed", wMiss as Int, MaterialTheme.colorScheme.error)
                }
            }
        }

        // Insight Cluster
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Insight Cluster", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        "All-Time Consistency",
                        "${(analytics.allTimeRate * 100).roundToInt()}%",
                        accentColor,
                        Modifier.weight(1f)
                    )
                    KpiCard(
                        "7-Day Focus",
                        "${(analytics.weeklyRate * 100).roundToInt()}%",
                        MaterialTheme.colorScheme.primary,
                        Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        "Surplus Rate",
                        "${analytics.surplusPercent}%",
                        MaterialTheme.colorScheme.secondary,
                        Modifier.weight(1f)
                    )
                    KpiCard(
                        "Daily Snapshot",
                        "$dailySnapshotPercent%",
                        MaterialTheme.colorScheme.tertiary,
                        Modifier.weight(1f)
                    )
                }
            }
        }

        // 12-Week Consistency Heatmap
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("12-Week Consistency Heatmap", style = MaterialTheme.typography.titleMedium)
                HabitHeatmapGrid(
                    cells = analytics.heatmapDays,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun PerHabitAnalyticsCard(
    analytics: HabitAnalytics,
    period: AnalyticsPeriod,
    onSelect: () -> Unit
) {
    val (habitIcon, catColor) = habitIconAndColor(analytics.habit, analytics.category)
    val rate = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyRate else analytics.monthlyRate
    val completed = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyCompleted else analytics.monthlyCompleted
    val partial = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyPartial else analytics.monthlyPartial
    val skipped = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklySkipped else analytics.monthlySkipped
    val missed = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyMissed else analytics.monthlyMissed

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable { onSelect() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = catColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = habitIcon,
                                contentDescription = analytics.habit.name,
                                tint = catColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(analytics.habit.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${analytics.category?.name ?: "General"} • Tap for calendar & full stats",
                            style = MaterialTheme.typography.bodySmall,
                            color = catColor
                        )
                    }
                }
                Text(
                    text = "${(rate * 100).roundToInt()}%",
                    style = MonoMetricLarge,
                    color = catColor
                )
            }

            // 12-Week Consistency Heatmap
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "12-Week Consistency Heatmap",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HabitHeatmapGrid(
                    cells = analytics.heatmapDays,
                    accentColor = catColor
                )
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StateStat("Done", completed, catColor)
                StateStat("Partial", partial, MaterialTheme.colorScheme.tertiary)
                StateStat("Skipped", skipped, MaterialTheme.colorScheme.secondary)
                StateStat("Missed", missed, MaterialTheme.colorScheme.error)
                StateStat("Streak", analytics.currentStreak, MaterialTheme.colorScheme.tertiary)
                StateStat("Best", analytics.bestStreak, MaterialTheme.colorScheme.primary)
            }

            if (period == AnalyticsPeriod.MONTHLY && analytics.habit.trackingType != TrackingType.BOOLEAN) {
                Text(
                    text = "30-Day Total Logged: ${formatCleanNumber(analytics.monthlyTotalValue)} ${analytics.habit.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MonoMetricLarge,
                color = accent
            )
        }
    }
}

@Composable
private fun StateStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", style = MonoMetricMedium, color = color)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
