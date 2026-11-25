package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.ui.LifeOsUiState
import com.example.ui.components.DayWiseSpendingSection
import com.example.ui.components.ExpenseDrillDownFilter
import com.example.ui.components.ExpenseDrillDownSheet
import com.example.ui.components.ExpenseEditorDialog
import com.example.ui.components.ExpenseItemRow
import com.example.ui.components.InfoTipButton
import com.example.ui.components.InteractiveBreakdownCard
import com.example.ui.components.MonthWiseSpendingSection
import com.example.ui.components.WeekWiseSpendingSection
import com.example.ui.components.formatCurrency
import com.example.ui.theme.MonoMetricLarge
import com.example.util.DateHelper

private enum class FinanceViewMode(val label: String) {
    DAY("Day Wise"),
    WEEK("Week Wise"),
    MONTH("Month Wise")
}

@Composable
fun FinanceScreen(
    uiState: LifeOsUiState,
    onAddExpense: (String, Double, ExpenseCategory, String, PaymentMethod, String) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val finance = uiState.financeSummary
    var viewMode by remember { mutableStateOf(FinanceViewMode.DAY) }
    var selectedDayDate by remember(uiState.logicalToday) { mutableStateOf(uiState.logicalToday) }
    var selectedWeekStart by remember(finance.weekSummaries) {
        mutableStateOf(
            finance.weekSummaries.firstOrNull()?.weekStartDate
                ?: DateHelper.startOfWeekMonday(uiState.logicalToday)
        )
    }
    var selectedMonthPrefix by remember(finance.monthSummaries) {
        mutableStateOf(
            finance.monthSummaries.firstOrNull()?.monthPrefix
                ?: uiState.logicalToday.take(7)
        )
    }

    var showEditorDialog by remember { mutableStateOf(false) }
    var editorDefaultDate by remember { mutableStateOf<String?>(null) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var activeDrillDown by remember { mutableStateOf<ExpenseDrillDownFilter?>(null) }

    val currentMonthPrefix = uiState.logicalToday.take(7)
    val currentMonthStart = "$currentMonthPrefix-01"
    val currentMonthLabel = remember(currentMonthPrefix) {
        DateHelper.formatMonthYearFromPrefix(currentMonthPrefix)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Finance Manager", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "Day, week & month spending intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        editingExpense = null
                        editorDefaultDate = if (viewMode == FinanceViewMode.DAY) selectedDayDate else uiState.logicalToday
                        showEditorDialog = true
                    },
                    modifier = Modifier.testTag("add_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense")
                }
            }
        }

        // Daily Prompt Banner
        item {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text("How much did you spend?", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Log today or missed days • Tap any entry to edit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            editingExpense = null
                            editorDefaultDate = uiState.logicalToday
                            showEditorDialog = true
                        }
                    ) {
                        Text("Log Now")
                    }
                }
            }
        }

        // Interactive KPI Cards: Today, This Week, This Month
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FinanceKpiCard(
                        label = "Today",
                        value = formatCurrency(finance.todaySpent),
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = {
                            viewMode = FinanceViewMode.DAY
                            selectedDayDate = uiState.logicalToday
                            activeDrillDown = ExpenseDrillDownFilter.ByDate(uiState.logicalToday)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceKpiCard(
                        label = "This Week",
                        value = formatCurrency(finance.thisWeekSpent),
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            viewMode = FinanceViewMode.WEEK
                            selectedWeekStart = DateHelper.startOfWeekMonday(uiState.logicalToday)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FinanceKpiCard(
                        label = "This Month",
                        value = formatCurrency(finance.monthSpent),
                        accent = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            viewMode = FinanceViewMode.MONTH
                            selectedMonthPrefix = currentMonthPrefix
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "This Month Daily Avg: ${formatCurrency(finance.monthDailyAverage)}/day • Tap any card or breakdown row for details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // View Mode Switcher: Day Wise | Week Wise | Month Wise
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FinanceViewMode.entries.forEach { mode ->
                        FilterChip(
                            selected = viewMode == mode,
                            onClick = { viewMode = mode },
                            label = { Text(mode.label) },
                            modifier = Modifier.testTag("finance_mode_${mode.name.lowercase()}")
                        )
                    }
                }
                InfoTipButton(
                    title = "Day, Week & Month Views",
                    explanation = "Switch between Day Wise, Week Wise, and Month Wise spending. Tap any day bar, day card, category, or payment method to see all expenses and how much you spent all day—and tap any expense to edit it."
                )
            }
        }

        // Active Period Breakdown Section
        item {
            when (viewMode) {
                FinanceViewMode.DAY -> DayWiseSpendingSection(
                    logicalToday = uiState.logicalToday,
                    selectedDate = selectedDayDate,
                    last7Days = finance.last7DaysSpending,
                    daySummaries = finance.daySummaries,
                    onSelectDate = { date -> selectedDayDate = date },
                    onAddForDate = { date ->
                        editingExpense = null
                        editorDefaultDate = date
                        showEditorDialog = true
                    },
                    onEditExpense = { exp ->
                        editingExpense = exp
                        showEditorDialog = true
                    },
                    onDeleteExpense = onDeleteExpense,
                    onOpenDaySheet = { date ->
                        activeDrillDown = ExpenseDrillDownFilter.ByDate(date)
                    },
                    onTapCategory = { cat ->
                        activeDrillDown = ExpenseDrillDownFilter.ByCategory(
                            category = cat,
                            periodLabel = DateHelper.formatReadable(selectedDayDate),
                            startDate = selectedDayDate,
                            endDate = selectedDayDate
                        )
                    },
                    onTapPaymentMethod = { method ->
                        activeDrillDown = ExpenseDrillDownFilter.ByPaymentMethod(
                            method = method,
                            periodLabel = DateHelper.formatReadable(selectedDayDate),
                            startDate = selectedDayDate,
                            endDate = selectedDayDate
                        )
                    }
                )

                FinanceViewMode.WEEK -> WeekWiseSpendingSection(
                    weekSummaries = finance.weekSummaries,
                    selectedWeekStart = selectedWeekStart,
                    onSelectWeek = { weekStart ->
                        selectedWeekStart = if (selectedWeekStart == weekStart) "" else weekStart
                    },
                    onOpenDaySheet = { date ->
                        activeDrillDown = ExpenseDrillDownFilter.ByDate(date)
                    },
                    onTapCategory = { week, cat ->
                        activeDrillDown = ExpenseDrillDownFilter.ByCategory(
                            category = cat,
                            periodLabel = week.label,
                            startDate = week.weekStartDate,
                            endDate = week.weekEndDate
                        )
                    },
                    onTapPaymentMethod = { week, method ->
                        activeDrillDown = ExpenseDrillDownFilter.ByPaymentMethod(
                            method = method,
                            periodLabel = week.label,
                            startDate = week.weekStartDate,
                            endDate = week.weekEndDate
                        )
                    },
                    onEditExpense = { exp ->
                        editingExpense = exp
                        showEditorDialog = true
                    },
                    onDeleteExpense = onDeleteExpense
                )

                FinanceViewMode.MONTH -> MonthWiseSpendingSection(
                    monthSummaries = finance.monthSummaries,
                    selectedMonthPrefix = selectedMonthPrefix,
                    onSelectMonth = { prefix ->
                        selectedMonthPrefix = if (selectedMonthPrefix == prefix) "" else prefix
                    },
                    onOpenDaySheet = { date ->
                        activeDrillDown = ExpenseDrillDownFilter.ByDate(date)
                    },
                    onTapCategory = { month, cat ->
                        activeDrillDown = ExpenseDrillDownFilter.ByCategory(
                            category = cat,
                            periodLabel = month.label,
                            startDate = "${month.monthPrefix}-01",
                            endDate = "${month.monthPrefix}-31"
                        )
                    },
                    onTapPaymentMethod = { month, method ->
                        activeDrillDown = ExpenseDrillDownFilter.ByPaymentMethod(
                            method = method,
                            periodLabel = month.label,
                            startDate = "${month.monthPrefix}-01",
                            endDate = "${month.monthPrefix}-31"
                        )
                    },
                    onEditExpense = { exp ->
                        editingExpense = exp
                        showEditorDialog = true
                    },
                    onDeleteExpense = onDeleteExpense
                )
            }
        }

        // Interactive Monthly Category & Payment Method Summaries (shown in Day mode for quick access)
        if (viewMode == FinanceViewMode.DAY && finance.categoryTotals.isNotEmpty()) {
            item {
                InteractiveBreakdownCard(
                    title = "What You Spent For ($currentMonthLabel)",
                    rows = finance.categoryTotals.map { (cat, amt) -> cat.label to amt },
                    total = finance.monthSpent,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onRowClick = { idx ->
                        finance.categoryTotals.getOrNull(idx)?.first?.let { cat ->
                            activeDrillDown = ExpenseDrillDownFilter.ByCategory(
                                category = cat,
                                periodLabel = currentMonthLabel,
                                startDate = currentMonthStart,
                                endDate = "${currentMonthPrefix}-31"
                            )
                        }
                    }
                )
            }
        }

        if (viewMode == FinanceViewMode.DAY && finance.paymentMethodTotals.isNotEmpty()) {
            item {
                InteractiveBreakdownCard(
                    title = "Payment Methods ($currentMonthLabel)",
                    rows = finance.paymentMethodTotals.map { (pm, amt) -> pm.label to amt },
                    total = finance.monthSpent,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onRowClick = { idx ->
                        finance.paymentMethodTotals.getOrNull(idx)?.first?.let { method ->
                            activeDrillDown = ExpenseDrillDownFilter.ByPaymentMethod(
                                method = method,
                                periodLabel = currentMonthLabel,
                                startDate = currentMonthStart,
                                endDate = "${currentMonthPrefix}-31"
                            )
                        }
                    }
                )
            }
        }

        // Recent Expenses List (tap or Edit icon to edit)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
                if (finance.recentExpenses.isNotEmpty()) {
                    Text(
                        text = "Tap any item to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (finance.recentExpenses.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No expenses logged yet. Tap + Expense to start tracking your daily spending.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(finance.recentExpenses, key = { it.id }) { exp ->
                ExpenseItemRow(
                    expense = exp,
                    onEdit = { selected ->
                        editingExpense = selected
                        showEditorDialog = true
                    },
                    onDelete = onDeleteExpense
                )
            }
        }
    }

    if (showEditorDialog) {
        ExpenseEditorDialog(
            logicalToday = uiState.logicalToday,
            initialExpense = editingExpense,
            defaultDate = editorDefaultDate,
            onDismiss = {
                showEditorDialog = false
                editingExpense = null
            },
            onSaveNew = { logDate, amount, cat, title, method, note ->
                onAddExpense(logDate, amount, cat, title, method, note)
                selectedDayDate = logDate
                showEditorDialog = false
            },
            onUpdateExisting = { updated ->
                onUpdateExpense(updated)
                selectedDayDate = updated.logDate
                showEditorDialog = false
                editingExpense = null
            }
        )
    }

    activeDrillDown?.let { filter ->
        ExpenseDrillDownSheet(
            filter = filter,
            allExpenses = finance.allExpenses,
            onDismiss = { activeDrillDown = null },
            onAddForDate = { date ->
                editingExpense = null
                editorDefaultDate = date
                showEditorDialog = true
            },
            onEditExpense = { exp ->
                editingExpense = exp
                showEditorDialog = true
            },
            onDeleteExpense = onDeleteExpense
        )
    }
}

@Composable
private fun FinanceKpiCard(
    label: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier.clickable(onClick = onClick)
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
                style = MonoMetricLarge.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(
                        18f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                ),
                color = accent,
                maxLines = 1
            )
        }
    }
}
