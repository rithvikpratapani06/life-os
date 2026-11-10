package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMethod

@Entity(
    tableName = "expenses",
    indices = [Index("logDate")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val logDate: String, // yyyy-MM-dd
    val amount: Double,
    val category: ExpenseCategory,
    val title: String, // What I spent for
    val paymentMethod: PaymentMethod,
    val note: String = "",
    val loggedAt: Long = System.currentTimeMillis()
)
