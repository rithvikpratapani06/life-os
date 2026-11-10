package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateHelper {
    private val isoFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
    }

    private val monthYearFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }

    private val readableFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    }

    private val shortMonthFormat = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat =
            SimpleDateFormat("MMM", Locale.getDefault())
    }

    fun logicalToday(cutoffHour: Int = 3): String {
        val cal = Calendar.getInstance()
        if (cal.get(Calendar.HOUR_OF_DAY) < cutoffHour) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return isoFormat.get()!!.format(cal.time)
    }

    fun parse(dateStr: String): Calendar {
        val date = isoFormat.get()!!.parse(dateStr) ?: Date()
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun format(cal: Calendar): String = isoFormat.get()!!.format(cal.time)

    fun addDays(dateStr: String, days: Int): String {
        val cal = parse(dateStr)
        cal.add(Calendar.DAY_OF_YEAR, days)
        return format(cal)
    }

    fun daysBetween(startDateStr: String, endDateStr: String): Int {
        val start = parse(startDateStr).timeInMillis
        val end = parse(endDateStr).timeInMillis
        return TimeUnit.MILLISECONDS.toDays(end - start).toInt()
    }

    /**
     * Returns 0 = Monday .. 6 = Sunday (matching our 7-bit scheduleDaysMask).
     */
    fun dayOfWeekMonZero(dateStr: String): Int {
        val cal = parse(dateStr)
        return calendarDayToMonZero(cal.get(Calendar.DAY_OF_WEEK))
    }

    fun calendarDayToMonZero(calendarDayOfWeek: Int): Int = when (calendarDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    fun isDayInMask(dateStr: String, mask: Int): Boolean {
        val bit = 1 shl dayOfWeekMonZero(dateStr)
        return (mask and bit) != 0
    }

    fun formatReadable(dateStr: String): String {
        val cal = parse(dateStr)
        return readableFormat.get()!!.format(cal.time)
    }

    fun formatMonthYear(year: Int, monthZeroBased: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return monthYearFormat.get()!!.format(cal.time)
    }

    fun daysInMonth(year: Int, monthZeroBased: Int): List<String> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val fmt = isoFormat.get()!!
        return (1..max).map { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            fmt.format(cal.time)
        }
    }

    fun firstDayOfMonthOffsetMonZero(year: Int, monthZeroBased: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return calendarDayToMonZero(cal.get(Calendar.DAY_OF_WEEK))
    }

    fun lastNDates(endDateStr: String, count: Int): List<String> {
        val cal = parse(endDateStr)
        cal.add(Calendar.DAY_OF_YEAR, -(count - 1))
        val fmt = isoFormat.get()!!
        return List(count) {
            val s = fmt.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            s
        }
    }

    fun shortDayLabel(dateStr: String): String {
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return labels[dayOfWeekMonZero(dateStr)]
    }

    fun shortMonthLabel(dateStr: String): String {
        val cal = parse(dateStr)
        return shortMonthFormat.get()!!.format(cal.time)
    }

    fun endOfWeekSunday(dateStr: String): String {
        val offsetToSunday = 6 - dayOfWeekMonZero(dateStr)
        return addDays(dateStr, offsetToSunday)
    }
}
