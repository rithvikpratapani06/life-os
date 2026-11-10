package com.example.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(ReminderScheduler.EXTRA_HABIT_ID, -1L)
        val slot = intent.getIntExtra(ReminderScheduler.EXTRA_SLOT, 1)
        val habitName = intent.getStringExtra(ReminderScheduler.EXTRA_HABIT_NAME) ?: "Habit"
        val hour = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_HOUR, 9)
        val minute = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_MINUTE, 0)

        ReminderScheduler.ensureChannel(context)

        val canPost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val notifId = if (habitId == ReminderScheduler.STRESS_CHECKIN_ID) 999001 else (habitId.toInt() * 10) + slot

        if (canPost && habitId != -1L) {
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                notifId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val isStress = habitId == ReminderScheduler.STRESS_CHECKIN_ID
            val title = if (isStress) "Daily Stress Check-In" else "Habtrac Reminder"
            val body = if (isStress) {
                "How was your stress level today? Tap to log Low, Moderate, High, or Very High."
            } else {
                "Time for: $habitName"
            }

            val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(notifId, notification)
        }

        if (habitId != -1L) {
            ReminderScheduler.scheduleNext(context, habitId, slot, habitName, hour, minute)
        }
    }
}
