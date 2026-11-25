package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import com.example.util.DateHelper
import java.util.Locale

sealed interface ExpenseDrillDownFilter {
    data class ByDate(val date: String) : ExpenseDrillDownFilter
    data class ByCategory(
        val category: ExpenseCategory,
        val periodLabel: String,
        val startDate: String? = null,
        val endDate: String? = null
    ) : ExpenseDrillDownFilter

    data class ByPaymentMethod(
        val method: PaymentMethod,
        val periodLabel: String,
        val startDate: String? = null,
        val endDate: String? = null
    ) : ExpenseDrillDownFilter
}

fun formatCurrency(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", amount)
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
}

@Composable
fun ExpenseItemRow(
    expense: ExpenseEntity,
    onEdit: (ExpenseEntity) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit(expense) }
            .testTag("expense_item_${expense.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${DateHelper.formatReadable(expense.logDate)} • ${expense.category.label} • ${expense.paymentMethod.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (expense.note.isNotBlank()) {
                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(expense.amount),
                    style = MonoMetricMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 2.dp)
                )
                IconButton(
                    onClick = { onEdit(expense) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_expense_button_${expense.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit expense",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onDelete(expense.id) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_expense_button_${expense.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete expense",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorDialog(
    logicalToday: String,
    initialExpense: ExpenseEntity? = null,
    defaultDate: String? = null,
    onDismiss: () -> Unit,
    onSaveNew: (String, Double, ExpenseCategory, String, PaymentMethod, String) -> Unit,
    onUpdateExisting: (ExpenseEntity) -> Unit
) {
    val isEditing = initialExpense != null
    val startingDate = initialExpense?.logDate ?: defaultDate ?: logicalToday

    var selectedDate by remember(initialExpense?.id, startingDate) { mutableStateOf(startingDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val yesterday = remember(logicalToday) { DateHelper.addDays(logicalToday, -1) }
    val twoDaysAgo = remember(logicalToday) { DateHelper.addDays(logicalToday, -2) }

    var amountText by remember(initialExpense?.id) {
        mutableStateOf(initialExpense?.let { formatCurrency(it.amount) } ?: "")
    }
    var titleText by remember(initialExpense?.id) {
        mutableStateOf(initialExpense?.title ?: "")
    }
    var selectedCategory by remember(initialExpense?.id) {
        mutableStateOf(initialExpense?.category ?: ExpenseCategory.FOOD)
    }
    var selectedMethod by remember(initialExpense?.id) {
        mutableStateOf(initialExpense?.paymentMethod ?: PaymentMethod.UPI)
    }
    var noteText by remember(initialExpense?.id) {
        mutableStateOf(initialExpense?.note ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isEditing) "Edit Expense" else "Log Expense",
                    style = MaterialTheme.typography.titleLarge
                )
                val dateTag = when (selectedDate) {
                    logicalToday -> "Today"
                    yesterday -> "Yesterday"
                    else -> "Previous Day"
                }
                Text(
                    text = "${DateHelper.formatReadable(selectedDate)} ($dateTag)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Expense Date", style = MaterialTheme.typography.labelMedium)
                    InfoTipButton(
                        title = "Expense Date & Backdating",
                        explanation = "Pick Today, Yesterday, 2 Days Ago, or tap Pick Date to choose any previous date. You can also move an existing expense to a different date anytime by editing it."
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedDate == logicalToday,
                        onClick = { selectedDate = logicalToday },
                        label = { Text("Today") },
                        modifier = Modifier.testTag("expense_date_today_chip")
                    )
                    FilterChip(
                        selected = selectedDate == yesterday,
                        onClick = { selectedDate = yesterday },
                        label = { Text("Yesterday") },
                        modifier = Modifier.testTag("expense_date_yesterday_chip")
                    )
                    FilterChip(
                        selected = selectedDate == twoDaysAgo,
                        onClick = { selectedDate = twoDaysAgo },
                        label = { Text("2 Days Ago") },
                        modifier = Modifier.testTag("expense_date_two_days_ago_chip")
                    )
                    val isCustomPastDate =
                        selectedDate != logicalToday && selectedDate != yesterday && selectedDate != twoDaysAgo
                    FilterChip(
                        selected = isCustomPastDate,
                        onClick = { showDatePicker = true },
                        label = {
                            Text(if (isCustomPastDate) DateHelper.formatReadable(selectedDate) else "Pick Date")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("expense_date_picker_chip")
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Spent *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input")
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("What did you spend for?") },
                    placeholder = { Text("e.g., Lunch, Metro, Coffee") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input")
                )
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        if (initialExpense != null) {
                            onUpdateExisting(
                                initialExpense.copy(
                                    logDate = selectedDate,
                                    amount = amt,
                                    category = selectedCategory,
                                    title = titleText,
                                    paymentMethod = selectedMethod,
                                    note = noteText
                                )
                            )
                        } else {
                            onSaveNew(
                                selectedDate,
                                amt,
                                selectedCategory,
                                titleText,
                                selectedMethod,
                                noteText
                            )
                        }
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val maxUtcMillis = remember(logicalToday) { DateHelper.toUtcMillis(logicalToday) }
        val maxYear = remember(logicalToday) { logicalToday.take(4).toIntOrNull() ?: 2100 }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateHelper.toUtcMillis(selectedDate),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= maxUtcMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year <= maxYear
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = DateHelper.fromUtcMillis(millis)
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("expense_date_confirm_button")
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDrillDownSheet(
    filter: ExpenseDrillDownFilter,
    allExpenses: List<ExpenseEntity>,
    onDismiss: () -> Unit,
    onAddForDate: (String) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filteredExpenses = remember(filter, allExpenses) {
        when (filter) {
            is ExpenseDrillDownFilter.ByDate -> allExpenses.filter { it.logDate == filter.date }
            is ExpenseDrillDownFilter.ByCategory -> allExpenses.filter { exp ->
                exp.category == filter.category &&
                    (filter.startDate == null || exp.logDate >= filter.startDate) &&
                    (filter.endDate == null || exp.logDate <= filter.endDate)
            }
            is ExpenseDrillDownFilter.ByPaymentMethod -> allExpenses.filter { exp ->
                exp.paymentMethod == filter.method &&
                    (filter.startDate == null || exp.logDate >= filter.startDate) &&
                    (filter.endDate == null || exp.logDate <= filter.endDate)
            }
        }
    }
    val (title, subtitle, addForDate) = remember(filter, filteredExpenses.size) {
        when (filter) {
            is ExpenseDrillDownFilter.ByDate -> Triple(
                DateHelper.formatReadable(filter.date),
                "${filteredExpenses.size} ${if (filteredExpenses.size == 1) "expense" else "expenses"} logged on this day",
                filter.date
            )
            is ExpenseDrillDownFilter.ByCategory -> Triple(
                filter.category.label,
                "${filter.periodLabel} • ${filteredExpenses.size} ${if (filteredExpenses.size == 1) "expense" else "expenses"}",
                null
            )
            is ExpenseDrillDownFilter.ByPaymentMethod -> Triple(
                filter.method.label,
                "${filter.periodLabel} • ${filteredExpenses.size} ${if (filteredExpenses.size == 1) "expense" else "expenses"}",
                null
            )
        }
    }
    val totalAmount = remember(filteredExpenses) { filteredExpenses.sumOf { it.amount } }
    val dayTotals = remember(filteredExpenses) {
        filteredExpenses
            .groupBy { it.logDate }
            .entries
            .sortedByDescending { it.key }
            .map { (date, list) -> Triple(date, list.sumOf { it.amount }, list.size) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(totalAmount),
                                style = MonoMetricLarge.copy(
                                    fontSize = androidx.compose.ui.unit.TextUnit(
                                        18f,
                                        androidx.compose.ui.unit.TextUnitType.Sp
                                    )
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (addForDate != null) {
                item {
                    Button(
                        onClick = { onAddForDate(addForDate) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Add Expense for ${DateHelper.formatReadable(addForDate)}")
                    }
                }
            }

            if (dayTotals.size > 1) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Day-by-Day Breakdown (${dayTotals.size} days)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            dayTotals.forEach { (date, daySum, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${DateHelper.formatReadable(date)} ($count ${if (count == 1) "item" else "items"})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatCurrency(daySum),
                                        style = MonoMetricMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Expenses (${filteredExpenses.size}) • Tap any to edit",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (filteredExpenses.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded for this selection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredExpenses, key = { it.id }) { exp ->
                    ExpenseItemRow(
                        expense = exp,
                        onEdit = onEditExpense,
                        onDelete = onDeleteExpense
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
