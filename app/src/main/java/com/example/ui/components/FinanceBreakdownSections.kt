package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.ui.DailySpendPoint
import com.example.ui.DaySpendSummary
import com.example.ui.MonthSpendSummary
import com.example.ui.WeekSpendSummary
import com.example.ui.theme.MonoMetricMedium
import com.example.util.DateHelper

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DayWiseSpendingSection(
    logicalToday: String,
    selectedDate: String,
    last7Days: List<DailySpendPoint>,
    daySummaries: List<DaySpendSummary>,
    onSelectDate: (String) -> Unit,
    onAddForDate: (String) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onOpenDaySheet: (String) -> Unit,
    onTapCategory: (ExpenseCategory) -> Unit,
    onTapPaymentMethod: (PaymentMethod) -> Unit
) {
    var showJumpDatePicker by remember { mutableStateOf(false) }
    val selectedDaySummary = remember(selectedDate, daySummaries) {
        daySummaries.firstOrNull { it.date == selectedDate }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Interactive 7-day chart + Jump to Date
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("7-Day Daily Spending", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Tap any day to see all expenses for that day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { showJumpDatePicker = true },
                        modifier = Modifier.testTag("jump_expense_date_button")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Any Date")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                InteractiveDailyBarStrip(
                    points = last7Days,
                    selectedDate = selectedDate,
                    onSelectDate = { date ->
                        onSelectDate(date)
                    }
                )
            }
        }

        // Selected Day Full Breakdown Card
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val dayTitle = if (selectedDate == logicalToday) {
                            "Today • ${DateHelper.formatReadable(selectedDate)}"
                        } else {
                            DateHelper.formatReadable(selectedDate)
                        }
                        Text(dayTitle, style = MaterialTheme.typography.titleMedium)
                        val count = selectedDaySummary?.count ?: 0
                        Text(
                            text = "$count ${if (count == 1) "expense" else "expenses"} logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatCurrency(selectedDaySummary?.total ?: 0.0),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { onAddForDate(selectedDate) }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }

                if (selectedDaySummary == null || selectedDaySummary.expenses.isEmpty()) {
                    Text(
                        text = "No expenses logged on ${DateHelper.formatReadable(selectedDate)}. Tap Add above to record missed spending for this day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    if (selectedDaySummary.categoryTotals.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedDaySummary.categoryTotals.forEach { (cat, sum) ->
                                AssistChip(
                                    onClick = { onTapCategory(cat) },
                                    label = { Text("${cat.label}: ${formatCurrency(sum)}") }
                                )
                            }
                            selectedDaySummary.paymentTotals.forEach { (method, sum) ->
                                AssistChip(
                                    onClick = { onTapPaymentMethod(method) },
                                    label = { Text("${method.label}: ${formatCurrency(sum)}") }
                                )
                            }
                        }
                    }

                    selectedDaySummary.expenses.forEach { exp ->
                        ExpenseItemRow(
                            expense = exp,
                            onEdit = onEditExpense,
                            onDelete = onDeleteExpense
                        )
                    }
                }
            }
        }

        // All Days With Spending History
        if (daySummaries.isNotEmpty()) {
            Text(
                text = "All Days History (${daySummaries.size} days) • Tap any day",
                style = MaterialTheme.typography.titleSmall
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                daySummaries.forEach { day ->
                    val isSelected = day.date == selectedDate
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectDate(day.date)
                                onOpenDaySheet(day.date)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (day.date == logicalToday) {
                                        "Today (${DateHelper.formatReadable(day.date)})"
                                    } else {
                                        DateHelper.formatReadable(day.date)
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                val topCat = day.categoryTotals.firstOrNull()?.first?.label ?: ""
                                Text(
                                    text = "${day.count} ${if (day.count == 1) "item" else "items"}" +
                                        if (topCat.isNotBlank()) " • Top: $topCat" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatCurrency(day.total),
                                    style = MonoMetricMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "View day details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJumpDatePicker) {
        val maxUtcMillis = remember(logicalToday) { DateHelper.toUtcMillis(logicalToday) }
        val maxYear = remember(logicalToday) { logicalToday.take(4).toIntOrNull() ?: 2100 }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateHelper.toUtcMillis(selectedDate),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= maxUtcMillis
                override fun isSelectableYear(year: Int): Boolean = year <= maxYear
            }
        )
        DatePickerDialog(
            onDismissRequest = { showJumpDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onSelectDate(DateHelper.fromUtcMillis(millis))
                        }
                        showJumpDatePicker = false
                    }
                ) {
                    Text("View Day")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun WeekWiseSpendingSection(
    weekSummaries: List<WeekSpendSummary>,
    selectedWeekStart: String?,
    onSelectWeek: (String) -> Unit,
    onOpenDaySheet: (String) -> Unit,
    onTapCategory: (WeekSpendSummary, ExpenseCategory) -> Unit,
    onTapPaymentMethod: (WeekSpendSummary, PaymentMethod) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit
) {
    if (weekSummaries.isEmpty()) {
        EmptySpendingCard("No weekly expense data yet. Tap + Expense to start logging.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Week-Wise Spending (${weekSummaries.size} ${if (weekSummaries.size == 1) "week" else "weeks"}) • Tap a week or day",
            style = MaterialTheme.typography.titleSmall
        )
        weekSummaries.forEach { week ->
            val isExpanded = week.weekStartDate == selectedWeekStart
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (isExpanded) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWeek(week.weekStartDate) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(week.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${week.count} items • ${week.activeDaysCount} active days • Avg ${formatCurrency(week.dailyAverage)}/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatCurrency(week.total),
                                style = MonoMetricMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse week" else "Expand week",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Day-by-Day in This Week (tap any day)",
                                style = MaterialTheme.typography.labelMedium
                            )
                            InteractiveDailyBarStrip(
                                points = week.dayPoints,
                                selectedDate = null,
                                onSelectDate = onOpenDaySheet
                            )
                            InteractiveBreakdownCard(
                                title = "What You Spent For (This Week)",
                                rows = week.categoryTotals.map { (c, amt) -> c.label to amt },
                                total = week.total,
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onRowClick = { idx ->
                                    week.categoryTotals.getOrNull(idx)?.first?.let { cat ->
                                        onTapCategory(week, cat)
                                    }
                                }
                            )
                            InteractiveBreakdownCard(
                                title = "Payment Methods (This Week)",
                                rows = week.paymentTotals.map { (m, amt) -> m.label to amt },
                                total = week.total,
                                accentColor = MaterialTheme.colorScheme.primary,
                                onRowClick = { idx ->
                                    week.paymentTotals.getOrNull(idx)?.first?.let { method ->
                                        onTapPaymentMethod(week, method)
                                    }
                                }
                            )
                            Text("All Week Expenses (${week.expenses.size})", style = MaterialTheme.typography.labelMedium)
                            week.expenses.forEach { exp ->
                                ExpenseItemRow(
                                    expense = exp,
                                    onEdit = onEditExpense,
                                    onDelete = onDeleteExpense
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
fun MonthWiseSpendingSection(
    monthSummaries: List<MonthSpendSummary>,
    selectedMonthPrefix: String?,
    onSelectMonth: (String) -> Unit,
    onOpenDaySheet: (String) -> Unit,
    onTapCategory: (MonthSpendSummary, ExpenseCategory) -> Unit,
    onTapPaymentMethod: (MonthSpendSummary, PaymentMethod) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit
) {
    if (monthSummaries.isEmpty()) {
        EmptySpendingCard("No monthly expense data yet. Tap + Expense to start logging.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Month-Wise Spending (${monthSummaries.size} ${if (monthSummaries.size == 1) "month" else "months"}) • Tap a month or day",
            style = MaterialTheme.typography.titleSmall
        )
        monthSummaries.forEach { month ->
            val isExpanded = month.monthPrefix == selectedMonthPrefix
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (isExpanded) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (isExpanded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMonth(month.monthPrefix) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(month.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${month.count} items • ${month.activeDaysCount} spending days • Avg ${formatCurrency(month.dailyAverage)}/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatCurrency(month.total),
                                style = MonoMetricMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse month" else "Expand month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Day-Wise Totals in ${month.label} (tap any day)",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                month.daySummaries.forEach { day ->
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        modifier = Modifier.clickable { onOpenDaySheet(day.date) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = DateHelper.formatShortMonthDay(day.date),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatCurrency(day.total),
                                                style = MonoMetricMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "${day.count} ${if (day.count == 1) "item" else "items"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            InteractiveBreakdownCard(
                                title = "What You Spent For (${month.label})",
                                rows = month.categoryTotals.map { (c, amt) -> c.label to amt },
                                total = month.total,
                                accentColor = MaterialTheme.colorScheme.secondary,
                                onRowClick = { idx ->
                                    month.categoryTotals.getOrNull(idx)?.first?.let { cat ->
                                        onTapCategory(month, cat)
                                    }
                                }
                            )
                            InteractiveBreakdownCard(
                                title = "Payment Methods (${month.label})",
                                rows = month.paymentTotals.map { (m, amt) -> m.label to amt },
                                total = month.total,
                                accentColor = MaterialTheme.colorScheme.primary,
                                onRowClick = { idx ->
                                    month.paymentTotals.getOrNull(idx)?.first?.let { method ->
                                        onTapPaymentMethod(month, method)
                                    }
                                }
                            )
                            Text("All Month Expenses (${month.expenses.size})", style = MaterialTheme.typography.labelMedium)
                            month.expenses.forEach { exp ->
                                ExpenseItemRow(
                                    expense = exp,
                                    onEdit = onEditExpense,
                                    onDelete = onDeleteExpense
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
fun InteractiveDailyBarStrip(
    points: List<DailySpendPoint>,
    selectedDate: String?,
    onSelectDate: (String) -> Unit
) {
    val maxSpend = points.maxOfOrNull { it.total }?.coerceAtLeast(1.0) ?: 1.0
    val defaultBarColor = MaterialTheme.colorScheme.secondary
    val selectedBarColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        points.forEach { point ->
            val isSelected = point.date == selectedDate
            val ratio = (point.total / maxSpend).toFloat().coerceIn(0f, 1f)
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onSelectDate(point.date) }
                    .testTag("day_bar_${point.date}")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(22.dp)
                            .height(82.dp)
                    ) {
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        val fillHeight = size.height * ratio
                        if (fillHeight > 0f) {
                            drawRoundRect(
                                color = if (isSelected) selectedBarColor else defaultBarColor,
                                topLeft = Offset(0f, size.height - fillHeight),
                                size = Size(size.width, fillHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (point.total > 0.0) formatCurrency(point.total) else "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (point.total > 0.0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveBreakdownCard(
    title: String,
    rows: List<Pair<String, Double>>,
    total: Double,
    accentColor: Color,
    onRowClick: (Int) -> Unit
) {
    if (rows.isEmpty()) return
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Tap to view details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val safeTotal = total.coerceAtLeast(1.0)
            rows.forEachIndexed { idx, (label, amount) ->
                val ratio = (amount / safeTotal).toFloat().coerceIn(0f, 1f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRowClick(idx) }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${formatCurrency(amount)} (${(ratio * 100).toInt()}%)",
                                style = MonoMetricMedium,
                                color = accentColor
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View $label expenses",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        color = accentColor,
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

@Composable
private fun EmptySpendingCard(message: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}
