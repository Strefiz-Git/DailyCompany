package com.dailycompany.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.dailycompany.ui.theme.languages.EnStrings
import com.dailycompany.ui.theme.languages.PtBrStrings
import com.dailycompany.ui.theme.languages.PtPtStrings
import java.util.Locale

/** Línguas suportadas pela app. Adicionar uma língua = novo valor aqui + novo objeto em `languages/`. */
enum class AppLanguage(val tag: String, val label: String) {
    PT_PT("pt-PT", "Português (Portugal)"),
    PT_BR("pt-BR", "Português (Brasil)"),
    EN("en", "English");

    companion object {
        val DEFAULT = PT_PT

        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: DEFAULT

        /** Usado quando o utilizador escolhe "Seguir o sistema". */
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
 * Todas as strings visíveis da app.
 *
 * Regra: qualquer chave nova adicionada aqui TEM de ser implementada nos três objetos
 * (PtPtStrings, PtBrStrings, EnStrings) — o compilador obriga, e é essa a vantagem
 * de usar uma interface em vez de strings.xml.
 *
 * Contagens são funções (plurais). Textos com valores usam parâmetros, nunca concatenação.
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
    val tasksFieldTitle: String
    val tasksFieldNote: String
    val tasksFieldDate: String
    val tasksFieldTime: String
    val tasksFieldPriority: String
    val tasksPriorityNone: String
    val tasksPriorityMedium: String
    val tasksPriorityHigh: String
    val tasksOverdue: String
    val tasksPostponeTomorrow: String
    val tasksEmpty: String
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
    val settingsDay: String
    val settingsDailyBriefTime: String
    val settingsFirstDayOfWeek: String
    val settingsOutfits: String
    val settingsAvoidRepeatDays: String
    val settingsData: String
    val settingsAbout: String

    // ---- Backup ----
    val backupTitle: String
    val backupExport: String
    val backupImport: String
    val backupExportDescription: String
    val backupImportDescription: String
    val backupStrategyTitle: String
    val backupStrategyMerge: String
    val backupStrategyMergeDescription: String
    val backupStrategyReplace: String
    val backupStrategyReplaceDescription: String
    val backupReplaceConfirm: String
    val backupInProgress: String
    val backupInvalidFile: String
    val backupUnsupportedVersion: String
    val backupFailed: String
    fun backupExportDone(items: Int, outfits: Int, tasks: Int, events: Int): String
    fun backupImportDone(items: Int, outfits: Int, tasks: Int, events: Int): String
    fun backupMissingImages(n: Int): String

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
