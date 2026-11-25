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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
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
                text = "Welcome to Habtrac",
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
                title = "1. Home & Quick Logging",
                description = "Shows your daily completion ring, active streaks, upcoming reminders, Daily Stress Check-In, and today's habits. Tap Yes/No for check-offs, AM/PM chips for Twice-a-Day habits, or tap any card for custom values, notes, or skipping."
            )
            GuideSectionCard(
                icon = Icons.Default.Checklist,
                title = "2. Habits, Custom Icons & Schedules",
                description = "Pick from 30+ icons across Health, Fitness, Learning, and Productivity—each automatically selects an exact matching color. Track 5 types across 6 schedules including Twice a Day (independent Morning AM & Night PM tracking)."
            )
            GuideSectionCard(
                icon = Icons.Default.EventNote,
                title = "3. Occasional Activities (Non-Habit Events)",
                description = "Things you do only sometimes—like playing Cricket, Badminton, eating Biryani, or having Alcohol—belong in Occasional Activities on Home rather than daily Habits. Log them in 1 tap without streaks, 'Missed' penalties, or lowering your daily habit score, and view weekly/monthly counts in Insights → Activities."
            )
            GuideSectionCard(
                icon = Icons.Default.Psychology,
                title = "4. Daily Stress Tracking & Alerts",
                description = "Log your daily stress level (Low, Moderate, High, Very High) right from Home. Enable the Daily Stress Check-In reminder in Settings and view weekly/monthly stress trends in Insights."
            )
            GuideSectionCard(
                icon = Icons.Default.AccountBalanceWallet,
                title = "5. Finance Manager & Expense Tracker",
                description = "Record expenses for today or any missed previous day, and tap any saved expense or Edit icon anytime to update its amount, date, category, payment method, or notes. Switch between Day Wise, Week Wise, and Month Wise views—and tap any day, week, month, category, or payment method to inspect every expense and total spent all day."
            )
            GuideSectionCard(
                icon = Icons.Default.NotificationsActive,
                title = "6. Custom & Dual Reminders",
                description = "Set any custom reminder time per habit. For morning/night routines, enable 'Remind twice a day' to receive two independent daily notifications."
            )
            GuideSectionCard(
                icon = Icons.Default.CalendarMonth,
                title = "7. Planner (Monthly Calendar)",
                description = "Browse your monthly consistency calendar. Tap any date to inspect that day's habits, log missed entries, mark Intentionally Skipped, or log occasional activities for that day."
            )
            GuideSectionCard(
                icon = Icons.Default.BarChart,
                title = "8. Insights, Per-Habit Calendar & Heatmaps",
                description = "Switch between Overall, Per Habit, Activities, and Stress. Select any individual habit in Per Habit to view its interactive Sun–Sat Date Calendar, Daily Log snapshot, All-Time Statistics (Streaks, Completions, Surplus, Debt, Net Balance), 7D/30D/All Performance, Insight Cluster, and 12-Week Heatmap."
            )
            GuideSectionCard(
                icon = Icons.Default.Palette,
                title = "9. Personalization & Settings",
                description = "Customize your greeting name, choose from 6 accent color palettes, switch Light/Dark/System themes, enable Compact Layout, and set your Logical Day Boundary (e.g., 03:00 AM so late-night logs count for yesterday)."
            )
            GuideSectionCard(
                icon = Icons.Default.Security,
                title = "10. Security & Offline Privacy",
                description = "Habtrac runs in an offline sandbox with zero internet or storage permissions. Enable a 4-digit salted SHA-256 App PIN lock and Screen Privacy (blocks screenshots and recent-apps preview) in Settings, or export your data as JSON anytime."
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
