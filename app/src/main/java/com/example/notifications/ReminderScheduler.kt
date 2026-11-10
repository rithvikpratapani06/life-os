package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.entity.HabitEntity
import com.example.data.model.HabitStatus
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "life_os_habit_reminders"
    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"
    const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
    const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
    const val EXTRA_SLOT = "extra_slot"
    const val STRESS_CHECKIN_ID = -999L

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit & Wellness Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for your Life OS habits and stress check-ins"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun syncStressReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        if (!enabled) {
            cancelSlot(context, STRESS_CHECKIN_ID, 1)
            return
        }
        ensureChannel(context)
        scheduleNext(context, STRESS_CHECKIN_ID, 1, "Daily Stress Check-In", hour, minute)
    }

    fun syncReminder(context: Context, habit: HabitEntity) {
        if (!habit.reminderEnabled || habit.status != HabitStatus.ACTIVE) {
            cancelReminder(context, habit.id)
            return
        }
        ensureChannel(context)
        scheduleNext(context, habit.id, 1, habit.name, habit.reminderHour, habit.reminderMinute)
        if (habit.secondReminderEnabled) {
            scheduleNext(context, habit.id, 2, habit.name, habit.secondReminderHour, habit.secondReminderMinute)
        } else {
            cancelSlot(context, habit.id, 2)
        }
    }

    fun scheduleNext(
        context: Context,
        habitId: Long,
        slot: Int,
        habitName: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_SLOT, slot)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_REMINDER_HOUR, hour)
            putExtra(EXTRA_REMINDER_MINUTE, minute)
        }
        val requestCode = if (habitId == STRESS_CHECKIN_ID) 999001 else (habitId.toInt() * 10) + slot
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, habitId: Long) {
        cancelSlot(context, habitId, 1)
        cancelSlot(context, habitId, 2)
    }

    private fun cancelSlot(context: Context, habitId: Long, slot: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val requestCode = if (habitId == STRESS_CHECKIN_ID) 999001 else (habitId.toInt() * 10) + slot
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
