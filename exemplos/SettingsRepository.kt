package com.dailycompany.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dailycompany.data.backup.BackupSettings
import com.dailycompany.data.backup.BackupSettingsSource
import com.dailycompany.ui.theme.AppLanguage
import com.dailycompany.ui.theme.AppStrings
import com.dailycompany.ui.theme.ThemeMode
import com.dailycompany.ui.theme.stringsFor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.DEFAULT,
    val followSystemLanguage: Boolean = true,
    val dynamicColor: Boolean = false,
    val dailyBriefTimeMinutes: Int? = null,
    val avoidRepeatDays: Int = 4,
    val includeOuterwear: String = "AUTO",
    val weatherCity: String? = null,
    val weatherEnabled: Boolean = false,
)

/**
 * Implementa BackupSettingsSource: é este repositório que faz o backup "ter cara" no
 * telemóvel novo — tema, língua e preferências de outfit voltam a ser as mesmas.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : BackupSettingsSource {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language_tag")
        val FOLLOW_SYSTEM_LANGUAGE = booleanPreferencesKey("follow_system_language")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DAILY_BRIEF_TIME = intPreferencesKey("daily_brief_time_minutes")
        val AVOID_REPEAT_DAYS = intPreferencesKey("avoid_repeat_days")
        val INCLUDE_OUTERWEAR = stringPreferencesKey("include_outerwear")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_ENABLED = booleanPreferencesKey("weather_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toSettings() }

    private fun Preferences.toSettings(): AppSettings {
        val followSystem = this[Keys.FOLLOW_SYSTEM_LANGUAGE] ?: true
        return AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(this[Keys.THEME] ?: "") }
                .getOrDefault(ThemeMode.SYSTEM),
            language = if (followSystem) AppLanguage.fromSystem()
            else AppLanguage.fromTag(this[Keys.LANGUAGE]),
            followSystemLanguage = followSystem,
            dynamicColor = this[Keys.DYNAMIC_COLOR] ?: false,
            dailyBriefTimeMinutes = this[Keys.DAILY_BRIEF_TIME],
            avoidRepeatDays = this[Keys.AVOID_REPEAT_DAYS] ?: 4,
            includeOuterwear = this[Keys.INCLUDE_OUTERWEAR] ?: "AUTO",
            weatherCity = this[Keys.WEATHER_CITY],
            weatherEnabled = this[Keys.WEATHER_ENABLED] ?: false,
        )
    }

    /** Leitura única, para código que não é Compose (notificações, workers, receivers). */
    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }.let { }

    suspend fun setLanguage(language: AppLanguage) = context.dataStore.edit {
        it[Keys.LANGUAGE] = language.tag
        it[Keys.FOLLOW_SYSTEM_LANGUAGE] = false
    }.let { }

    suspend fun setFollowSystemLanguage() =
        context.dataStore.edit { it[Keys.FOLLOW_SYSTEM_LANGUAGE] = true }.let { }

    suspend fun setDynamicColor(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }.let { }

    suspend fun setDailyBriefTime(minutesFromMidnight: Int?) = context.dataStore.edit {
        if (minutesFromMidnight == null) it.remove(Keys.DAILY_BRIEF_TIME)
        else it[Keys.DAILY_BRIEF_TIME] = minutesFromMidnight
    }.let { }

    suspend fun setAvoidRepeatDays(days: Int) =
        context.dataStore.edit { it[Keys.AVOID_REPEAT_DAYS] = days }.let { }

    suspend fun setIncludeOuterwear(mode: String) =
        context.dataStore.edit { it[Keys.INCLUDE_OUTERWEAR] = mode }.let { }

    suspend fun setWeather(city: String?, enabled: Boolean) = context.dataStore.edit {
        if (city == null) it.remove(Keys.WEATHER_CITY) else it[Keys.WEATHER_CITY] = city
        it[Keys.WEATHER_ENABLED] = enabled
    }.let { }

    // ---------------------------------------------------------------- BackupSettingsSource

    override suspend fun exportSettings(): BackupSettings {
        val s = current()
        return BackupSettings(
            themeMode = s.themeMode.name,
            language = if (s.followSystemLanguage) null else s.language.tag,
            followSystemLanguage = s.followSystemLanguage,
            dynamicColor = s.dynamicColor,
            dailyBriefTimeMinutes = s.dailyBriefTimeMinutes,
            firstDayOfWeek = "MONDAY",
            avoidRepeatDays = s.avoidRepeatDays,
            includeOuterwear = s.includeOuterwear,
            weatherCity = s.weatherCity,
            weatherEnabled = s.weatherEnabled,
        )
    }

    override suspend fun importSettings(settings: BackupSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = runCatching { ThemeMode.valueOf(settings.themeMode) }
                .getOrDefault(ThemeMode.SYSTEM).name
            prefs[Keys.FOLLOW_SYSTEM_LANGUAGE] = settings.followSystemLanguage
            if (!settings.followSystemLanguage && settings.language != null) {
                prefs[Keys.LANGUAGE] = settings.language
            }
            prefs[Keys.DYNAMIC_COLOR] = settings.dynamicColor
            settings.dailyBriefTimeMinutes?.let { prefs[Keys.DAILY_BRIEF_TIME] = it }
            prefs[Keys.AVOID_REPEAT_DAYS] = settings.avoidRepeatDays
            prefs[Keys.INCLUDE_OUTERWEAR] = settings.includeOuterwear
            settings.weatherCity?.let { prefs[Keys.WEATHER_CITY] = it }
            prefs[Keys.WEATHER_ENABLED] = settings.weatherEnabled
        }
    }
}

/**
 * Acesso às strings fora do Compose: notificações, canais, workers, receivers.
 * Nunca uses PtPtStrings diretamente nesses sítios — a língua é a que o utilizador escolheu.
 */
@Singleton
class StringsProvider @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun current(): AppStrings = stringsFor(settingsRepository.current().language)
}