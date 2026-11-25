package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.HabitStatus
import com.example.data.model.ScheduleType
import com.example.data.model.TrackingType

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId"), Index("status")]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val categoryId: Long,
    val trackingType: TrackingType = TrackingType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String = "done",
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val scheduleDaysMask: Int = 127, // 7-bit mask: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16, Sat=32, Sun=64
    val weeklyTargetCount: Int = 3,
    val intervalDays: Int = 2,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val secondReminderEnabled: Boolean = false,
    val secondReminderHour: Int = 20,
    val secondReminderMinute: Int = 0,
    val startDate: String, // yyyy-MM-dd
    val endDate: String? = null,
    val status: HabitStatus = HabitStatus.ACTIVE,
    val iconKey: String = "",
    val colorHex: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
