package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGuideSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Welcome to Life OS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Your offline-first personal life management and habit intelligence system. Everything is stored locally on your device with immutable historical logs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GuideSectionCard(
                icon = Icons.Default.Home,
                title = "1. Home (Today's Focus)",
                description = "Shows your daily completion ring, active streaks, upcoming reminders, and all habits scheduled for today. Tap the check/add icon for instant logging, or tap a card to enter custom values, notes, or intentionally skip."
            )
            GuideSectionCard(
                icon = Icons.Default.Checklist,
                title = "2. Habits & Categories",
                description = "Create and organize habits across 5 tracking types (Check-off, Numeric, Duration, Quantity, Rating 1–5) and 5 schedules (Every Day, Twice a Day for morning/evening routines, Specific Days, X Days/Week, or Every N Days). You can pause, archive, or edit habits anytime without losing past history."
            )
            GuideSectionCard(
                icon = Icons.Default.NotificationsActive,
                title = "3. Custom & Dual Reminders",
                description = "Enable reminders per habit and pick any exact custom time (24h clock). For twice-a-day routines, toggle 'Remind twice a day' to schedule two independent daily notifications."
            )
            GuideSectionCard(
                icon = Icons.Default.CalendarMonth,
                title = "4. Planner (Calendar Heatmap)",
                description = "Browse your monthly consistency heatmap. Tap any date to inspect that day's habits, log missed entries, or mark a habit as Intentionally Skipped with a reason so it doesn't count as missed."
            )
            GuideSectionCard(
                icon = Icons.Default.BarChart,
                title = "5. Insights (Weekly Analytics)",
                description = "Track your 7-day consistency bar chart, completion rate, state breakdown (Completed, Partial, Skipped, Missed), category-wise performance, and active streak leaderboard."
            )
            GuideSectionCard(
                icon = Icons.Default.Settings,
                title = "6. Settings & Data Control",
                description = "Switch between System, Light, and Dark themes, configure your Logical Day Boundary (e.g., 03:00 AM so late-night logs count for yesterday), manage notification permissions, and export all your data as JSON."
            )
            GuideSectionCard(
                icon = Icons.Default.Spa,
                title = "Historical Integrity",
                description = "Changing a habit's daily target today never rewrites yesterday's logs—each entry snapshots the target at the time it was recorded."
            )

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Got It")
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
