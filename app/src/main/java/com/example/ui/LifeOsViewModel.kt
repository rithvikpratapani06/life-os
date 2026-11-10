package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LifeOsDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.StressLogEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.HabitStatus
import com.example.data.model.LogStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.StressLevel
import com.example.data.model.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.DayHabitSummary
import com.example.data.repository.LifeOsRepository
import com.example.notifications.ReminderScheduler
import com.example.util.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyTrendPoint(
    val date: String,
    val label: String,
    val completedCount: Int,
    val scheduledCount: Int
) {
    val rate: Float
        get() = if (scheduledCount == 0) 0f else completedCount.toFloat() / scheduledCount.toFloat()
}

data class CategoryInsight(
    val category: CategoryEntity,
    val completed: Int,
    val scheduled: Int
) {
    val rate: Float
        get() = if (scheduled == 0) 0f else completed.toFloat() / scheduled.toFloat()
}

data class HeatmapCell(
    val date: String,
    val status: LogStatus?, // null = not scheduled / before startDate
    val isScheduled: Boolean
)

data class HabitAnalytics(
    val habit: HabitEntity,
    val category: CategoryEntity?,
    val currentStreak: Int,
    val bestStreak: Int,
    val weeklyRate: Float,
    val weeklyCompleted: Int,
    val weeklyPartial: Int,
    val weeklySkipped: Int,
    val weeklyMissed: Int,
    val monthlyRate: Float,
    val monthlyCompleted: Int,
    val monthlyPartial: Int,
    val monthlySkipped: Int,
    val monthlyMissed: Int,
    val monthlyTotalValue: Double,
    val heatmapDays: List<HeatmapCell> // Last 84 days (12 weeks)
)

data class PeriodInsights(
    val completionRate: Float = 0f,
    val completedCount: Int = 0,
    val partialCount: Int = 0,
    val skippedCount: Int = 0,
    val missedCount: Int = 0,
    val trend: List<DailyTrendPoint> = emptyList(),
    val categoryBreakdown: List<CategoryInsight> = emptyList(),
    val topStreaks: List<Pair<HabitEntity, Int>> = emptyList()
)

data class StressAnalytics(
    val todayStress: StressLogEntity? = null,
    val weeklyLogs: List<StressLogEntity> = emptyList(),
    val monthlyLogs: List<StressLogEntity> = emptyList(),
    val weeklyCounts: Map<StressLevel, Int> = emptyMap(),
    val monthlyCounts: Map<StressLevel, Int> = emptyMap(),
    val weeklyAvgScore: Float = 0f,
    val monthlyAvgScore: Float = 0f,
    val recentLogs: List<StressLogEntity> = emptyList()
)

data class DailySpendPoint(
    val date: String,
    val label: String,
    val total: Double
)

data class FinanceSummary(
    val todaySpent: Double = 0.0,
    val monthSpent: Double = 0.0,
    val monthDailyAverage: Double = 0.0,
    val paymentMethodTotals: List<Pair<PaymentMethod, Double>> = emptyList(),
    val categoryTotals: List<Pair<ExpenseCategory, Double>> = emptyList(),
    val last7DaysSpending: List<DailySpendPoint> = emptyList(),
    val recentExpenses: List<ExpenseEntity> = emptyList()
)

data class LifeOsUiState(
    val logicalToday: String = DateHelper.logicalToday(),
    val selectedPlannerDate: String = DateHelper.logicalToday(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dayCutoffHour: Int = 3,
    val isPinEnabled: Boolean = false,
    val isUnlocked: Boolean = true,
    val screenSecurityEnabled: Boolean = false,
    val stressReminderEnabled: Boolean = true,
    val stressReminderHour: Int = 21,
    val stressReminderMinute: Int = 0,
    val categories: List<CategoryEntity> = emptyList(),
    val allHabits: List<HabitEntity> = emptyList(),
    val todaySummaries: List<DayHabitSummary> = emptyList(),
    val plannerSummaries: List<DayHabitSummary> = emptyList(),
    val monthDayCompletionMap: Map<String, Float> = emptyMap(),
    val upcomingReminders: List<HabitEntity> = emptyList(),
    val weeklyInsights: PeriodInsights = PeriodInsights(),
    val monthlyInsights: PeriodInsights = PeriodInsights(),
    val perHabitAnalytics: List<HabitAnalytics> = emptyList(),
    val stressAnalytics: StressAnalytics = StressAnalytics(),
    val financeSummary: FinanceSummary = FinanceSummary()
)

class LifeOsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LifeOsDatabase.getInstance(application)
    private val repository = LifeOsRepository(db.lifeOsDao())
    private val prefs = UserPreferencesRepository(application)

    private val selectedPlannerDate = MutableStateFlow(DateHelper.logicalToday())
    private val isUnlockedState = MutableStateFlow(false)
    private var currentPinCreds: Pair<String, String>? = null
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val coreDataFlow = combine(
        repository.categories,
        repository.habits,
        repository.logs,
        repository.stressLogs,
        repository.expenses
    ) { cats, hbs, lgs, stress, exps ->
        DataBundle(cats, hbs, lgs, stress, exps)
    }

    private val prefsFlow = combine(
        prefs.themeMode,
        prefs.dayCutoffHour,
        selectedPlannerDate,
        prefs.pinCredentials,
        prefs.screenSecurityEnabled,
        prefs.stressReminderConfig,
        isUnlockedState
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        PrefsBundle(
            themeMode = values[0] as ThemeMode,
            cutoffHour = values[1] as Int,
            plannerDate = values[2] as String,
            pinCreds = values[3] as Pair<String, String>?,
            screenSec = values[4] as Boolean,
            stressReminder = values[5] as Triple<Boolean, Int, Int>,
            unlocked = values[6] as Boolean
        )
    }

    val uiState: StateFlow<LifeOsUiState> = combine(coreDataFlow, prefsFlow) { data, pref ->
        val categories = data.categories
        val habits = data.habits
        val logs = data.logs
        val stressLogs = data.stressLogs
        val expenses = data.expenses

        currentPinCreds = pref.pinCreds
        val pinEnabled = pref.pinCreds != null
        val today = DateHelper.logicalToday(pref.cutoffHour)
        val catMap = categories.associateBy { it.id }
        val logsByHabit = logs.groupBy { it.habitId }
        val streakMap = habits.associate { h ->
            h.id to LifeOsRepository.computeCurrentStreak(h, logsByHabit[h.id].orEmpty(), today)
        }

        val todaySummaries = buildSummariesForDate(today, today, habits, catMap, logsByHabit, streakMap)
        val plannerSummaries = buildSummariesForDate(pref.plannerDate, today, habits, catMap, logsByHabit, streakMap)

        val heatmapDates = DateHelper.lastNDates(today, 45)
        val monthMap = heatmapDates.associateWith { date ->
            val dayItems = buildSummariesForDate(date, today, habits, catMap, logsByHabit, streakMap)
            if (dayItems.isEmpty()) 0f
            else dayItems.count { it.status == LogStatus.COMPLETED }.toFloat() / dayItems.size.toFloat()
        }

        val upcomingReminders = habits
            .filter { it.status == HabitStatus.ACTIVE && it.reminderEnabled }
            .sortedBy { it.reminderHour * 60 + it.reminderMinute }

        val weekly = computePeriodInsights(today, 7, categories, habits, catMap, logsByHabit, streakMap)
        val monthly = computePeriodInsights(today, 30, categories, habits, catMap, logsByHabit, streakMap)
        val perHabit = computePerHabitAnalytics(today, habits, catMap, logsByHabit, streakMap)
        val stressAnalytics = computeStressAnalytics(today, stressLogs)
        val financeSummary = computeFinanceSummary(today, expenses)

        LifeOsUiState(
            logicalToday = today,
            selectedPlannerDate = pref.plannerDate,
            themeMode = pref.themeMode,
            dayCutoffHour = pref.cutoffHour,
            isPinEnabled = pinEnabled,
            isUnlocked = !pinEnabled || pref.unlocked,
            screenSecurityEnabled = pref.screenSec,
            stressReminderEnabled = pref.stressReminder.first,
            stressReminderHour = pref.stressReminder.second,
            stressReminderMinute = pref.stressReminder.third,
            categories = categories,
            allHabits = habits,
            todaySummaries = todaySummaries,
            plannerSummaries = plannerSummaries,
            monthDayCompletionMap = monthMap,
            upcomingReminders = upcomingReminders,
            weeklyInsights = weekly,
            monthlyInsights = monthly,
            perHabitAnalytics = perHabit,
            stressAnalytics = stressAnalytics,
            financeSummary = financeSummary
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LifeOsUiState()
        )

    private data class DataBundle(
        val categories: List<CategoryEntity>,
        val habits: List<HabitEntity>,
        val logs: List<HabitLogEntity>,
        val stressLogs: List<StressLogEntity>,
        val expenses: List<ExpenseEntity>
    )

    private data class PrefsBundle(
        val themeMode: ThemeMode,
        val cutoffHour: Int,
        val plannerDate: String,
        val pinCreds: Pair<String, String>?,
        val screenSec: Boolean,
        val stressReminder: Triple<Boolean, Int, Int>,
        val unlocked: Boolean
    )

    private fun buildSummariesForDate(
        date: String,
        logicalToday: String,
        habits: List<HabitEntity>,
        catMap: Map<Long, CategoryEntity>,
        logsByHabit: Map<Long, List<HabitLogEntity>>,
        streakMap: Map<Long, Int>
    ): List<DayHabitSummary> {
        return habits
            .filter { it.status == HabitStatus.ACTIVE && LifeOsRepository.isHabitScheduledOn(it, date) }
            .map { habit ->
                val habitLogs = logsByHabit[habit.id].orEmpty()
                val dayLogs = habitLogs.filter { it.logDate == date }
                LifeOsRepository.evaluateDaySummary(
                    habit = habit,
                    category = catMap[habit.categoryId],
                    logsForDay = dayLogs,
                    allLogsForHabit = habitLogs,
                    targetDate = date,
                    logicalToday = logicalToday,
                    precomputedStreak = streakMap[habit.id] ?: 0
                )
            }
    }

    private fun computePeriodInsights(
        today: String,
        daysCount: Int,
        categories: List<CategoryEntity>,
        habits: List<HabitEntity>,
        catMap: Map<Long, CategoryEntity>,
        logsByHabit: Map<Long, List<HabitLogEntity>>,
        streakMap: Map<Long, Int>
    ): PeriodInsights {
        val dates = DateHelper.lastNDates(today, daysCount)
        var completed = 0
        var partial = 0
        var skipped = 0
        var missed = 0
        var totalScheduled = 0

        val catCompleted = mutableMapOf<Long, Int>()
        val catScheduled = mutableMapOf<Long, Int>()

        val trend = dates.map { date ->
            val items = buildSummariesForDate(date, today, habits, catMap, logsByHabit, streakMap)
            var dayCompleted = 0
            items.forEach { item ->
                totalScheduled++
                val cid = item.habit.categoryId
                catScheduled[cid] = (catScheduled[cid] ?: 0) + 1
                when (item.status) {
                    LogStatus.COMPLETED -> {
                        completed++
                        dayCompleted++
                        catCompleted[cid] = (catCompleted[cid] ?: 0) + 1
                    }
                    LogStatus.PARTIALLY_COMPLETED -> partial++
                    LogStatus.INTENTIONALLY_SKIPPED -> skipped++
                    LogStatus.MISSED -> missed++
                    LogStatus.SCHEDULED -> Unit
                }
            }
            DailyTrendPoint(
                date = date,
                label = if (daysCount <= 7) DateHelper.shortDayLabel(date) else date.takeLast(2),
                completedCount = dayCompleted,
                scheduledCount = items.size
            )
        }

        val denom = (totalScheduled - skipped).coerceAtLeast(0)
        val rate = if (denom == 0) 0f else completed.toFloat() / denom.toFloat()

        val categoryBreakdown = categories.mapNotNull { cat ->
            val sched = catScheduled[cat.id] ?: 0
            if (sched == 0) null
            else CategoryInsight(cat, catCompleted[cat.id] ?: 0, sched)
        }.sortedByDescending { it.rate }

        val topStreaks = habits
            .filter { it.status == HabitStatus.ACTIVE }
            .map { h -> h to LifeOsRepository.computeCurrentStreak(h, logsByHabit[h.id].orEmpty(), today) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(5)

        return PeriodInsights(
            completionRate = rate,
            completedCount = completed,
            partialCount = partial,
            skippedCount = skipped,
            missedCount = missed,
            trend = trend,
            categoryBreakdown = categoryBreakdown,
            topStreaks = topStreaks
        )
    }

    private fun computePerHabitAnalytics(
        today: String,
        habits: List<HabitEntity>,
        catMap: Map<Long, CategoryEntity>,
        logsByHabit: Map<Long, List<HabitLogEntity>>,
        streakMap: Map<Long, Int>
    ): List<HabitAnalytics> {
        val weekEnd = DateHelper.endOfWeekSunday(today)
        val last84 = DateHelper.lastNDates(weekEnd, 84)
        val last30Set = DateHelper.lastNDates(today, 30).toSet()
        val last7Set = DateHelper.lastNDates(today, 7).toSet()

        return habits
            .filter { it.status != HabitStatus.ARCHIVED }
            .map { habit ->
                val habitLogs = logsByHabit[habit.id].orEmpty()
                val logsByDate = habitLogs.groupBy { it.logDate }
                val habitStreak = streakMap[habit.id] ?: 0

                var wComp = 0; var wPart = 0; var wSkip = 0; var wMiss = 0; var wSched = 0
                var mComp = 0; var mPart = 0; var mSkip = 0; var mMiss = 0; var mSched = 0
                var mTotalVal = 0.0

                val heatmap = last84.map { date ->
                    val scheduled = date <= today && LifeOsRepository.isHabitScheduledOn(habit, date)
                    if (!scheduled) {
                        HeatmapCell(date = date, status = null, isScheduled = false)
                    } else {
                        val summary = LifeOsRepository.evaluateDaySummary(
                            habit = habit,
                            category = catMap[habit.categoryId],
                            logsForDay = logsByDate[date].orEmpty(),
                            allLogsForHabit = habitLogs,
                            targetDate = date,
                            logicalToday = today,
                            precomputedStreak = habitStreak
                        )
                        if (date in last7Set) {
                            wSched++
                            when (summary.status) {
                                LogStatus.COMPLETED -> wComp++
                                LogStatus.PARTIALLY_COMPLETED -> wPart++
                                LogStatus.INTENTIONALLY_SKIPPED -> wSkip++
                                LogStatus.MISSED -> wMiss++
                                LogStatus.SCHEDULED -> Unit
                            }
                        }
                        if (date in last30Set) {
                            mSched++
                            mTotalVal += summary.totalValue
                            when (summary.status) {
                                LogStatus.COMPLETED -> mComp++
                                LogStatus.PARTIALLY_COMPLETED -> mPart++
                                LogStatus.INTENTIONALLY_SKIPPED -> mSkip++
                                LogStatus.MISSED -> mMiss++
                                LogStatus.SCHEDULED -> Unit
                            }
                        }
                        HeatmapCell(date = date, status = summary.status, isScheduled = true)
                    }
                }

                val wDenom = (wSched - wSkip).coerceAtLeast(0)
                val mDenom = (mSched - mSkip).coerceAtLeast(0)

                HabitAnalytics(
                    habit = habit,
                    category = catMap[habit.categoryId],
                    currentStreak = LifeOsRepository.computeCurrentStreak(habit, habitLogs, today),
                    bestStreak = LifeOsRepository.computeBestStreak(habit, habitLogs, today),
                    weeklyRate = if (wDenom == 0) 0f else wComp.toFloat() / wDenom.toFloat(),
                    weeklyCompleted = wComp,
                    weeklyPartial = wPart,
                    weeklySkipped = wSkip,
                    weeklyMissed = wMiss,
                    monthlyRate = if (mDenom == 0) 0f else mComp.toFloat() / mDenom.toFloat(),
                    monthlyCompleted = mComp,
                    monthlyPartial = mPart,
                    monthlySkipped = mSkip,
                    monthlyMissed = mMiss,
                    monthlyTotalValue = mTotalVal,
                    heatmapDays = heatmap
                )
            }
    }

    fun selectPlannerDate(date: String) {
        selectedPlannerDate.value = date
    }

    fun logHabit(
        habit: HabitEntity,
        date: String,
        value: Double,
        note: String = "",
        isSkip: Boolean = false,
        isExplicitNo: Boolean = false
    ) {
        viewModelScope.launch {
            repository.logHabitProgress(habit, date, value, note, isSkip, isExplicitNo)
        }
    }

    fun clearHabitDayLogs(habitId: Long, date: String) {
        viewModelScope.launch {
            repository.clearDayLogs(habitId, date)
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val savedId = repository.saveHabit(habit)
            val updated = habit.copy(id = savedId)
            ReminderScheduler.syncReminder(getApplication(), updated)
            _events.emit(if (habit.id == 0L) "Habit created" else "Habit updated")
        }
    }

    fun setHabitStatus(habit: HabitEntity, status: HabitStatus) {
        viewModelScope.launch {
            repository.updateHabitStatus(habit, status)
            ReminderScheduler.syncReminder(getApplication(), habit.copy(status = status))
            _events.emit("Habit marked ${status.label.lowercase()}")
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), habitId)
            repository.deleteHabit(habitId)
            _events.emit("Habit deleted")
        }
    }

    fun addCategory(name: String, colorHex: Long, iconKey: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            repository.addCategory(name, colorHex, iconKey)
            _events.emit("Category added")
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _events.emit("Category updated")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val deleted = repository.deleteCategory(category)
            _events.emit(
                if (deleted) "Category deleted"
                else "Cannot delete category with existing habits"
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setDayCutoffHour(hour: Int) {
        viewModelScope.launch { prefs.setDayCutoffHour(hour) }
    }

    fun verifyPin(pin: String): Boolean {
        val creds = currentPinCreds ?: return true
        val (expectedHash, salt) = creds
        val actualHash = UserPreferencesRepository.hashPin(pin, salt)
        val ok = actualHash == expectedHash
        if (ok) {
            isUnlockedState.value = true
        }
        return ok
    }

    fun setAppPin(pin: String) {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return
        viewModelScope.launch {
            prefs.setPin(pin)
            isUnlockedState.value = true
            _events.emit("App PIN lock enabled")
        }
    }

    fun disableAppPin() {
        viewModelScope.launch {
            prefs.clearPin()
            _events.emit("App PIN lock disabled")
        }
    }

    fun setScreenSecurityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setScreenSecurity(enabled)
            _events.emit(if (enabled) "Screen privacy enabled" else "Screen privacy disabled")
        }
    }

    fun logStress(level: StressLevel, trigger: String, note: String) {
        viewModelScope.launch {
            repository.logStress(uiState.value.logicalToday, level, trigger, note)
            _events.emit("Stress level logged: ${level.label}")
        }
    }

    fun setStressReminder(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setStressReminder(enabled, hour, minute)
            ReminderScheduler.syncStressReminder(getApplication(), enabled, hour, minute)
            _events.emit(if (enabled) "Daily stress reminder set" else "Daily stress reminder off")
        }
    }

    fun addExpense(
        logDate: String,
        amount: Double,
        category: ExpenseCategory,
        title: String,
        paymentMethod: PaymentMethod,
        note: String
    ) {
        viewModelScope.launch {
            repository.addExpense(logDate, amount, category, title, paymentMethod, note)
            _events.emit("Expense recorded")
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
            _events.emit("Expense deleted")
        }
    }

    private fun computeStressAnalytics(today: String, logs: List<StressLogEntity>): StressAnalytics {
        val last7 = DateHelper.lastNDates(today, 7).toSet()
        val last30 = DateHelper.lastNDates(today, 30).toSet()
        val wLogs = logs.filter { it.logDate in last7 }
        val mLogs = logs.filter { it.logDate in last30 }

        val wCounts = StressLevel.entries.associateWith { lvl -> wLogs.count { it.level == lvl } }
        val mCounts = StressLevel.entries.associateWith { lvl -> mLogs.count { it.level == lvl } }

        val wAvg = if (wLogs.isEmpty()) 0f else wLogs.map { it.level.score }.average().toFloat()
        val mAvg = if (mLogs.isEmpty()) 0f else mLogs.map { it.level.score }.average().toFloat()

        return StressAnalytics(
            todayStress = logs.firstOrNull { it.logDate == today },
            weeklyLogs = wLogs,
            monthlyLogs = mLogs,
            weeklyCounts = wCounts,
            monthlyCounts = mCounts,
            weeklyAvgScore = wAvg,
            monthlyAvgScore = mAvg,
            recentLogs = logs.take(14)
        )
    }

    private fun computeFinanceSummary(today: String, expenses: List<ExpenseEntity>): FinanceSummary {
        val monthPrefix = today.take(7) // yyyy-MM
        val dayOfMonth = today.takeLast(2).toIntOrNull()?.coerceAtLeast(1) ?: 1

        val todaySpent = expenses.filter { it.logDate == today }.sumOf { it.amount }
        val monthExpenses = expenses.filter { it.logDate.startsWith(monthPrefix) }
        val monthSpent = monthExpenses.sumOf { it.amount }
        val monthDailyAvg = monthSpent / dayOfMonth.toDouble()

        val paymentTotals = PaymentMethod.entries.map { pm ->
            pm to monthExpenses.filter { it.paymentMethod == pm }.sumOf { it.amount }
        }.filter { it.second > 0.0 }.sortedByDescending { it.second }

        val categoryTotals = ExpenseCategory.entries.map { cat ->
            cat to monthExpenses.filter { it.category == cat }.sumOf { it.amount }
        }.filter { it.second > 0.0 }.sortedByDescending { it.second }

        val last7 = DateHelper.lastNDates(today, 7)
        val byDate = expenses.groupBy { it.logDate }
        val last7Points = last7.map { d ->
            DailySpendPoint(
                date = d,
                label = DateHelper.shortDayLabel(d),
                total = byDate[d].orEmpty().sumOf { it.amount }
            )
        }

        return FinanceSummary(
            todaySpent = todaySpent,
            monthSpent = monthSpent,
            monthDailyAverage = monthDailyAvg,
            paymentMethodTotals = paymentTotals,
            categoryTotals = categoryTotals,
            last7DaysSpending = last7Points,
            recentExpenses = expenses.take(30)
        )
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportAllDataJson()
            onResult(json)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            uiState.value.allHabits.forEach {
                ReminderScheduler.cancelReminder(getApplication(), it.id)
            }
            repository.clearAllUserData()
            _events.emit("All habit data cleared")
        }
    }
}
