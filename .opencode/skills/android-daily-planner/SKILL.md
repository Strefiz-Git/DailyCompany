---
name: android-daily-planner
description: Design and build the core of a personal daily-planner Android app in Kotlin and Jetpack Compose - Today screen combining tasks, notes, events, reminders and suggestions into a daily guide, task and checklist data model, quick add, swipe actions with undo, home-screen widget with Jetpack Glance, local backup/export, and settings. Use this skill whenever the task involves a to-do list, notes, "what I have to do today", daily agenda/guide, habit or routine features, a dashboard of the day, widgets, or backup/restore of user data for a personal-organizer app.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, room, glance
---

# Daily planner app (tasks, notes, daily guide)

Use with `kotlin-compose-android`, `android-data-offline`, `android-recurring-events-dates`, `android-alarms-notifications`.

## Product principles

- **Local-first and private:** all data stays on the device by default. No account, no server unless the user asks.
- **Fast capture:** adding a task or note must take 1-2 taps (FAB or bottom-sheet quick add with a text field, optional time).
- **The Today screen is the home screen.** Everything else supports it.
- **Never lose user data:** confirm destructive actions or offer **Undo** via Snackbar; make backups easy.

## Suggested modules and screens

```
ui/today/        TodayScreen (the daily guide)
ui/tasks/        TaskList, TaskEditor
ui/notes/        NoteList, NoteEditor   # fora de âmbito neste projeto: não há notas (PROJECT.md §5)
ui/calendar/     MonthView + DayAgenda (events; uses recurrence skill)
ui/wardrobe/     Outfit suggestion (see android-wardrobe-outfits)
ui/settings/     Notifications, theme, backup, defaults
```

Navigation: in this project the bottom bar has 5 destinations (today, tasks, events, wardrobe, outfits) plus full-screen editors — see `SCREENS.md` §1. Use Navigation Compose type-safe routes; never invent top-level destinations (PROJECT.md rule 12).

## Data model (Room)

```kotlin
@Entity data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String? = null,
    val dueDate: Long? = null,       // epoch day, null = no date
    val dueTimeSec: Int? = null,     // seconds of day, null = no time
    val priority: Int = 0,           // 0 none, 1 low, 2 medium, 3 high
    val isDone: Boolean = false,
    val completedAt: Long? = null,   // epoch millis
    val sortOrder: Int = 0,
)

@Entity data class ChecklistItemEntity(   // sub-items of a task
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long, val text: String, val isDone: Boolean = false, val position: Int = 0,
)

// Notas NÃO existem neste projeto (PROJECT.md §5 — fora de âmbito). Não criar.
@Entity data class NoteEntity(   // template genérico de outro projeto — ignorar no DailyCompany
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val body: String,
    val updatedAt: Long, val isPinned: Boolean = false,
)
```

- Events and recurrence come from `android-recurring-events-dates`. Routines ("every weekday at 08:00: take vitamins") are tasks/reminders with a `Recurrence`.
- Soft-delete or Undo buffer for tasks; hard-delete only after the Snackbar times out.

## Today screen (the daily guide)

Build one `TodayUseCase` that **combines flows** into a single `TodayUiState`:

```kotlin
data class TodayUiState(
    val date: LocalDate,
    val greeting: String,                 // "Bom dia" / "Boa tarde" by hour
    val timeline: List<TodayItem>,        // events + timed tasks + reminders sorted by time
    val anytimeTasks: List<Task>,         // tasks for today without a time
    val overdue: List<Task>,
    val weather: WeatherSummary?,         // optional, may be null offline
    val outfit: OutfitSuggestion?,        // optional
    val isLoading: Boolean,
)
```

- `combine(tasksFlow, eventsForTodayFlow, weatherFlow, ...)` in the ViewModel; `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Order: **next thing first** (highlight "Agora / A seguir"), then timeline, then anytime tasks, then overdue (collapsed), then outfit card.
- Empty state must be friendly and offer "Adicionar tarefa".
- Refresh at midnight: when the date changes while the app is open, the screen must roll to the new day (observe a `Clock`-based date flow, not a value computed once).

## Interaction patterns

- Checkbox toggles completion with animation; completed items move to a collapsed "Concluidas" section.
- Swipe (`SwipeToDismissBox`) to complete or delete, with Snackbar **Undo** (`SnackbarHostState.showSnackbar(..., actionLabel = "Anular")`).
- Reorder with long-press drag only if the user asks; keep `sortOrder` gaps to avoid rewriting all rows.
- Text fields: keep the value in local state, save on confirm or debounce (300-500 ms); never save on every keystroke to the database in a way that recreates the list.
- Search across tasks and notes (`LIKE` query or FTS4 if large).
- Accessibility: min 48 dp targets, content descriptions on icon buttons, no information conveyed by color alone.

## Glance widget (optional, when asked)

- Use `androidx.glance:glance-appwidget` (check the project's versions). A "Hoje" widget shows the next 3-5 items.
- Data from Room via a `GlanceAppWidget` provider; update with `GlanceAppWidget.updateAll(context)` after data changes or from a periodic worker. Keep the widget lightweight; no heavy work in composition.
- Clicking opens the app at the Today screen (`actionStartActivity`) or toggles a task with an `ActionCallback`.

## Backup and export

- Offer **Export/Import** through the Storage Access Framework. **In this project the format is one `.zip` with everything** (domain data + settings + photos) — NOT a bare JSON file. Use the `android-backup-restore` skill; a JSON-only export would violate R13.
- Serialize with `kotlinx.serialization`; include a `schemaVersion` field. Import must validate and be transactional (all or nothing) and ask before replacing existing data.
- Do not rely on Android auto-backup alone; state clearly in settings what is backed up.
- After an import, **reschedule all alarms** (see alarms skill).

## Settings

- DataStore: theme (system/light/dark), default reminder lead time, notification toggles, first day of week, 24 h clock.
- Deep-link to system notification settings for channels: `Settings.ACTION_APP_NOTIFICATION_SETTINGS`.

## Done criteria

- [ ] Add, edit, complete, delete (with undo) a task works end to end.
- [ ] Today screen updates live when data changes and when the day rolls over.
- [ ] Empty, loading and error states exist; texts via the project's `AppStrings` (three languages, see `android-i18n-theme`).
- [ ] Unit tests for `TodayUseCase` ordering and for overdue/today logic.
- [ ] `./gradlew :app:assembleDebug` and unit tests run and results reported.
