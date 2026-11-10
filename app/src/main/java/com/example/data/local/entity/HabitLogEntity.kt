package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.LogStatus
import com.example.data.model.TrackingType

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("logDate"), Index(value = ["habitId", "logDate"])]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val logDate: String, // yyyy-MM-dd (logical day)
    val value: Double,
    val targetValueSnapshot: Double,
    val trackingTypeSnapshot: TrackingType,
    val status: LogStatus,
    val note: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
