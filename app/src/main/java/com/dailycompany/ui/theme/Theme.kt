package com.dailycompany.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.dailycompany.ui.theme.languages.EnStrings
import com.dailycompany.ui.theme.languages.PtBrStrings
import com.dailycompany.ui.theme.languages.PtPtStrings
import java.util.Locale

enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val LightScheme = lightColorScheme(
    primary = md_light_primary, onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer, onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary, onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer, onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary, onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer, onTertiaryContainer = md_light_onTertiaryContainer,
    error = md_light_error, onError = md_light_onError,
    errorContainer = md_light_errorContainer, onErrorContainer = md_light_onErrorContainer,
    background = md_light_background, onBackground = md_light_onBackground,
    surface = md_light_surface, onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant, onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline, outlineVariant = md_light_outlineVariant,
    surfaceContainer = md_light_surfaceContainer, surfaceContainerHigh = md_light_surfaceContainerHigh,
    inverseSurface = md_light_inverseSurface, inverseOnSurface = md_light_inverseOnSurface,
    scrim = md_light_scrim,
)

private val DarkScheme = darkColorScheme(
    primary = md_dark_primary, onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer, onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary, onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer, onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary, onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer, onTertiaryContainer = md_dark_onTertiaryContainer,
    error = md_dark_error, onError = md_dark_onError,
    errorContainer = md_dark_errorContainer, onErrorContainer = md_dark_onErrorContainer,
    background = md_dark_background, onBackground = md_dark_onBackground,
    surface = md_dark_surface, onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant, onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline, outlineVariant = md_dark_outlineVariant,
    surfaceContainer = md_dark_surfaceContainer, surfaceContainerHigh = md_dark_surfaceContainerHigh,
    inverseSurface = md_dark_inverseSurface, inverseOnSurface = md_dark_inverseOnSurface,
    scrim = md_dark_scrim,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalSpacing = staticCompositionLocalOf { Spacing }
val LocalAppLocale = staticCompositionLocalOf { Locale.forLanguageTag(AppLanguage.DEFAULT.tag) }

/** Línguas suportadas pela app. Os nomes visíveis vêm de `AppStrings` (rule: no text in theme). */
enum class AppLanguage(val tag: String) {
    PT_PT("pt-PT"),
    PT_BR("pt-BR"),
    EN("en");

    companion object {
        val DEFAULT = PT_PT

        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: DEFAULT

        fun fromSystem(): AppLanguage {
            val locale = Locale.getDefault()
            return when {
                locale.language != "pt" -> EN
                locale.country.equals("BR", ignoreCase = true) -> PT_BR
                else -> PT_PT
            }
        }
    }
}

fun stringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.PT_PT -> PtPtStrings
    AppLanguage.PT_BR -> PtBrStrings
    AppLanguage.EN -> EnStrings
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { PtPtStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.DEFAULT }

/** Atalho: `val s = strings()` no topo de um composable. */
@Composable
fun strings(): AppStrings = LocalStrings.current

/**
 * Tema da app: cor, tipografia, formas, língua e locale, tudo num sítio.
 */
@Composable
fun DailyCompanyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    language: AppLanguage = AppLanguage.DEFAULT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val locale = remember(language) { Locale.forLanguageTag(language.tag) }
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(configuration, locale) {
        android.content.res.Configuration(configuration).apply { setLocale(locale) }
    }

    // Do NOT override LocalContext here: hiltViewModel() needs the activity context
    // (createConfigurationContext would return an unwrappable ContextImpl and crash).
    // Locale reaches composables via LocalConfiguration and LocalAppLocale.
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalSpacing provides Spacing,
        LocalStrings provides stringsFor(language),
        LocalAppLanguage provides language,
        LocalAppLocale provides locale,
        LocalConfiguration provides localizedConfiguration,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/**
 * Todas as strings visíveis da app.
 *
 * Regra: qualquer chave nova tem de ser implementada nos três objetos
 * (PtPtStrings, PtBrStrings, EnStrings) — o compilador obriga.
 */
interface AppStrings {

    // ---- Identidade / navegação ----
    val appName: String
    val navToday: String
    val navTasks: String
    val navEvents: String
    val navWardrobe: String
    val navOutfits: String

    // ---- Ações comuns ----
    val actionSave: String
    val actionCancel: String
    val actionDelete: String
    val actionEdit: String
    val actionAdd: String
    val actionUndo: String
    val actionRetry: String
    val actionClose: String
    val actionConfirm: String
    val actionDiscard: String
    val actionRename: String
    val actionDuplicate: String
    val actionSearch: String
    val actionClearFilters: String
    val actionChoosePhoto: String
    val actionTakePhoto: String
    val actionMore: String

    // ---- Hoje ----
    val todayGreetingMorning: String
    val todayGreetingAfternoon: String
    val todayGreetingEvening: String
    val todayOutfitCardTitle: String
    val todayNoTasks: String
    val todayNoEvents: String
    val todayEmpty: String
    val todayUseOutfit: String
    val todayAnotherOutfit: String

    // ---- Tarefas ----
    val tasksTitle: String
    val tasksTabToday: String
    val tasksTabUpcoming: String
    val tasksTabNoDate: String
    val tasksTabDone: String
    val tasksNewTitle: String
    val tasksEditTitle: String
    val tasksQuickAddHint: String
    val tasksFieldTitle: String
    val tasksFieldNote: String
    val tasksFieldDate: String
    val tasksFieldTime: String
    val tasksFieldPriority: String
    val tasksFieldIcon: String
    val tasksFieldRepeat: String
    val tasksPriorityNone: String
    val tasksPriorityMedium: String
    val tasksPriorityHigh: String
    val tasksIconNone: String
    val tasksIconTabIcons: String
    val tasksIconTabEmojis: String
    val tasksEmojiHint: String
    val tasksOverdue: String
    val tasksPostponeTomorrow: String
    val tasksEmpty: String
    val tasksEmptyToday: String
    val tasksEmptyUpcoming: String
    val tasksEmptyNoDate: String
    val tasksEmptyDone: String
    val tasksConclude: String
    val tasksReopen: String
    val tasksConcluded: String
    val tasksReopened: String
    val tasksPostponed: String
    val tasksDeleted: String
    fun tasksCount(n: Int): String

    // ---- Eventos ----
    val eventsTitle: String
    val eventsNewTitle: String
    val eventsFieldTitle: String
    val eventsFieldAllDay: String
    val eventsFieldLocation: String
    val eventsFieldNotes: String
    val eventsFieldReminder: String
    val eventsEmpty: String
    val eventsViewList: String
    val eventsViewCalendar: String
    val recurrenceNever: String
    val recurrenceDaily: String
    val recurrenceWeekdays: String
    val recurrenceWeekly: String
    val recurrenceMonthly: String
    val recurrenceYearly: String
    val recurrenceCustom: String
    val recurrenceEndNever: String
    val recurrenceEndOnDate: String
    val recurrenceEndAfterCount: String
    val recurrenceEditThisOne: String
    val recurrenceEditAll: String
    fun eventTurnsYears(years: Int): String

    // ---- Alarmes / notificações ----
    val alarmsTitle: String
    val alarmsNewTitle: String
    val alarmsRepeat: String
    val alarmsEmpty: String
    val notificationChannelAlarms: String
    val notificationChannelReminders: String
    val notificationChannelDailyBrief: String
    val notificationActionDone: String
    val notificationActionSnooze: String
    val permissionNotificationsRationale: String
    val permissionExactAlarmRationale: String
    val permissionBatteryHelp: String

    // ---- Guarda-roupa ----
    val wardrobeTitle: String
    val wardrobeAddItem: String
    val wardrobeEditItem: String
    val wardrobeFieldName: String
    val wardrobeFieldCategories: String
    val wardrobeFieldSize: String
    val wardrobeFieldBrand: String
    val wardrobeFieldColors: String
    val wardrobeFieldSeasons: String
    val wardrobeFieldNotes: String
    val wardrobeFavorite: String
    val wardrobeInLaundry: String
    val wardrobeAvailableOnly: String
    val wardrobeEmpty: String
    val wardrobeEmptyFiltered: String
    val wardrobeManageCategories: String
    val sizeSystemLetter: String
    val sizeSystemNumeric: String
    val sizeSystemShoe: String
    val sizeSystemWaistLength: String
    val sizeSystemOneSize: String
    val sizeSystemCustom: String
    fun wardrobeItemsCount(n: Int): String
    fun wardrobeWornTimes(n: Int): String
    fun wardrobeItemUsedInOutfits(n: Int): String

    // ---- Slots ----
    val slotTop: String
    val slotBottom: String
    val slotOnePiece: String
    val slotOuterwear: String
    val slotShoes: String
    val slotAccessory: String

    // ---- Outfits ----
    val outfitsTitle: String
    val outfitsRandomButton: String
    val outfitsBuilderTitle: String
    val outfitsSaveDialogTitle: String
    val outfitsNameHint: String
    val outfitsEmpty: String
    val outfitsLock: String
    val outfitsUnlock: String
    val outfitsFilters: String
    val outfitsIncludeOuterwearAuto: String
    val outfitsIncludeOuterwearAlways: String
    val outfitsIncludeOuterwearNever: String
    val outfitsUseWeather: String
    val outfitsFavoritesOnly: String
    val reasonRain: String
    val reasonCold: String
    val reasonWarm: String
    val reasonFormal: String
    val reasonFavorite: String
    fun outfitsMissingSlots(slots: String): String
    fun outfitsSaved(name: String): String

    // ---- Definições ----
    val settingsTitle: String
    val settingsAppearance: String
    val settingsTheme: String
    val settingsThemeLight: String
    val settingsThemeDark: String
    val settingsThemeSystem: String
    val settingsDynamicColor: String
    val settingsLanguage: String
    val settingsLanguageSystem: String
    val settingsLanguagePtPt: String
    val settingsLanguagePtBr: String
    val settingsLanguageEn: String
    val settingsDay: String
    val settingsDailyBriefTime: String
    val settingsFirstDayOfWeek: String
    val settingsOutfits: String
    val settingsAvoidRepeatDays: String
    val settingsData: String
    val settingsAbout: String

    // ---- Estados genéricos ----
    val stateLoading: String
    val stateErrorGeneric: String
    val stateNoResults: String
    val confirmDiscardChanges: String
    val relativeToday: String
    val relativeTomorrow: String
    val relativeYesterday: String
    fun relativeInDays(n: Int): String
}