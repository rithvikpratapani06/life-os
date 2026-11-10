package com.example.data.model

enum class TrackingType(val label: String, val defaultUnit: String) {
    BOOLEAN("Check-off", "done"),
    NUMERIC("Numeric", "pages"),
    DURATION("Duration", "min"),
    QUANTITY("Quantity", "glasses"),
    RATING("Rating (1–5)", "pts")
}

enum class ScheduleType(val label: String) {
    DAILY("Every Day"),
    TWICE_DAILY("Twice a Day"),
    SPECIFIC_DAYS("Specific Days"),
    WEEKLY_TARGET("X Days / Week"),
    INTERVAL("Every N Days")
}

enum class HabitStatus(val label: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    ARCHIVED("Archived")
}

enum class LogStatus(val label: String) {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    PARTIALLY_COMPLETED("Partial"),
    INTENTIONALLY_SKIPPED("Skipped"),
    MISSED("Missed")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class StressLevel(val label: String, val score: Int) {
    LOW("Low", 1),
    MODERATE("Moderate", 2),
    HIGH("High", 3),
    VERY_HIGH("Very High", 4)
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    UPI_CREDIT_CARD("UPI (Credit Card)"),
    UPI_DEBIT_CARD("UPI (Debit Card)"),
    OTHER("Card / Other")
}

enum class ExpenseCategory(val label: String) {
    FOOD("Food & Dining"),
    GROCERIES("Groceries"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills & Utilities"),
    HEALTH("Health"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other")
}
