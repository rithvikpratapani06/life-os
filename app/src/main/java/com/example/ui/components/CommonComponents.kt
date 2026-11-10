package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.LogStatus
import com.example.data.model.ScheduleType
import com.example.data.model.TrackingType
import com.example.data.repository.DayHabitSummary
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import kotlin.math.roundToInt

fun categoryIconFor(key: String): ImageVector = when (key) {
    "health" -> Icons.Default.Favorite
    "fitness" -> Icons.Default.FitnessCenter
    "learning" -> Icons.Default.MenuBook
    "productivity" -> Icons.Default.CheckCircle
    "care" -> Icons.Default.Spa
    "mindfulness" -> Icons.Default.SelfImprovement
    else -> Icons.Default.Category
}

@Composable
fun DailyProgressRing(
    progress: Float,
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "ringProgress"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = 10.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).roundToInt()}%",
                style = MonoMetricLarge.copy(fontSize = 20.spOrFallback()),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Int.spOrFallback() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

@Composable
fun StatusPill(status: LogStatus) {
    val (bg, fg) = when (status) {
        LogStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        LogStatus.PARTIALLY_COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        LogStatus.MISSED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        LogStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun DayHabitCard(
    summary: DayHabitSummary,
    onQuickAction: () -> Unit,
    onOpenDetails: () -> Unit,
    onQuickNo: (() -> Unit)? = null,
    onLogSlot: ((slot: String, isDone: Boolean, isSkip: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val catColor = summary.category?.let { Color(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val isCompleted = summary.status == LogStatus.COMPLETED
    val isSkipped = summary.status == LogStatus.INTENTIONALLY_SKIPPED

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onOpenDetails() }
            .testTag("habit_card_${summary.habit.id}"),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) catColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        tonalElevation = if (isCompleted) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(catColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = summary.habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (summary.currentStreak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${summary.currentStreak}d",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = summary.category?.name ?: "General",
                        style = MaterialTheme.typography.bodySmall,
                        color = catColor
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.habit.scheduleType == ScheduleType.TWICE_DAILY) {
                        StatusPill(summary.status)
                    } else if (summary.habit.trackingType == TrackingType.BOOLEAN && summary.targetValue <= 1.0) {
                        StatusPill(summary.status)
                    } else {
                        val valStr = formatCleanNumber(summary.totalValue)
                        val tgtStr = formatCleanNumber(summary.targetValue)
                        Text(
                            text = "$valStr / $tgtStr ${summary.habit.unit}",
                            style = MonoMetricMedium.copy(fontSize = 12.spOrFallback()),
                            color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isSkipped || summary.status == LogStatus.MISSED || summary.status == LogStatus.PARTIALLY_COMPLETED) {
                            StatusPill(summary.status)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (summary.habit.scheduleType == ScheduleType.TWICE_DAILY && onLogSlot != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TwiceDailySlotChip(
                        label = "AM",
                        slotStatus = summary.morningStatus,
                        accentColor = catColor,
                        onClick = {
                            if (summary.morningStatus == LogStatus.COMPLETED) {
                                onOpenDetails()
                            } else {
                                onLogSlot("MORNING", true, false)
                            }
                        },
                        modifier = Modifier.testTag("habit_am_action_${summary.habit.id}")
                    )
                    TwiceDailySlotChip(
                        label = "PM",
                        slotStatus = summary.eveningStatus,
                        accentColor = catColor,
                        onClick = {
                            if (summary.eveningStatus == LogStatus.COMPLETED) {
                                onOpenDetails()
                            } else {
                                onLogSlot("EVENING", true, false)
                            }
                        },
                        modifier = Modifier.testTag("habit_pm_action_${summary.habit.id}")
                    )
                }
            } else if (summary.habit.trackingType == TrackingType.BOOLEAN && summary.targetValue <= 1.0 && onQuickNo != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isExplicitNo = summary.status == LogStatus.MISSED
                    IconButton(
                        onClick = onQuickNo,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("habit_no_action_${summary.habit.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isExplicitNo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "No / Not done",
                                    tint = if (isExplicitNo) Color.White else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onQuickAction,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("habit_quick_action_${summary.habit.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isCompleted) catColor else catColor.copy(alpha = 0.16f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Yes / Completed",
                                    tint = if (isCompleted) Color.White else catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                IconButton(
                    onClick = onQuickAction,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("habit_quick_action_${summary.habit.id}")
                ) {
                    when {
                        isCompleted -> Surface(
                            shape = CircleShape,
                            color = catColor,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        isSkipped -> Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skipped",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        summary.habit.trackingType == TrackingType.BOOLEAN && summary.targetValue <= 1.0 -> Icon(
                            imageVector = Icons.Outlined.Circle,
                            contentDescription = "Mark complete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        else -> Surface(
                            shape = CircleShape,
                            color = catColor.copy(alpha = 0.14f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Log progress",
                                    tint = catColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TwiceDailySlotChip(
    label: String,
    slotStatus: LogStatus?,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when (slotStatus) {
        LogStatus.COMPLETED -> accentColor
        LogStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
        LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }
    val fg = when (slotStatus) {
        LogStatus.COMPLETED -> Color.White
        LogStatus.MISSED -> MaterialTheme.colorScheme.error
        LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val icon = when (slotStatus) {
        LogStatus.COMPLETED -> Icons.Default.Check
        LogStatus.MISSED -> Icons.Default.Close
        LogStatus.INTENTIONALLY_SKIPPED -> Icons.Default.SkipNext
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = if (slotStatus == null || slotStatus == LogStatus.SCHEDULED) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        } else null,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$label ${slotStatus?.label ?: "Pending"}",
                    tint = fg,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = fg
            )
        }
    }
}

@Composable
fun QuickLogDialog(
    summary: DayHabitSummary,
    dateLabel: String,
    onDismiss: () -> Unit,
    onLogValue: (Double, String) -> Unit,
    onExplicitNo: (String) -> Unit,
    onSkip: (String) -> Unit,
    onClearDay: () -> Unit,
    onLogSlot: ((slot: String, isDone: Boolean, isSkip: Boolean) -> Unit)? = null
) {
    val habit = summary.habit
    val isTwiceDaily = habit.scheduleType == ScheduleType.TWICE_DAILY && onLogSlot != null
    var inputText by remember {
        mutableStateOf(
            when (habit.trackingType) {
                TrackingType.NUMERIC -> "5"
                TrackingType.DURATION -> "15"
                TrackingType.QUANTITY -> "1"
                TrackingType.RATING -> "4"
                TrackingType.BOOLEAN -> "1"
            }
        )
    }
    var selectedRating by remember { mutableStateOf(4) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(habit.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current: ${formatCleanNumber(summary.totalValue)} / ${formatCleanNumber(summary.targetValue)} ${habit.unit}",
                    style = MonoMetricMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isTwiceDaily) {
                    Text(
                        text = "Log Morning and Night independently for $dateLabel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TwiceDailySlotRow(
                        title = "Morning (AM)",
                        slotStatus = summary.morningStatus,
                        onYes = { onLogSlot?.invoke("MORNING", true, false) },
                        onNo = { onLogSlot?.invoke("MORNING", false, false) },
                        onSkip = { onLogSlot?.invoke("MORNING", false, true) }
                    )
                    TwiceDailySlotRow(
                        title = "Night (PM)",
                        slotStatus = summary.eveningStatus,
                        onYes = { onLogSlot?.invoke("EVENING", true, false) },
                        onNo = { onLogSlot?.invoke("EVENING", false, false) },
                        onSkip = { onLogSlot?.invoke("EVENING", false, true) }
                    )
                    if (summary.logs.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                onClearDay()
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reset Day")
                        }
                    }
                } else {
                    when (habit.trackingType) {
                        TrackingType.BOOLEAN -> {
                            Text(
                                text = "Mark this habit completed or intentionally skip it for $dateLabel.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        TrackingType.RATING -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                (1..5).forEach { score ->
                                    FilterChip(
                                        selected = selectedRating == score,
                                        onClick = { selectedRating = score },
                                        label = { Text("$score") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                label = { Text("Add ${habit.unit}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_log_value_input")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note or skip reason (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (habit.trackingType == TrackingType.BOOLEAN && habit.targetValue <= 1.0) {
                            OutlinedButton(
                                onClick = {
                                    onExplicitNo(noteText)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("no_habit_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("No")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onSkip(noteText)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("skip_habit_button")
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Skip")
                        }
                        if (summary.logs.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    onClearDay()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isTwiceDaily) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("confirm_log_button")
                ) {
                    Text("Done")
                }
            } else {
                Button(
                    onClick = {
                        val value = when (habit.trackingType) {
                            TrackingType.BOOLEAN -> 1.0
                            TrackingType.RATING -> selectedRating.toDouble()
                            else -> inputText.toDoubleOrNull() ?: 0.0
                        }
                        if (value > 0.0) {
                            onLogValue(value, noteText)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_log_button")
                ) {
                    Text(if (habit.trackingType == TrackingType.BOOLEAN && habit.targetValue <= 1.0) "Yes (Done)" else "Save Log")
                }
            }
        },
        dismissButton = {
            if (!isTwiceDaily) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun TwiceDailySlotRow(
    title: String,
    slotStatus: LogStatus?,
    onYes: () -> Unit,
    onNo: () -> Unit,
    onSkip: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                StatusPill(slotStatus ?: LogStatus.SCHEDULED)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = slotStatus == LogStatus.COMPLETED,
                    onClick = onYes,
                    label = { Text("Yes") },
                    leadingIcon = {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = slotStatus == LogStatus.MISSED,
                    onClick = onNo,
                    label = { Text("No") },
                    leadingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = slotStatus == LogStatus.INTENTIONALLY_SKIPPED,
                    onClick = onSkip,
                    label = { Text("Skip") },
                    leadingIcon = {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HabitHeatmapGrid(
    cells: List<com.example.ui.HeatmapCell>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val weeks = remember(cells) { cells.chunked(7) }
    var selectedCell by remember { mutableStateOf<com.example.ui.HeatmapCell?>(null) }
    val dayLetters = remember { listOf("M", "T", "W", "T", "F", "S", "S") }
    val monthLabels = remember(weeks) {
        weeks.mapIndexed { idx, week ->
            val firstDate = week.firstOrNull()?.date
            val prevFirstDate = if (idx > 0) weeks[idx - 1].firstOrNull()?.date else null
            if (firstDate != null) {
                val m = com.example.util.DateHelper.shortMonthLabel(firstDate)
                val prevM = prevFirstDate?.let { com.example.util.DateHelper.shortMonthLabel(it) }
                if (idx == 0 || m != prevM) m else ""
            } else ""
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            monthLabels.forEach { monthLabel ->
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Column(
                modifier = Modifier.width(16.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                dayLetters.forEach { d ->
                    Box(
                        modifier = Modifier.height(22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            weeks.forEach { week ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    week.forEach { cell ->
                        val isSelected = selectedCell?.date == cell.date
                        val isSkipped = cell.status == LogStatus.INTENTIONALLY_SKIPPED
                        val cellColor = when (cell.status) {
                            LogStatus.COMPLETED -> accentColor
                            LogStatus.PARTIALLY_COMPLETED -> accentColor.copy(alpha = 0.45f)
                            LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                            LogStatus.MISSED -> MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                            LogStatus.SCHEDULED -> MaterialTheme.colorScheme.surfaceVariant
                            null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }
                        val textColor = when (cell.status) {
                            LogStatus.COMPLETED -> Color.White
                            LogStatus.INTENTIONALLY_SKIPPED -> MaterialTheme.colorScheme.secondary
                            LogStatus.MISSED -> MaterialTheme.colorScheme.error
                            null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val dayNum = cell.date.takeLast(2).trimStart('0')

                        val cellShape = RoundedCornerShape(4.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(cellShape)
                                .background(cellColor)
                                .then(
                                    when {
                                        isSelected -> Modifier.background(Color.Transparent)
                                        else -> Modifier
                                    }
                                )
                                .clickable { selectedCell = cell },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    fontWeight = if (isSelected || isSkipped) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        selectedCell?.let { cell ->
            val readable = com.example.util.DateHelper.formatReadable(cell.date)
            val statusText = cell.status?.label ?: "Not Scheduled"
            Text(
                text = "$readable • $statusText",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeatmapLegendItem("Done", accentColor)
            HeatmapLegendItem("Partial", accentColor.copy(alpha = 0.45f))
            HeatmapLegendItem("Skipped", MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            HeatmapLegendItem("Missed", MaterialTheme.colorScheme.error.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun HeatmapLegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatCleanNumber(v: Double): String {
    return if (v % 1.0 == 0.0) v.toInt().toString() else String.format(java.util.Locale.US, "%.1f", v)
}

@Composable
fun InfoTipButton(
    title: String,
    explanation: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true },
        modifier = modifier.size(28.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Info about $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
