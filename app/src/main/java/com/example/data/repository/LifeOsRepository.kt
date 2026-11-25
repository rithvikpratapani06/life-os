package com.example.data.repository

import com.example.data.local.LifeOsDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.model.HabitStatus
import com.example.data.model.LogStatus
import com.example.data.model.ScheduleType
import com.example.data.model.TrackingType
import com.example.util.DateHelper
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

data class DayHabitSummary(
    val habit: HabitEntity,
    val category: CategoryEntity?,
    val status: LogStatus,
    val totalValue: Double,
    val targetValue: Double,
    val logs: List<HabitLogEntity>,
    val currentStreak: Int,
    val morningStatus: LogStatus? = null,
    val eveningStatus: LogStatus? = null
)

class LifeOsRepository(private val dao: LifeOsDao) {
    val categories: Flow<List<CategoryEntity>> = dao.observeCategories()
    val habits: Flow<List<HabitEntity>> = dao.observeAllHabits()
    val logs: Flow<List<HabitLogEntity>> = dao.observeAllLogs()
    val stressLogs: Flow<List<com.example.data.local.entity.StressLogEntity>> = dao.observeAllStressLogs()
    val expenses: Flow<List<com.example.data.local.entity.ExpenseEntity>> = dao.observeAllExpenses()
    val activityLogs: Flow<List<com.example.data.local.entity.ActivityLogEntity>> = dao.observeAllActivityLogs()

    suspend fun addCategory(name: String, colorHex: Long, iconKey: String): Long {
        return dao.insertCategory(
            CategoryEntity(name = name.trim(), colorHex = colorHex, iconKey = iconKey, isDefault = false)
        )
    }

    suspend fun updateCategory(category: CategoryEntity) {
        dao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity): Boolean {
        val count = dao.countHabitsInCategory(category.id)
        if (count > 0) return false
        dao.deleteCategory(category)
        return true
    }

    suspend fun saveHabit(habit: HabitEntity): Long {
        return if (habit.id == 0L) {
            dao.insertHabit(habit.copy(createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
        } else {
            dao.updateHabit(habit.copy(updatedAt = System.currentTimeMillis()))
            habit.id
        }
    }

    suspend fun updateHabitStatus(habit: HabitEntity, status: HabitStatus) {
        dao.updateHabit(habit.copy(status = status, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteHabit(habitId: Long) {
        dao.deleteHabitById(habitId)
    }

    /**
     * Records a new timestamped log entry without overwriting prior days.
     * For BOOLEAN habits, toggling off removes today's log if already completed.
     */
    suspend fun logHabitProgress(
        habit: HabitEntity,
        logDate: String,
        value: Double,
        note: String = "",
        isSkip: Boolean = false,
        isExplicitNo: Boolean = false,
        slot: String = ""
    ) {
        if (habit.scheduleType == ScheduleType.TWICE_DAILY && slot.isNotEmpty()) {
            dao.deleteLogsForHabitOnDateAndSlot(habit.id, logDate, slot)
            val slotStatus = when {
                isSkip -> LogStatus.INTENTIONALLY_SKIPPED
                isExplicitNo || value <= 0.0 -> LogStatus.MISSED
                else -> LogStatus.COMPLETED
            }
            dao.insertLog(
                HabitLogEntity(
                    habitId = habit.id,
                    logDate = logDate,
                    value = if (slotStatus == LogStatus.COMPLETED) 1.0 else 0.0,
                    targetValueSnapshot = habit.targetValue,
                    trackingTypeSnapshot = habit.trackingType,
                    status = slotStatus,
                    note = note.trim(),
                    slot = slot
                )
            )
            return
        }

        val existing = dao.getLogsForHabitOnDate(habit.id, logDate)
        if (isSkip) {
            dao.deleteLogsForHabitOnDate(habit.id, logDate)
            dao.insertLog(
                HabitLogEntity(
                    habitId = habit.id,
                    logDate = logDate,
                    value = 0.0,
                    targetValueSnapshot = habit.targetValue,
                    trackingTypeSnapshot = habit.trackingType,
                    status = LogStatus.INTENTIONALLY_SKIPPED,
                    note = note.trim()
                )
            )
            return
        }

        if (isExplicitNo) {
            dao.deleteLogsForHabitOnDate(habit.id, logDate)
            dao.insertLog(
                HabitLogEntity(
                    habitId = habit.id,
                    logDate = logDate,
                    value = 0.0,
                    targetValueSnapshot = habit.targetValue,
                    trackingTypeSnapshot = habit.trackingType,
                    status = LogStatus.MISSED,
                    note = note.trim()
                )
            )
            return
        }

        if (habit.trackingType == TrackingType.BOOLEAN && habit.targetValue <= 1.0) {
            val alreadyCompleted = existing.any { it.status == LogStatus.COMPLETED }
            dao.deleteLogsForHabitOnDate(habit.id, logDate)
            if (!alreadyCompleted && value > 0.0) {
                dao.insertLog(
                    HabitLogEntity(
                        habitId = habit.id,
                        logDate = logDate,
                        value = 1.0,
                        targetValueSnapshot = 1.0,
                        trackingTypeSnapshot = TrackingType.BOOLEAN,
                        status = LogStatus.COMPLETED,
                        note = note.trim()
                    )
                )
            }
            return
        }

        // For numeric, duration, quantity, rating: append a historical timestamped record
        // Remove any prior skip or explicit-missed marker for that day if user now logs actual progress
        val hasSkipOrMissed = existing.any {
            it.status == LogStatus.INTENTIONALLY_SKIPPED || it.status == LogStatus.MISSED
        }
        if (hasSkipOrMissed) {
            dao.deleteLogsForHabitOnDate(habit.id, logDate)
        }

        val currentSum = if (habit.trackingType == TrackingType.RATING) {
            value
        } else {
            existing.filter { it.status != LogStatus.INTENTIONALLY_SKIPPED }.sumOf { it.value } + value
        }

        val target = habit.targetValue.coerceAtLeast(1.0)
        val computedStatus = if (currentSum >= target) {
            LogStatus.COMPLETED
        } else if (currentSum > 0.0) {
            LogStatus.PARTIALLY_COMPLETED
        } else {
            LogStatus.SCHEDULED
        }

        dao.insertLog(
            HabitLogEntity(
                habitId = habit.id,
                logDate = logDate,
                value = value,
                targetValueSnapshot = target,
                trackingTypeSnapshot = habit.trackingType,
                status = computedStatus,
                note = note.trim()
            )
        )
    }

    suspend fun clearDayLogs(habitId: Long, logDate: String) {
        dao.deleteLogsForHabitOnDate(habitId, logDate)
    }

    suspend fun clearAllUserData() {
        dao.deleteAllLogs()
        dao.deleteAllHabits()
        dao.deleteAllStressLogs()
        dao.deleteAllExpenses()
        dao.deleteAllActivityLogs()
    }

    suspend fun logStress(
        logDate: String,
        level: com.example.data.model.StressLevel,
        trigger: String,
        note: String
    ) {
        dao.upsertStressLog(
            com.example.data.local.entity.StressLogEntity(
                logDate = logDate,
                level = level,
                trigger = trigger.trim().take(60),
                note = note.trim().take(200)
            )
        )
    }

    suspend fun addExpense(
        logDate: String,
        amount: Double,
        category: com.example.data.model.ExpenseCategory,
        title: String,
        paymentMethod: com.example.data.model.PaymentMethod,
        note: String
    ) {
        if (amount <= 0.0) return
        dao.insertExpense(
            com.example.data.local.entity.ExpenseEntity(
                logDate = logDate,
                amount = amount,
                category = category,
                title = title.trim().ifBlank { category.label }.take(80),
                paymentMethod = paymentMethod,
                note = note.trim().take(200)
            )
        )
    }

    suspend fun updateExpense(expense: com.example.data.local.entity.ExpenseEntity) {
        if (expense.amount <= 0.0) return
        dao.updateExpense(
            expense.copy(
                title = expense.title.trim().ifBlank { expense.category.label }.take(80),
                note = expense.note.trim().take(200)
            )
        )
    }

    suspend fun deleteExpense(expenseId: Long) {
        dao.deleteExpenseById(expenseId)
    }

    suspend fun logActivity(
        logDate: String,
        name: String,
        kind: com.example.data.model.ActivityKind,
        detail: String
    ) {
        val cleanName = name.trim().take(60)
        if (cleanName.isBlank()) return
        dao.insertActivityLog(
            com.example.data.local.entity.ActivityLogEntity(
                logDate = logDate,
                name = cleanName,
                kind = kind,
                detail = detail.trim().take(120)
            )
        )
    }

    suspend fun deleteActivity(id: Long) {
        dao.deleteActivityLogById(id)
    }

    suspend fun exportAllDataJson(): String {
        val cats = dao.getAllCategoriesOnce()
        val hbs = dao.getAllHabitsOnce()
        val lgs = dao.getAllLogsOnce()

        val root = JSONObject()
        root.put("exportedAt", System.currentTimeMillis())

        val catArray = JSONArray()
        cats.forEach { c ->
            catArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("colorHex", c.colorHex)
                put("iconKey", c.iconKey)
            })
        }
        root.put("categories", catArray)

        val habitArray = JSONArray()
        hbs.forEach { h ->
            habitArray.put(JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("description", h.description)
                put("categoryId", h.categoryId)
                put("trackingType", h.trackingType.name)
                put("targetValue", h.targetValue)
                put("unit", h.unit)
                put("scheduleType", h.scheduleType.name)
                put("status", h.status.name)
                put("startDate", h.startDate)
                put("endDate", h.endDate ?: JSONObject.NULL)
                put("iconKey", h.iconKey)
                put("colorHex", h.colorHex)
            })
        }
        root.put("habits", habitArray)

        val logArray = JSONArray()
        lgs.forEach { l ->
            logArray.put(JSONObject().apply {
                put("id", l.id)
                put("habitId", l.habitId)
                put("logDate", l.logDate)
                put("value", l.value)
                put("targetValueSnapshot", l.targetValueSnapshot)
                put("trackingTypeSnapshot", l.trackingTypeSnapshot.name)
                put("status", l.status.name)
                put("note", l.note)
                put("loggedAt", l.loggedAt)
            })
        }
        root.put("habitLogs", logArray)

        val actArray = JSONArray()
        dao.getAllActivityLogsOnce().forEach { a ->
            actArray.put(JSONObject().apply {
                put("id", a.id)
                put("logDate", a.logDate)
                put("name", a.name)
                put("kind", a.kind.name)
                put("detail", a.detail)
                put("loggedAt", a.loggedAt)
            })
        }
        root.put("activityLogs", actArray)

        return root.toString(2)
    }

    companion object {
        fun isHabitScheduledOn(habit: HabitEntity, dateStr: String): Boolean {
            if (habit.status == HabitStatus.ARCHIVED) return false
            if (dateStr < habit.startDate) return false
            if (habit.endDate != null && dateStr > habit.endDate) return false

            return when (habit.scheduleType) {
                ScheduleType.DAILY, ScheduleType.TWICE_DAILY -> true
                ScheduleType.SPECIFIC_DAYS -> DateHelper.isDayInMask(dateStr, habit.scheduleDaysMask)
                ScheduleType.WEEKLY_TARGET -> true // Eligible any day of the week until weekly target is met
                ScheduleType.INTERVAL -> {
                    val diff = DateHelper.daysBetween(habit.startDate, dateStr)
                    val interval = habit.intervalDays.coerceAtLeast(1)
                    diff >= 0 && diff % interval == 0
                }
                ScheduleType.OCCASIONAL -> false
            }
        }

        fun evaluateDaySummary(
            habit: HabitEntity,
            category: CategoryEntity?,
            logsForDay: List<HabitLogEntity>,
            allLogsForHabit: List<HabitLogEntity>,
            targetDate: String,
            logicalToday: String,
            precomputedStreak: Int? = null
        ): DayHabitSummary {
            val activeLogs = logsForDay.sortedBy { it.loggedAt }
            if (habit.scheduleType == ScheduleType.TWICE_DAILY) {
                val amLog = activeLogs.lastOrNull { it.slot == "MORNING" }
                val pmLog = activeLogs.lastOrNull { it.slot == "EVENING" }
                val amStatus = amLog?.status ?: if (targetDate < logicalToday && habit.status == HabitStatus.ACTIVE) LogStatus.MISSED else LogStatus.SCHEDULED
                val pmStatus = pmLog?.status ?: if (targetDate < logicalToday && habit.status == HabitStatus.ACTIVE) LogStatus.MISSED else LogStatus.SCHEDULED

                val completedSlots = listOf(amStatus, pmStatus).count { it == LogStatus.COMPLETED }
                val skippedSlots = listOf(amStatus, pmStatus).count { it == LogStatus.INTENTIONALLY_SKIPPED }
                val missedSlots = listOf(amStatus, pmStatus).count { it == LogStatus.MISSED }

                val dayStatus = when {
                    completedSlots == 2 -> LogStatus.COMPLETED
                    completedSlots == 1 && skippedSlots == 1 -> LogStatus.COMPLETED
                    skippedSlots == 2 -> LogStatus.INTENTIONALLY_SKIPPED
                    completedSlots == 1 -> LogStatus.PARTIALLY_COMPLETED
                    missedSlots > 0 && targetDate < logicalToday -> LogStatus.MISSED
                    amLog?.status == LogStatus.MISSED && pmLog?.status == LogStatus.MISSED -> LogStatus.MISSED
                    else -> LogStatus.SCHEDULED
                }

                val streak = precomputedStreak ?: computeCurrentStreak(habit, allLogsForHabit, logicalToday)
                return DayHabitSummary(
                    habit = habit,
                    category = category,
                    status = dayStatus,
                    totalValue = completedSlots.toDouble(),
                    targetValue = 2.0,
                    logs = activeLogs,
                    currentStreak = streak,
                    morningStatus = amStatus,
                    eveningStatus = pmStatus
                )
            }

            val hasSkip = activeLogs.any { it.status == LogStatus.INTENTIONALLY_SKIPPED }
            val hasExplicitMissed = activeLogs.any { it.status == LogStatus.MISSED }
            val nonSkipLogs = activeLogs.filter {
                it.status != LogStatus.INTENTIONALLY_SKIPPED && it.status != LogStatus.MISSED
            }

            val target = nonSkipLogs.lastOrNull()?.targetValueSnapshot ?: habit.targetValue.coerceAtLeast(1.0)
            val totalValue = when {
                nonSkipLogs.isEmpty() -> 0.0
                habit.trackingType == TrackingType.RATING -> nonSkipLogs.last().value
                else -> nonSkipLogs.sumOf { it.value }
            }

            val status = when {
                hasSkip && nonSkipLogs.isEmpty() -> LogStatus.INTENTIONALLY_SKIPPED
                hasExplicitMissed && nonSkipLogs.isEmpty() -> LogStatus.MISSED
                nonSkipLogs.isNotEmpty() && totalValue >= target -> LogStatus.COMPLETED
                nonSkipLogs.isNotEmpty() && totalValue > 0.0 -> LogStatus.PARTIALLY_COMPLETED
                targetDate < logicalToday && habit.status == HabitStatus.ACTIVE && habit.scheduleType != ScheduleType.OCCASIONAL -> LogStatus.MISSED
                else -> LogStatus.SCHEDULED
            }

            val streak = precomputedStreak ?: computeCurrentStreak(habit, allLogsForHabit, logicalToday)
            return DayHabitSummary(
                habit = habit,
                category = category,
                status = status,
                totalValue = totalValue,
                targetValue = target,
                logs = activeLogs,
                currentStreak = streak
            )
        }

        fun computeCurrentStreak(
            habit: HabitEntity,
            allLogsForHabit: List<HabitLogEntity>,
            logicalToday: String
        ): Int {
            if (habit.status == HabitStatus.ARCHIVED) return 0
            val logsByDate = allLogsForHabit.groupBy { it.logDate }
            var streak = 0
            var cursor = logicalToday

            // Check up to 365 days back
            for (i in 0 until 365) {
                if (cursor < habit.startDate) break
                val scheduled = isHabitScheduledOn(habit, cursor)
                val dayLogs = logsByDate[cursor].orEmpty()
                val hasSkip = dayLogs.any { it.status == LogStatus.INTENTIONALLY_SKIPPED }
                val nonSkip = dayLogs.filter {
                    it.status != LogStatus.INTENTIONALLY_SKIPPED && it.status != LogStatus.MISSED
                }
                val target = nonSkip.lastOrNull()?.targetValueSnapshot ?: habit.targetValue.coerceAtLeast(1.0)
                val total = if (habit.trackingType == TrackingType.RATING) {
                    nonSkip.lastOrNull()?.value ?: 0.0
                } else {
                    nonSkip.sumOf { it.value }
                }
                val isCompleted = nonSkip.isNotEmpty() && total >= target

                if (isCompleted) {
                    streak++
                } else if (hasSkip) {
                    // Intentionally skipped preserves streak without incrementing
                } else if (scheduled) {
                    if (i == 0 && dayLogs.none { it.status == LogStatus.MISSED }) {
                        // Today not yet completed doesn't break yesterday's streak unless explicitly marked No
                    } else {
                        break
                    }
                }
                cursor = DateHelper.addDays(cursor, -1)
            }
            return streak
        }

        fun computeBestStreak(
            habit: HabitEntity,
            allLogsForHabit: List<HabitLogEntity>,
            logicalToday: String
        ): Int {
            if (habit.status == HabitStatus.ARCHIVED) return 0
            val logsByDate = allLogsForHabit.groupBy { it.logDate }
            var best = 0
            var current = 0
            var cursor = habit.startDate
            if (cursor > logicalToday) return 0

            val totalDays = DateHelper.daysBetween(habit.startDate, logicalToday).coerceIn(0, 365)
            for (i in 0..totalDays) {
                val scheduled = isHabitScheduledOn(habit, cursor)
                val dayLogs = logsByDate[cursor].orEmpty()
                val hasSkip = dayLogs.any { it.status == LogStatus.INTENTIONALLY_SKIPPED }
                val nonSkip = dayLogs.filter {
                    it.status != LogStatus.INTENTIONALLY_SKIPPED && it.status != LogStatus.MISSED
                }
                val target = nonSkip.lastOrNull()?.targetValueSnapshot ?: habit.targetValue.coerceAtLeast(1.0)
                val total = if (habit.trackingType == TrackingType.RATING) {
                    nonSkip.lastOrNull()?.value ?: 0.0
                } else {
                    nonSkip.sumOf { it.value }
                }
                val isCompleted = nonSkip.isNotEmpty() && total >= target

                if (isCompleted) {
                    current++
                    if (current > best) best = current
                } else if (hasSkip) {
                    // Skip preserves streak
                } else if (scheduled) {
                    if (cursor < logicalToday || dayLogs.any { it.status == LogStatus.MISSED }) {
                        current = 0
                    }
                }
                cursor = DateHelper.addDays(cursor, 1)
            }
            return best
        }
    }
}
