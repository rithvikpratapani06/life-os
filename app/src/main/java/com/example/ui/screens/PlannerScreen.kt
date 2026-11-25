package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.TrackingType
import com.example.data.repository.DayHabitSummary
import com.example.ui.LifeOsUiState
import com.example.ui.components.DayHabitCard
import com.example.ui.components.activityKindIcon
import com.example.util.DateHelper
import java.util.Calendar

@Composable
fun PlannerScreen(
    uiState: LifeOsUiState,
    onSelectDate: (String) -> Unit,
    onQuickToggleBoolean: (DayHabitSummary) -> Unit,
    onQuickNoBoolean: (DayHabitSummary) -> Unit,
    onLogTwiceDailySlot: (DayHabitSummary, String, Boolean, Boolean) -> Unit,
    onOpenQuickLog: (DayHabitSummary) -> Unit,
    onOpenLogActivity: () -> Unit,
    onDeleteActivity: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCal = remember(uiState.logicalToday) { DateHelper.parse(uiState.logicalToday) }
    var displayYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) }

    val monthDays = remember(displayYear, displayMonth) {
        DateHelper.daysInMonth(displayYear, displayMonth)
    }
    val leadingEmpty = remember(displayYear, displayMonth) {
        DateHelper.firstDayOfMonthOffsetMonZero(displayYear, displayMonth)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Planner", style = MaterialTheme.typography.headlineMedium)
        }

        // Calendar Card
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val cells: List<String?> = List(leadingEmpty) { null } + monthDays
                    val rows = cells.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 0 until 7) {
                                val dateStr = week.getOrNull(i)
                                if (dateStr == null) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val isSelected = dateStr == uiState.selectedPlannerDate
                                    val isToday = dateStr == uiState.logicalToday
                                    val rate = uiState.monthDayCompletionMap[dateStr] ?: 0f
                                    val dayNumber = dateStr.takeLast(2).trimStart('0')

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    rate >= 0.99f -> MaterialTheme.colorScheme.primaryContainer
                                                    rate > 0f -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                                    isToday -> MaterialTheme.colorScheme.surfaceVariant
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .clickable { onSelectDate(dateStr) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                rate >= 0.99f -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = DateHelper.formatReadable(uiState.selectedPlannerDate),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (uiState.plannerSummaries.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No habits scheduled for this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(uiState.plannerSummaries, key = { "${uiState.selectedPlannerDate}_${it.habit.id}" }) { summary ->
                DayHabitCard(
                    summary = summary,
                    onQuickAction = {
                        if (summary.habit.trackingType == TrackingType.BOOLEAN) {
                            if (summary.targetValue > 1.0 && summary.status == com.example.data.model.LogStatus.COMPLETED) {
                                onOpenQuickLog(summary)
                            } else {
                                onQuickToggleBoolean(summary)
                            }
                        } else {
                            onOpenQuickLog(summary)
                        }
                    },
                    onQuickNo = { onQuickNoBoolean(summary) },
                    onLogSlot = { slot, isDone, isSkip ->
                        onLogTwiceDailySlot(summary, slot, isDone, isSkip)
                    },
                    onOpenDetails = { onOpenQuickLog(summary) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Occasional Activities", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(
                    onClick = onOpenLogActivity,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Log Event")
                }
            }
        }

        val dayActivities = uiState.activityAnalytics.plannerActivities
        if (dayActivities.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No occasional activities logged on this date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(dayActivities, key = { "act_${it.id}" }) { act ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = activityKindIcon(act.kind),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(act.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = if (act.detail.isNotBlank()) "${act.kind.label} • ${act.detail}" else act.kind.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteActivity(act.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete activity",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
