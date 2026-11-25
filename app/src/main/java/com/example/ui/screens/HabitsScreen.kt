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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.HabitEntity
import com.example.data.model.HabitStatus
import com.example.ui.LifeOsUiState
import com.example.ui.components.formatCleanNumber
import com.example.ui.theme.habitIconAndColor

@Composable
fun HabitsScreen(
    uiState: LifeOsUiState,
    onCreateHabit: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onChangeStatus: (HabitEntity, HabitStatus) -> Unit,
    onDeleteHabit: (Long) -> Unit,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf(HabitStatus.ACTIVE) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val catMap = remember(uiState.categories) { uiState.categories.associateBy { it.id } }

    val filteredHabits = remember(uiState.allHabits, selectedStatus, selectedCategoryId, searchQuery) {
        uiState.allHabits.filter { habit ->
            habit.status == selectedStatus &&
                (selectedCategoryId == null || habit.categoryId == selectedCategoryId) &&
                (searchQuery.isBlank() || habit.name.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Habits", style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onManageCategories) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Categories")
                    }
                    OutlinedButton(
                        onClick = onCreateHabit,
                        modifier = Modifier.testTag("habits_new_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search habits...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status.label) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All Categories") }
                )
                uiState.categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = {
                            selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id
                        },
                        label = { Text(cat.name) }
                    )
                }
            }
        }

        if (filteredHabits.isEmpty()) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No ${selectedStatus.label.lowercase()} habits",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap + New to create a habit with custom schedules and tracking types.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredHabits, key = { it.id }) { habit ->
                val cat = catMap[habit.categoryId]
                val (habitIcon, catColor) = habitIconAndColor(habit, cat)
                var menuExpanded by remember { mutableStateOf(false) }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = catColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, catColor.copy(alpha = 0.35f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = habitIcon,
                                    contentDescription = habit.name,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    append(cat?.name ?: "General")
                                    append(" • ")
                                    append(habit.trackingType.label)
                                    if (habit.trackingType.name != "BOOLEAN") {
                                        append(" (${formatCleanNumber(habit.targetValue)} ${habit.unit})")
                                    }
                                    append(" • ")
                                    append(habit.scheduleType.label)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Habit options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onEditHabit(habit)
                                    }
                                )
                                if (habit.status != HabitStatus.ACTIVE) {
                                    DropdownMenuItem(
                                        text = { Text("Resume") },
                                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            onChangeStatus(habit, HabitStatus.ACTIVE)
                                        }
                                    )
                                }
                                if (habit.status == HabitStatus.ACTIVE) {
                                    DropdownMenuItem(
                                        text = { Text("Pause") },
                                        leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            onChangeStatus(habit, HabitStatus.PAUSED)
                                        }
                                    )
                                }
                                if (habit.status != HabitStatus.ARCHIVED) {
                                    DropdownMenuItem(
                                        text = { Text("Archive") },
                                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            onChangeStatus(habit, HabitStatus.ARCHIVED)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteHabit(habit.id)
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
