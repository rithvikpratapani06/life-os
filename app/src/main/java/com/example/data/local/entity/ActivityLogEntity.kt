package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ActivityKind

@Entity(
    tableName = "activity_logs",
    indices = [Index("logDate")]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val logDate: String, // yyyy-MM-dd
    val name: String,
    val kind: ActivityKind,
    val detail: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
