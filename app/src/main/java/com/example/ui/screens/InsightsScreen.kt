package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.TrackingType
import com.example.ui.HabitAnalytics
import com.example.ui.LifeOsUiState
import com.example.ui.components.HabitHeatmapGrid
import com.example.ui.components.formatCleanNumber
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import kotlin.math.roundToInt

private enum class AnalyticsPeriod(val label: String) {
    WEEKLY("Weekly (7D)"),
    MONTHLY("Monthly (30D)")
}

private enum class AnalyticsScope(val label: String) {
    OVERALL("Overall"),
    PER_HABIT("Per Habit"),
    STRESS("Stress")
}

@Composable
fun InsightsScreen(
    uiState: LifeOsUiState,
    modifier: Modifier = Modifier
) {
    var period by remember { mutableStateOf(AnalyticsPeriod.WEEKLY) }
    var scope by remember { mutableStateOf(AnalyticsScope.OVERALL) }

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
                Text("Analytics & Insights", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            // PER-HABIT SEPARATE ANALYSIS + HEATMAPS
            if (uiState.perHabitAnalytics.isEmpty()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Create habits to view individual consistency heatmaps and weekly/monthly breakdowns.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(uiState.perHabitAnalytics, key = { it.habit.id }) { analytics ->
                    PerHabitAnalyticsCard(analytics = analytics, period = period)
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
private fun PerHabitAnalyticsCard(
    analytics: HabitAnalytics,
    period: AnalyticsPeriod
) {
    val catColor = analytics.category?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val rate = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyRate else analytics.monthlyRate
    val completed = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyCompleted else analytics.monthlyCompleted
    val partial = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyPartial else analytics.monthlyPartial
    val skipped = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklySkipped else analytics.monthlySkipped
    val missed = if (period == AnalyticsPeriod.WEEKLY) analytics.weeklyMissed else analytics.monthlyMissed

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(analytics.habit.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${analytics.category?.name ?: "General"} • ${analytics.habit.scheduleType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = catColor
                    )
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
