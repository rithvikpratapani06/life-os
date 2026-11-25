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
    INTERVAL("Every N Days"),
    OCCASIONAL("Few Days / Month")
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

enum class AccentPalette(
    val label: String,
    val lightPrimary: Long,
    val darkPrimary: Long,
    val lightContainer: Long,
    val darkContainer: Long
) {
    EMERALD("Emerald", 0xFF10B981L, 0xFF34D399L, 0xFFD1FAE5L, 0xFF064E3BL),
    INDIGO("Indigo", 0xFF6366F1L, 0xFF818CF8L, 0xFFE0E7FFL, 0xFF312E81L),
    CORAL("Coral", 0xFFF43F5EL, 0xFFFB7185L, 0xFFFFE4E6L, 0xFF881337L),
    AMBER("Amber", 0xFFD97706L, 0xFFFBBF24L, 0xFFFEF3C7L, 0xFF78350FL),
    CYAN("Cyan", 0xFF0891B2L, 0xFF22D3EEL, 0xFFCFFAFEL, 0xFF164E63L),
    VIOLET("Violet", 0xFF7C3AEDL, 0xFFA78BFAL, 0xFFEDE9FEL, 0xFF4C1D95L)
}

enum class ActivityKind(val label: String) {
    SPORTS("Sports & Play"),
    FOOD("Food & Dining"),
    DRINK("Drinks / Alcohol"),
    SOCIAL("Social & Outing"),
    OTHER("Other Event")
}
