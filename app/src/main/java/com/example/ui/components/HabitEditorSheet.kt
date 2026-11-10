package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.model.HabitStatus
import com.example.data.model.ScheduleType
import com.example.data.model.TrackingType
import com.example.ui.theme.CategoryColorOptions
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitEditorSheet(
    existingHabit: HabitEntity?,
    categories: List<CategoryEntity>,
    logicalToday: String,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(existingHabit?.name ?: "") }
    var description by remember { mutableStateOf(existingHabit?.description ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(existingHabit?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var trackingType by remember { mutableStateOf(existingHabit?.trackingType ?: TrackingType.BOOLEAN) }
    var targetText by remember {
        mutableStateOf(
            existingHabit?.let { formatCleanNumber(it.targetValue) } ?: "1"
        )
    }
    var unit by remember { mutableStateOf(existingHabit?.unit ?: TrackingType.BOOLEAN.defaultUnit) }
    var scheduleType by remember { mutableStateOf(existingHabit?.scheduleType ?: ScheduleType.DAILY) }
    var daysMask by remember { mutableIntStateOf(existingHabit?.scheduleDaysMask ?: 127) }
    var weeklyTarget by remember { mutableIntStateOf(existingHabit?.weeklyTargetCount ?: 3) }
    var intervalDays by remember { mutableIntStateOf(existingHabit?.intervalDays ?: 2) }
    var reminderEnabled by remember { mutableStateOf(existingHabit?.reminderEnabled ?: false) }
    var reminderHour by remember { mutableIntStateOf(existingHabit?.reminderHour ?: 9) }
    var reminderMinute by remember { mutableIntStateOf(existingHabit?.reminderMinute ?: 0) }
    var secondReminderEnabled by remember { mutableStateOf(existingHabit?.secondReminderEnabled ?: false) }
    var secondReminderHour by remember { mutableIntStateOf(existingHabit?.secondReminderHour ?: 20) }
    var secondReminderMinute by remember { mutableIntStateOf(existingHabit?.secondReminderMinute ?: 0) }
    var editingReminderSlot by remember { mutableStateOf<Int?>(null) }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existingHabit == null) "New Habit" else "Edit Habit",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Habit Name *") },
                placeholder = { Text("e.g., Apply Skincare / Read 20 pages") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Please enter a habit name") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val catColor = Color(cat.colorHex)
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIconFor(cat.iconKey),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tracking Type", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TrackingType.entries.forEach { type ->
                        FilterChip(
                            selected = trackingType == type,
                            onClick = {
                                trackingType = type
                                if (existingHabit == null || existingHabit.trackingType != type) {
                                    unit = if (type == TrackingType.BOOLEAN && scheduleType == ScheduleType.TWICE_DAILY) {
                                        "times"
                                    } else {
                                        type.defaultUnit
                                    }
                                    targetText = when {
                                        type == TrackingType.BOOLEAN && scheduleType == ScheduleType.TWICE_DAILY -> "2"
                                        type == TrackingType.BOOLEAN -> "1"
                                        type == TrackingType.NUMERIC -> "20"
                                        type == TrackingType.DURATION -> "30"
                                        type == TrackingType.QUANTITY -> "8"
                                        else -> "4"
                                    }
                                }
                            },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            if (trackingType != TrackingType.BOOLEAN || scheduleType == ScheduleType.TWICE_DAILY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text("Daily Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("habit_target_input")
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Schedule", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScheduleType.entries.forEach { sched ->
                        FilterChip(
                            selected = scheduleType == sched,
                            onClick = {
                                scheduleType = sched
                                if (sched == ScheduleType.TWICE_DAILY) {
                                    if (trackingType == TrackingType.BOOLEAN) {
                                        targetText = "2"
                                        unit = "times"
                                    }
                                    if (reminderEnabled) {
                                        secondReminderEnabled = true
                                    }
                                } else if (trackingType == TrackingType.BOOLEAN && targetText == "2") {
                                    targetText = "1"
                                    unit = TrackingType.BOOLEAN.defaultUnit
                                }
                            },
                            label = { Text(sched.label) }
                        )
                    }
                }

                when (scheduleType) {
                    ScheduleType.TWICE_DAILY -> {
                        Text(
                            text = "Designed for morning & evening routines (2 check-offs per day).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    ScheduleType.SPECIFIC_DAYS -> {
                        val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dayNames.forEachIndexed { index, label ->
                                val selected = (daysMask and (1 shl index)) != 0
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            daysMask = daysMask xor (1 shl index)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ScheduleType.WEEKLY_TARGET -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Target days per week:", style = MaterialTheme.typography.bodyMedium)
                            (1..6).forEach { count ->
                                FilterChip(
                                    selected = weeklyTarget == count,
                                    onClick = { weeklyTarget = count },
                                    label = { Text("${count}x") }
                                )
                            }
                        }
                    }
                    ScheduleType.INTERVAL -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Repeat every", style = MaterialTheme.typography.bodyMedium)
                            listOf(2, 3, 4, 7).forEach { days ->
                                FilterChip(
                                    selected = intervalDays == days,
                                    onClick = { intervalDays = days },
                                    label = { Text("${days}d") }
                                )
                            }
                        }
                    }
                    ScheduleType.DAILY -> Unit
                }
            }

            // Custom Reminders (1x or 2x daily)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Daily Reminders", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = when {
                                    !reminderEnabled -> "Off"
                                    secondReminderEnabled -> String.format(
                                        Locale.US,
                                        "Twice daily at %02d:%02d & %02d:%02d",
                                        reminderHour, reminderMinute, secondReminderHour, secondReminderMinute
                                    )
                                    else -> String.format(Locale.US, "Once daily at %02d:%02d", reminderHour, reminderMinute)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = {
                                reminderEnabled = it
                                if (it && scheduleType == ScheduleType.TWICE_DAILY) {
                                    secondReminderEnabled = true
                                }
                            }
                        )
                    }

                    if (reminderEnabled) {
                        OutlinedButton(
                            onClick = { editingReminderSlot = 1 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                String.format(
                                    Locale.US,
                                    "1st Reminder Time: %02d:%02d (Tap to change)",
                                    reminderHour,
                                    reminderMinute
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Remind twice a day", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Add a 2nd custom reminder time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = secondReminderEnabled,
                                onCheckedChange = { secondReminderEnabled = it }
                            )
                        }

                        if (secondReminderEnabled) {
                            OutlinedButton(
                                onClick = { editingReminderSlot = 2 },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    String.format(
                                        Locale.US,
                                        "2nd Reminder Time: %02d:%02d (Tap to change)",
                                        secondReminderHour,
                                        secondReminderMinute
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val parsedTarget = (targetText.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)
                    val finalMask = if (scheduleType == ScheduleType.SPECIFIC_DAYS && daysMask == 0) 127 else daysMask
                    val entity = HabitEntity(
                        id = existingHabit?.id ?: 0L,
                        name = name.trim(),
                        description = description.trim(),
                        categoryId = selectedCategoryId,
                        trackingType = trackingType,
                        targetValue = if (trackingType == TrackingType.BOOLEAN && scheduleType != ScheduleType.TWICE_DAILY) 1.0 else parsedTarget,
                        unit = unit.ifBlank { trackingType.defaultUnit },
                        scheduleType = scheduleType,
                        scheduleDaysMask = finalMask,
                        weeklyTargetCount = weeklyTarget,
                        intervalDays = intervalDays,
                        reminderEnabled = reminderEnabled,
                        reminderHour = reminderHour,
                        reminderMinute = reminderMinute,
                        secondReminderEnabled = reminderEnabled && secondReminderEnabled,
                        secondReminderHour = secondReminderHour,
                        secondReminderMinute = secondReminderMinute,
                        startDate = existingHabit?.startDate ?: logicalToday,
                        endDate = existingHabit?.endDate,
                        status = existingHabit?.status ?: HabitStatus.ACTIVE,
                        createdAt = existingHabit?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(entity)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_habit_button")
            ) {
                Text(if (existingHabit == null) "Create Habit" else "Save Changes")
            }
        }
    }

    editingReminderSlot?.let { slot ->
        val initHour = if (slot == 1) reminderHour else secondReminderHour
        val initMinute = if (slot == 1) reminderMinute else secondReminderMinute
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { editingReminderSlot = null },
            title = { Text(if (slot == 1) "Set 1st Reminder Time" else "Set 2nd Reminder Time") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (slot == 1) {
                            reminderHour = timePickerState.hour
                            reminderMinute = timePickerState.minute
                        } else {
                            secondReminderHour = timePickerState.hour
                            secondReminderMinute = timePickerState.minute
                        }
                        editingReminderSlot = null
                    }
                ) {
                    Text("Set Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReminderSlot = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ManageCategoriesDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (String, Long, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(CategoryColorOptions.first()) }
    val iconKeys = listOf("health", "fitness", "learning", "productivity", "care", "mindfulness")
    var selectedIcon by remember { mutableStateOf(iconKeys.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Categories") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryColorOptions.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconKeys.forEach { key ->
                        FilterChip(
                            selected = selectedIcon == key,
                            onClick = { selectedIcon = key },
                            label = {
                                Icon(
                                    imageVector = categoryIconFor(key),
                                    contentDescription = key,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddCategory(newName, selectedColor, selectedIcon)
                            newName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Category")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Existing Categories", style = MaterialTheme.typography.titleSmall)
                categories.forEach { cat ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIconFor(cat.iconKey),
                                    contentDescription = null,
                                    tint = Color(cat.colorHex),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!cat.isDefault) {
                                IconButton(onClick = { onDeleteCategory(cat) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete category",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
