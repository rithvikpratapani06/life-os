# Habtrac

**Habtrac** is an offline-first personal habit tracking, wellness, finance, and life-analytics Android application built with **Kotlin**, **Jetpack Compose**, **Material Design 3**, and **Room (SQLite)**.

Designed for speed, visual clarity, and complete local privacy, Habtrac records immutable historical logs so you can analyze consistency, stress patterns, and daily spending without accounts, ads, or cloud dependencies.

---

## Features

### 1. Habit Tracking & Flexible Schedules
- **5 Tracking Types:**
  - **Check-off (`BOOLEAN`):** Quick **Yes / No** completion logging.
  - **Numeric (`NUMERIC`):** Pages, reps, steps, or custom counts.
  - **Duration (`DURATION`):** Minutes or hours spent.
  - **Quantity (`QUANTITY`):** Glasses of water, servings, or items.
  - **Rating (`RATING`):** 1–5 subjective daily score.
- **6 Schedule Modes:**
  - **Every Day (`DAILY`)**
  - **Twice a Day (`TWICE_DAILY`):** Independent **Morning (`AM`)** and **Night (`PM`)** slot tracking (`Done`, `Missed`, `Skipped`) so missing Morning and completing Night records accurately.
  - **Specific Days (`SPECIFIC_DAYS`):** Custom weekday bitmask (Mon–Sun).
  - **X Days / Week (`WEEKLY_TARGET`):** Flexible weekly target.
  - **Every N Days (`INTERVAL`):** Repeating multi-day cadence.
  - **Few Days / Month (`OCCASIONAL`):** Ideal for sports or infrequent activities—unlogged rest days remain neutral and do not count as missed.
- **Historical Log Integrity:** Each log snapshots the target value and tracking type at the time of entry; changing a target today never overwrites past history.
- **Custom Categories:** Create, edit, and color-code categories with custom icons.

### 2. Daily Stress Tracking & Alerts
- Quick daily check-in from Home (**Low**, **Moderate**, **High**, **Very High**).
- Configurable daily notification reminder (`AlarmManager` + `NotificationCompat`).
- Dedicated **Stress** tab in Insights with weekly and monthly distributions and average scores.

### 3. Finance Manager & Expense Tracker
- Dedicated **Finance** tab to track daily expenses with title/purpose, category, and payment method (**Cash**, **UPI**, **UPI Credit Card**, **UPI Debit Card**, **Card / Other**).
- Real-time metrics: **Today's Spend**, **Monthly Spend**, and **Monthly Daily Average**.
- **7-Day Spending Chart**, **Payment Method Breakdown**, and **Category Breakdown**.

### 4. Planner & 12-Week Calendar Heatmaps
- **Planner:** Monthly calendar overview; tap any date to inspect, log, or intentionally skip habits for that day.
- **Insights (`Overall`, `Per Habit`, `Stress`):**
  - **Weekly (7D)** and **Monthly (30D)** trend charts and completion rates.
  - **Per-Habit 12-Week Calendar Heatmap** with month headers, weekday rows (`M`–`S`), date numbers, and tap-to-inspect status details (`Completed`, `Partial`, `Skipped`, `Missed`).

### 5. Personalization, Guidance & Security
- **Personalization Studio:** Custom greeting name, **6 Accent Color Palettes** (Emerald, Indigo, Coral, Amber, Cyan, Violet), **Light / Dark / System** theme modes, and **Compact Layout**.
- **Logical Day Boundary:** Configurable cutoff hour (e.g., `03:00 AM`) so late-night logs count toward the previous day.
- **In-App Help & Tooltips:** Complete **Habtrac Guide** plus inline **`(i)`** info tooltips explaining settings and schedule options.
- **Security & Privacy:**
  - Zero internet, storage, or dynamic code permissions (`allowBackup="false"`, `usesCleartextTraffic="false"`).
  - Optional **4-digit App PIN Lock** stored with a salted **SHA-256** hash.
  - Optional **Screen Privacy** (`FLAG_SECURE`) to block screenshots and hide recents thumbnails.
  - One-tap **JSON Data Export**.

---

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Unidirectional StateFlow (`LifeOsViewModel`) |
| **Local Database** | Room (SQLite) with KSP & versioned schema migrations (`v1 → v4`) |
| **Preferences** | Jetpack DataStore (Preferences) |
| **Concurrency** | Kotlin Coroutines & Flow (`Dispatchers.Default` for analytics/heatmaps) |
| **Notifications** | `AlarmManager` & `NotificationCompat` (`BroadcastReceiver`) |
| **Typography** | Custom bundled `Plus Jakarta Sans` & `JetBrains Mono` |
| **Testing** | JUnit 4, Robolectric, Coroutines Test |

---

## Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt                  # App entry point, responsive BottomBar/NavRail, PIN & FLAG_SECURE guard
├── data/
│   ├── local/
│   │   ├── Converters.kt            # Room enum converters
│   │   ├── LifeOsDao.kt             # Room DAO queries (habits, logs, stress, expenses, categories)
│   │   ├── LifeOsDatabase.kt        # Room DB & migrations (v1 -> v4)
│   │   └── entity/                  # CategoryEntity, HabitEntity, HabitLogEntity, StressLogEntity, ExpenseEntity
│   ├── model/
│   │   └── Enums.kt                 # TrackingType, ScheduleType, LogStatus, StressLevel, PaymentMethod, AccentPalette
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt  # DataStore settings & salted SHA-256 PIN storage
│   └── repository/
│       └── LifeOsRepository.kt      # Habit evaluation, AM/PM slot logic, streak math, JSON export
├── notifications/
│   ├── ReminderReceiver.kt          # Notification delivery
│   └── ReminderScheduler.kt         # Dual habit & daily stress reminder scheduling
├── ui/
│   ├── LifeOsViewModel.kt           # StateFlow aggregation & background analytics/heatmap computation
│   ├── components/                  # CommonComponents, HabitEditorSheet, AppGuideSheet, PinLockScreen
│   ├── screens/                     # HomeScreen, HabitsScreen, FinanceScreen, PlannerScreen, InsightsScreen, SettingsScreen
│   └── theme/                       # Color.kt, Theme.kt, Type.kt
└── util/
    └── DateHelper.kt                # Thread-safe yyyy-MM-dd date utilities (minSdk 24 compatible)
```

---

## Getting Started

### Prerequisites
- **Android Studio** (Meerkat / Ladybug or newer) or **Gradle** CLI
- **JDK 11+**
- **Android SDK** (`compileSdk 36`, `minSdk 24`, `targetSdk 36`)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/habtrac.git
   cd habtrac
   ```
2. Open the project in Android Studio and sync Gradle, or build from the command line:
   ```bash
   gradle :app:assembleDebug
   ```
3. Run local JVM unit tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
4. Install the debug APK onto a connected device or emulator (`app/build/outputs/apk/debug/app-debug.apk`).

---

## License

This project is open for personal use and learning. Add your preferred license file (e.g., `MIT`) if distributing publicly.
