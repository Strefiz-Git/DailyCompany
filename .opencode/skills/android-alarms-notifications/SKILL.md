---
name: android-alarms-notifications
description: Implement reliable alarms, reminders and notifications in Android apps written in Kotlin - AlarmManager exact alarms, setAlarmClock, notification channels, POST_NOTIFICATIONS permission, BroadcastReceivers, rescheduling after reboot / time change / app update, Doze and battery limits, and Android 12-14 exact-alarm permission rules. Use this skill whenever the task involves alarms at specific times, reminders, "notify me at", wake-up alarms, daily notifications, snooze, repeating reminders, or anything that must fire while the app is closed.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, alarmmanager, notifications, broadcastreceiver
---

# Android alarms and notifications

Use together with `kotlin-compose-android` and `android-recurring-events-dates` (which computes *when* the next occurrence is). This skill covers *firing it reliably*.

## Choose the right mechanism

| Need | Use |
|---|---|
| User-visible alarm clock (wake-up alarm) | `AlarmManager.setAlarmClock(...)` - most reliable, shows the alarm icon, does not need the exact-alarm permission |
| Time-critical reminder at an exact minute (calendar-style) | `setExactAndAllowWhileIdle(...)` + exact-alarm permission handling |
| "Around this time is fine" (daily summary, hourly sync) | `WorkManager` (not exact, battery friendly) |
| Repeating alarms | Schedule **only the next occurrence**; when it fires, compute and schedule the following one. Do not use `setRepeating` (inexact since API 19). |

Never use a foreground service or a wake lock just to "wait" for a time.

## Permissions (check each against the app's target and minimum SDK)

- Notifications: `POST_NOTIFICATIONS` is a **runtime permission on Android 13+**. Request it in context (for example when the user creates the first reminder), and handle denial (the app must explain that alarms will be silent/not shown).
- Exact alarms (Android 12+): `SCHEDULE_EXACT_ALARM` may be **denied by default** on newer versions. Before scheduling call `alarmManager.canScheduleExactAlarms()`; if false, send the user to `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` and fall back gracefully (inexact alarm) meanwhile.
- `USE_EXACT_ALARM` is auto-granted but Google Play restricts it to apps whose **core function is an alarm clock or calendar**. Tell the user to read the current Play policy before using it; do not add it silently.
- `RECEIVE_BOOT_COMPLETED` is required to reschedule after a reboot.
- Do not request `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` unless the user insists; it is restricted on Play. Prefer explaining OEM battery settings in the UI.
- Full-screen alarm screens (`USE_FULL_SCREEN_INTENT`) are restricted on newer Android; ask before adding and describe the limitation.

## Scheduling pattern

```kotlin
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(reminderId: Long, triggerAtMillis: Long) {
        val pending = pendingIntent(reminderId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending) // inexact fallback
        }
    }

    fun cancel(reminderId: Long) = alarmManager.cancel(pendingIntent(reminderId))

    private fun pendingIntent(id: Long) = PendingIntent.getBroadcast(
        context,
        id.toInt(),                                    // unique request code per reminder
        Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
```

- `FLAG_IMMUTABLE` is mandatory unless mutability is really needed.
- Use a **stable unique request code** per reminder so it can be updated/cancelled. Store the id in the database, not only in memory.
- The **database is the source of truth**; alarms are a cache of it. Any function that creates, edits or deletes a reminder must also (re)schedule or cancel the alarm.

## Receiver

- Keep `onReceive` short (< 10 s). For database work use `goAsync()` with a coroutine, or enqueue a `OneTimeWorkRequest`.
- On fire: load the reminder from Room, post the notification, compute the **next** occurrence, schedule it (or mark done for one-shot reminders).
- Declare receivers in the manifest with `android:exported="false"` (except the boot one, which is exported false too because the system broadcast does not need it; set explicitly).

```xml
<receiver android:name=".alarms.ReminderReceiver" android:exported="false" />
<receiver android:name=".alarms.RescheduleReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
        <action android:name="android.intent.action.TIMEZONE_CHANGED" />
        <action android:name="android.intent.action.TIME_SET" />
    </intent-filter>
</receiver>
```

`RescheduleReceiver` reads all active reminders from Room and re-schedules each one. Alarms are **wiped on reboot**, so this receiver is not optional.

## Notifications

- Create channels on app start (API 26+) with clear names: e.g. `reminders` (high importance), `daily_guide` (default). Channel settings are controlled by the user afterwards; do not recreate them with different importance and expect it to change.
- Use `NotificationCompat.Builder`, a small monochrome icon (`setSmallIcon`), a title, text and `setContentIntent` that opens the right screen (deep link to the event/task).
- Add actions where useful: **Done** and **Snooze 10 min**. Snooze = schedule a new one-shot alarm now + 10 min.
- Use a stable notification id per reminder so updates replace instead of piling up.
- Check `NotificationManagerCompat.areNotificationsEnabled()` before posting and handle permission denied.
- Text comes from the project's `AppStrings` — via `StringsProvider` outside Compose — in all three languages, never hardcoded. See `android-i18n-theme`.

## Edge cases to handle explicitly

- **Time zone / DST changes:** decide per reminder if it is "wall clock time" (07:00 wherever I am) or a fixed instant; recompute on `TIMEZONE_CHANGED`. On a DST gap (e.g. 02:30 that does not exist) shift forward; on overlap pick the first.
- **Past times:** never schedule a trigger in the past; compute the next valid occurrence instead.
- **Many alarms:** Android limits alarms per app (about 500). Schedule only the next N (for example the next 30 days of occurrences) plus a WorkManager job that tops the window up.
- **Device idle / OEM killers:** exact alarms survive Doze, but some manufacturers kill apps. Add an in-app "reliability tips" screen instead of hacks.
- **User force-stops the app:** alarms are cancelled by the system; they return when the user opens the app again (reschedule on app start too).

## Testing and verification

- Unit test the "next trigger time" logic (pure functions) - see `android-testing`.
- Manual: `adb shell dumpsys alarm | grep com.example.app` shows scheduled alarms; use `adb shell cmd alarm ...` and emulator time change to test; reboot the emulator to test rescheduling.
- Report honestly what was tested on a device and what was not. Never claim alarms "work" from compiling alone.
