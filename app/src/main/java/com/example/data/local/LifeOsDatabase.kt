package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        com.example.data.local.entity.StressLogEntity::class,
        com.example.data.local.entity.ExpenseEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeOsDatabase : RoomDatabase() {
    abstract fun lifeOsDao(): LifeOsDao

    companion object {
        @Volatile
        private var INSTANCE: LifeOsDatabase? = null

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN secondReminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN secondReminderHour INTEGER NOT NULL DEFAULT 20")
                db.execSQL("ALTER TABLE habits ADD COLUMN secondReminderMinute INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stress_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        logDate TEXT NOT NULL,
                        level TEXT NOT NULL,
                        trigger TEXT NOT NULL,
                        note TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_stress_logs_logDate ON stress_logs (logDate)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        logDate TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        note TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_logDate ON expenses (logDate)")
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habit_logs ADD COLUMN slot TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): LifeOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeOsDatabase::class.java,
                    "life_os.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(SeedCategoriesCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCategoriesCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                val defaults = listOf(
                    Triple("Health", 0xFF10B981L, "health"),
                    Triple("Fitness", 0xFFF43F5EL, "fitness"),
                    Triple("Learning", 0xFF6366F1L, "learning"),
                    Triple("Productivity", 0xFFF59E0BL, "productivity"),
                    Triple("Personal Care", 0xFFEC4899L, "care"),
                    Triple("Mindfulness", 0xFF14B8A6L, "mindfulness")
                )
                defaults.forEach { (name, color, icon) ->
                    db.execSQL(
                        "INSERT INTO categories (name, colorHex, iconKey, isDefault, createdAt) VALUES (?, ?, ?, 1, ?)",
                        arrayOf<Any>(name, color, icon, now)
                    )
                }
            }
        }
    }
}
