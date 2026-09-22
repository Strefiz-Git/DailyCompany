package com.dailycompany.data.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
)

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language_tag")
        val FOLLOW_SYSTEM_LANGUAGE = booleanPreferencesKey("follow_system_language")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
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
        )
    }

    /** Leitura única, para código que não é Compose (notificações, workers, receivers). */
    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }

    suspend fun setLanguage(language: AppLanguage) = context.dataStore.edit {
        it[Keys.LANGUAGE] = language.tag
        it[Keys.FOLLOW_SYSTEM_LANGUAGE] = false
    }

    suspend fun setFollowSystemLanguage() =
        context.dataStore.edit { it[Keys.FOLLOW_SYSTEM_LANGUAGE] = true }

    suspend fun setDynamicColor(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
}

/**
 * Acesso às strings fora do Compose: notificações, canais, workers, receivers.
 */
@Singleton
class StringsProvider @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend fun current(): AppStrings = stringsFor(settingsRepository.current().language)
}