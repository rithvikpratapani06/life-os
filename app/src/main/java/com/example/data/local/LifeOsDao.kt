package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeOsDao {
    // Categories
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM habits WHERE categoryId = :categoryId")
    suspend fun countHabitsInCategory(categoryId: Long): Int

    // Habits
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabitsOnce(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    // Habit Logs
    @Query("SELECT * FROM habit_logs ORDER BY logDate DESC, loggedAt DESC")
    fun observeAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE logDate BETWEEN :startDate AND :endDate ORDER BY loggedAt ASC")
    fun observeLogsInRange(startDate: String, endDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND logDate = :logDate ORDER BY loggedAt DESC")
    suspend fun getLogsForHabitOnDate(habitId: Long, logDate: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs ORDER BY logDate DESC, loggedAt DESC")
    suspend fun getAllLogsOnce(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND logDate = :logDate")
    suspend fun deleteLogsForHabitOnDate(habitId: Long, logDate: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND logDate = :logDate AND slot = :slot")
    suspend fun deleteLogsForHabitOnDateAndSlot(habitId: Long, logDate: String, slot: String)

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    // Stress Logs
    @Query("SELECT * FROM stress_logs ORDER BY logDate DESC")
    fun observeAllStressLogs(): Flow<List<com.example.data.local.entity.StressLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStressLog(log: com.example.data.local.entity.StressLogEntity): Long

    @Query("DELETE FROM stress_logs")
    suspend fun deleteAllStressLogs()

    // Expenses (Finance Manager)
    @Query("SELECT * FROM expenses ORDER BY logDate DESC, loggedAt DESC")
    fun observeAllExpenses(): Flow<List<com.example.data.local.entity.ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: com.example.data.local.entity.ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
