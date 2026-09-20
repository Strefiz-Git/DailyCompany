---
name: android-recurring-events-dates
description: Model and compute dates, times, time zones and recurring events in Kotlin Android apps using java.time - one-off and repeating events (daily, weekly on chosen weekdays, monthly, yearly such as birthdays), end dates and counts, skipped or edited single occurrences, next-occurrence calculation, DST and leap-year edge cases, storage in Room, and date/time pickers and formatting for PT-PT. Use this skill whenever the task involves calendars, events on specific dates, "repeat every...", birthdays, anniversaries, schedules, agenda by day, next occurrence, or date/time formatting and parsing.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, java-time, room, material3-datepicker
---

# Dates, time zones and recurring events

Use together with `android-data-offline` (Room) and `android-alarms-notifications` (firing).

## Setup

- Use `java.time` only (`LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `ZoneId`, `DayOfWeek`, `Month`, `YearMonth`). Never `java.util.Date` / `Calendar` / `SimpleDateFormat` in new code.
- If `minSdk` < 26, enable **core library desugaring**: `compileOptions { isCoreLibraryDesugaringEnabled = true }` and `coreLibraryDesugaring(libs.desugar.jdk.libs)` (add it to the version catalog; do not guess the version).
- Inject a `Clock` (or a `TimeProvider`) instead of calling `LocalDate.now()` in logic, so tests are deterministic.

## Which type for what

| Meaning | Type |
|---|---|
| Birthday, anniversary, all-day event | `LocalDate` (no time, no zone) |
| "Alarm at 07:30 every day, wherever I am" | `LocalTime` + recurrence (wall-clock/floating) |
| Appointment at a real instant (flight, meeting in a specific city) | `ZonedDateTime` / `Instant` + `ZoneId` |
| When something happened (created, completed) | `Instant` (UTC epoch millis) |

Decide and document per field whether it is floating or fixed. Default for personal reminders: **floating local time**.

## Storing in Room

Keep it simple and queryable:

- `LocalDate` -> `Long` epoch day (`toEpochDay()`) or ISO string (`yyyy-MM-dd`); `LocalTime` -> `Int` seconds of day or ISO string (`HH:mm`); `Instant` -> `Long` epoch millis; enums -> string.
- Provide `TypeConverter`s in one place. Prefer ISO strings if readability matters, epoch numbers if you need range queries (`WHERE date BETWEEN :from AND :to`).

## Recurrence model

Do **not** store one row per future occurrence. Store the rule and **expand on demand** for a date range.

```kotlin
enum class Frequency { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

data class Recurrence(
    val frequency: Frequency = Frequency.NONE,
    val interval: Int = 1,                       // every N days/weeks/months/years
    val daysOfWeek: Set<DayOfWeek> = emptySet(), // for WEEKLY
    val until: LocalDate? = null,                // optional end date
    val count: Int? = null,                      // optional max occurrences
)

data class Event(
    val id: Long,
    val title: String,
    val startDate: LocalDate,
    val time: LocalTime?,          // null = all day
    val recurrence: Recurrence,
    val yearlyIgnoreYear: Boolean, // birthdays: show age = year - startDate.year
)
```

- **Expansion function** (pure, unit-tested): `fun Event.occurrencesBetween(from: LocalDate, to: LocalDate): List<LocalDate>`. Never expand infinitely; always bound by a range (for example one month for the calendar, 30-60 days for alarm scheduling).
- **Next occurrence:** `fun Event.nextOccurrenceAfter(date: LocalDate): LocalDate?` returns null if the event has ended.
- **Exceptions:** store per-occurrence overrides in a separate table (`eventId`, `originalDate`, `isCancelled`, optional `newDate/newTime/newTitle`). "Delete only this occurrence" = insert an exception; "delete all" = delete the event; "this and following" = set `until` on the old event and create a new one.
- Keep an `isDone`/`completedDates` set for recurring tasks so completing today does not complete every day.
- A recurrence with `count` needs counting from `startDate`; test it.

## Edge cases (write unit tests for each)

- **Monthly on day 31/30/29:** months without that day -> use the last day of the month (documented choice; make it consistent), via `YearMonth.atEndOfMonth()` clamp.
- **Yearly on Feb 29:** in non-leap years fall back to Feb 28 (or Mar 1 - pick one, document, test).
- **Weekly with several weekdays:** iterate the week containing the date; respect `interval` (every 2 weeks) counting from the week of `startDate`.
- **DST:** convert local date+time with `ZonedDateTime.of(date, time, zone)`; a non-existent time (spring forward) shifts forward automatically, an ambiguous time (fall back) picks the earlier offset. Do not add 24 hours to an `Instant` to get "tomorrow same time" - add days to the `LocalDate`.
- **Time zone change of the device:** floating events keep the wall-clock time; recompute the trigger instants.
- **Week start:** use `WeekFields.of(Locale.getDefault())` (Portugal: Monday) instead of assuming Sunday.
- **Leap seconds / year boundaries:** operate on `LocalDate`, not on millisecond arithmetic.

## Formatting and UI (PT-PT)

- Format with `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)` / `ofPattern("EEEE, d 'de' MMMM", Locale("pt", "PT"))` so month/day names are Portuguese. Use `DateFormat.is24HourFormat(context)` for the time picker mode.
- Pickers: Material 3 `DatePicker` / `TimePicker` (`DatePickerDialog`, `rememberDatePickerState`, `rememberTimePickerState`). Note that `DatePickerState` returns **UTC epoch millis at midnight**; convert with `Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()` to avoid off-by-one-day bugs.
- Show relative info where helpful: "hoje", "amanha", "em 3 dias", and the age for yearly events ("faz 25 anos").
- Put all user-visible strings in `strings.xml` (`values-pt-rPT` if the default is another language).

## Checklist

- [ ] `Clock` injected; no `now()` inside pure logic.
- [ ] Rule stored, occurrences expanded on demand within a range.
- [ ] Unit tests for: daily/weekly/monthly/yearly, interval > 1, `until`, `count`, Feb 29, day 31, DST gap/overlap, exceptions.
- [ ] Date pickers converted correctly from UTC millis.
- [ ] Alarm scheduling uses only upcoming occurrences within a bounded window.
