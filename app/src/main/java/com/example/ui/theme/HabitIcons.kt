package com.example.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.HabitEntity
import com.example.ui.components.categoryIconFor

data class HabitIconPreset(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val colorHex: Long,
    val group: String
)

val HabitIconCatalog: List<HabitIconPreset> = listOf(
    // Health & Personal Care
    HabitIconPreset("water", "Water", Icons.Default.WaterDrop, 0xFF0EA5E9L, "Health & Care"),
    HabitIconPreset("medication", "Medicine / Serum", Icons.Default.Medication, 0xFF14B8A6L, "Health & Care"),
    HabitIconPreset("skincare", "Skincare", Icons.Default.FaceRetouchingNatural, 0xFFEC4899L, "Health & Care"),
    HabitIconPreset("haircare", "Hair / Grooming", Icons.Default.Brush, 0xFF0D9488L, "Health & Care"),
    HabitIconPreset("shower", "Hygiene / Shower", Icons.Default.Shower, 0xFF06B6D4L, "Health & Care"),
    HabitIconPreset("nutrition", "Clean Eating", Icons.Default.Restaurant, 0xFF22C55EL, "Health & Care"),
    HabitIconPreset("hydration", "Drink / Tea", Icons.Default.LocalDrink, 0xFF10B981L, "Health & Care"),
    HabitIconPreset("no_alcohol", "No Alcohol", Icons.Default.NoDrinks, 0xFF059669L, "Health & Care"),
    HabitIconPreset("heart", "Heart / Vitals", Icons.Default.Favorite, 0xFFF43F5EL, "Health & Care"),
    HabitIconPreset("sleep", "Sleep", Icons.Default.Bedtime, 0xFF6366F1L, "Health & Care"),

    // Fitness & Sports
    HabitIconPreset("gym", "Gym / Strength", Icons.Default.FitnessCenter, 0xFFEF4444L, "Fitness & Sports"),
    HabitIconPreset("run", "Running", Icons.Default.DirectionsRun, 0xFFF97316L, "Fitness & Sports"),
    HabitIconPreset("yoga", "Yoga / Stretch", Icons.Default.SelfImprovement, 0xFF8B5CF6L, "Fitness & Sports"),
    HabitIconPreset("bike", "Cycling", Icons.Default.DirectionsBike, 0xFF0284C7L, "Fitness & Sports"),
    HabitIconPreset("cricket", "Cricket", Icons.Default.SportsCricket, 0xFF16A34AL, "Fitness & Sports"),
    HabitIconPreset("badminton", "Badminton / Tennis", Icons.Default.SportsTennis, 0xFF84CC16L, "Fitness & Sports"),
    HabitIconPreset("swim", "Swimming", Icons.Default.Pool, 0xFF0891B2L, "Fitness & Sports"),

    // Mind & Learning
    HabitIconPreset("reading", "Reading", Icons.Default.MenuBook, 0xFF4F46E5L, "Learning & Mind"),
    HabitIconPreset("coding", "Coding", Icons.Default.Code, 0xFF3B82F6L, "Learning & Mind"),
    HabitIconPreset("language", "Language", Icons.Default.Translate, 0xFF9333EAL, "Learning & Mind"),
    HabitIconPreset("journal", "Journal", Icons.Default.EditNote, 0xFFD97706L, "Learning & Mind"),
    HabitIconPreset("meditation", "Mindfulness", Icons.Default.Spa, 0xFF0F766EL, "Learning & Mind"),
    HabitIconPreset("music", "Music", Icons.Default.MusicNote, 0xFFD946EFL, "Learning & Mind"),

    // Productivity & Lifestyle
    HabitIconPreset("wake_early", "Wake Early", Icons.Default.WbSunny, 0xFFF59E0BL, "Productivity"),
    HabitIconPreset("alarm", "Schedule / Time", Icons.Default.Alarm, 0xFFEA580CL, "Productivity"),
    HabitIconPreset("focus", "Deep Work", Icons.Default.LaptopMac, 0xFF2563EBL, "Productivity"),
    HabitIconPreset("tasks", "Checklist", Icons.Default.CheckCircle, 0xFF10B981L, "Productivity"),
    HabitIconPreset("finance", "Save Money", Icons.Default.Savings, 0xFFEAB308L, "Productivity"),
    HabitIconPreset("cleaning", "Home / Clean", Icons.Default.CleaningServices, 0xFF7C3AEDL, "Productivity"),
    HabitIconPreset("spark", "Self Growth", Icons.Default.AutoAwesome, 0xFFA855F7L, "Productivity")
)

private val presetByKey = HabitIconCatalog.associateBy { it.key }

fun findHabitIconPreset(key: String): HabitIconPreset? = presetByKey[key]

@Composable
fun habitIconAndColor(habit: HabitEntity, category: CategoryEntity?): Pair<ImageVector, Color> {
    val preset = if (habit.iconKey.isNotBlank()) presetByKey[habit.iconKey] else null
    val icon = preset?.icon ?: categoryIconFor(category?.iconKey ?: "")
    val color = when {
        habit.colorHex != 0L -> Color(habit.colorHex)
        preset != null -> Color(preset.colorHex)
        category != null -> Color(category.colorHex)
        else -> MaterialTheme.colorScheme.primary
    }
    return icon to color
}
