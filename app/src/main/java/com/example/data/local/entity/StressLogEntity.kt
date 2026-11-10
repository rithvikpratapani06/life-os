package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.StressLevel

@Entity(
    tableName = "stress_logs",
    indices = [Index("logDate", unique = true)]
)
data class StressLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val logDate: String, // yyyy-MM-dd
    val level: StressLevel,
    val trigger: String = "",
    val note: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
