package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "life_os_prefs")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DAY_CUTOFF_HOUR = intPreferencesKey("day_cutoff_hour")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val SCREEN_SECURITY = booleanPreferencesKey("screen_security")
        val STRESS_REMINDER_ENABLED = booleanPreferencesKey("stress_reminder_enabled")
        val STRESS_REMINDER_HOUR = intPreferencesKey("stress_reminder_hour")
        val STRESS_REMINDER_MINUTE = intPreferencesKey("stress_reminder_minute")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val dayCutoffHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAY_CUTOFF_HOUR] ?: 3
    }

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[Keys.PIN_HASH].isNullOrBlank()
    }

    val pinCredentials: Flow<Pair<String, String>?> = context.dataStore.data.map { prefs ->
        val hash = prefs[Keys.PIN_HASH]
        val salt = prefs[Keys.PIN_SALT]
        if (!hash.isNullOrBlank() && !salt.isNullOrBlank()) hash to salt else null
    }

    val screenSecurityEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SCREEN_SECURITY] ?: false
    }

    val stressReminderConfig: Flow<Triple<Boolean, Int, Int>> = context.dataStore.data.map { prefs ->
        Triple(
            prefs[Keys.STRESS_REMINDER_ENABLED] ?: true,
            prefs[Keys.STRESS_REMINDER_HOUR] ?: 21,
            prefs[Keys.STRESS_REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDayCutoffHour(hour: Int) {
        context.dataStore.edit { it[Keys.DAY_CUTOFF_HOUR] = hour.coerceIn(0, 5) }
    }

    suspend fun setPin(pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = hashPin(pin, salt)
        context.dataStore.edit {
            it[Keys.PIN_SALT] = salt
            it[Keys.PIN_HASH] = hash
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit {
            it.remove(Keys.PIN_SALT)
            it.remove(Keys.PIN_HASH)
        }
    }

    suspend fun setScreenSecurity(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCREEN_SECURITY] = enabled }
    }

    suspend fun setStressReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.STRESS_REMINDER_ENABLED] = enabled
            it[Keys.STRESS_REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.STRESS_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    companion object {
        fun hashPin(pin: String, salt: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
