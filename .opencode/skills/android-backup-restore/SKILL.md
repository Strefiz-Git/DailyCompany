---
name: android-backup-restore
description: Export and import EVERYTHING of an offline Kotlin Jetpack Compose Android app in one zip - all domain data (wardrobe, outfits, tasks, events, reminders, wear history) plus app settings (theme, language, preferences) plus photos, so a new phone ends up identical to the old one. Storage Access Framework pickers, versioned schema, merge or replace strategies, uid-based deduplication, rescheduling reminders after import, Auto Backup configuration, and integrity checks. Use this skill whenever the task involves backup, export, import, restore, moving data to a new phone, losing data, or sharing the database, settings and photos.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, room, kotlinx-serialization, saf
---

# Backup and restore

Use with `android-data-offline`, `android-images-media`, `android-i18n-theme`. Everything is local: no cloud, no account. The user chooses where the file goes.

**A backup is the whole phone, not just the photos.** There is no partial backup and no separate "settings export" — one file, one action, everything: wardrobe, outfits, tasks, events, reminders, wear history, app settings (theme, language, outfit preferences) and photos. If a new data type or setting is added to the app, it MUST be added to `BackupData`/`BackupSettings` in the same commit — an item left out of the backup is data the user silently loses when they switch phones.

## Format

One `.zip` written by the app, named `dailycompany-backup-YYYY-MM-DD-HHmm.zip`:

```
manifest.json      # { "app": "DailyCompany", "schemaVersion": 1, "appVersion": "1.2.0", "createdAt": "...", "counts": {...} }
data.json          # BackupData: ALL domain entities (grouped by type) + BackupSettings (theme, language, outfit prefs)
images/<uuid>.jpg  # originals and thumbnails referenced by data.json
```

`data.json` has two parts, always exported and imported together as one unit:
- **Domain data** — categories, items, outfits, tasks, events, reminders, wear logs. Owned by `BackupRepository` (Room).
- **App settings** — `BackupSettings`: theme mode, language (or "follow system"), dynamic color, daily brief time, first day of week, avoid-repeat days, include-outerwear mode, weather city/enabled. Owned by `SettingsRepository` (DataStore) via the `BackupSettingsSource` interface. Serialized as plain strings/ints, never as the app's own enums directly — an enum rename must not break old backup files.

Settings are **always overwritten on import**, in both Merge and Replace strategies — unlike domain rows there is no per-row uid to merge against; the imported file is treated as "this is what the phone I'm restoring from looked like".

Rules:
- Serialize **domain-level DTOs**, not Room entities, so refactors do not break old backups. Use `kotlinx.serialization` with explicit `@SerialName` and defaults on every optional field.
- Every exportable row carries a stable `uid` (UUID). Foreign keys in the file reference **uids**, never auto-increment ids.
- Dates as ISO-8601 strings or epoch millis — pick one and document it in `manifest.json`.
- `schemaVersion` is bumped whenever the shape changes; the importer must handle every older version or refuse clearly.

## Export flow

1. `ActivityResultContracts.CreateDocument("application/zip")` with a suggested name.
2. Run in a `CoroutineWorker` (WorkManager) or a ViewModel scope with a progress state; never on the main thread.
3. Read all DAOs inside a single read transaction, map to DTOs (`BackupRepository.exportData()`), **then also read current settings** (`BackupSettingsSource.exportSettings()`) and attach them to the same `BackupData`. Write `data.json` into a `ZipOutputStream` opened over `contentResolver.openOutputStream(uri)`.
4. Stream each referenced image file into `images/`; skip missing files and collect a warning list.
5. Show a result message with counts ("Guardado: 124 peças, 38 outfits, 57 eventos, definições incluídas") and any warnings.

## Import flow

1. `ActivityResultContracts.OpenDocument(arrayOf("application/zip"))`.
2. **Validate first, write second:** open the zip, read `manifest.json`, check the app name and `schemaVersion`; parse `data.json` fully into memory (or to a temp file) before touching the database. Reject with a clear PT-PT message if invalid or newer than supported.
3. Ask the user for a strategy in a dialog:
   - **Substituir tudo** — wipe tables and image dir, then insert. Ask for explicit confirmation.
   - **Juntar** (default) — insert rows whose `uid` is unknown; for known `uids`, keep the newer `updatedAt` (or keep local and report conflicts).
4. Import inside **one Room transaction** per phase: categories → items → item/category links → outfits → outfit links → tasks → events → reminders → wear logs. Map uid → new local id as you go.
5. Extract images to a temp dir, then move into `filesDir/images/`; guard against Zip Slip (reject entry names containing `..` or absolute paths) and against absurd sizes.
6. **Apply `data.settings` via `BackupSettingsSource.importSettings()`** — always, regardless of Merge/Replace, and after the domain-data transaction succeeds. This is what makes the restored phone look and behave like the old one, not just contain the same rows.
7. After a successful import: **cancel and reschedule all reminders and alarms** (see `android-alarms-notifications`), invalidate image caches, and run orphan cleanup.
8. On any failure, roll back the domain-data transaction, leave settings untouched, and delete the temp files; the app must be left exactly as before.

## Auto Backup (Android system)

- Keep `android:allowBackup="true"` only with an explicit `data_extraction_rules.xml` (and `full_backup_content` for older APIs) that **includes the database and `files/images`** and **excludes** caches, temp dirs and anything sensitive.
- Say clearly in the UI that system backup depends on the user's Google settings and is not a substitute for the manual export.

## Tests (required)

- **Full round-trip**: export a populated database *and* customized settings (dark theme, English, non-default avoid-repeat-days), wipe both, import, assert domain rows, relations, AND settings are all identical to before.
- Merge: importing the same file twice creates no duplicate domain rows (uid dedup) but settings still get reapplied both times.
- Corrupt/truncated zip, missing `manifest.json`, unknown `schemaVersion`, entry with `../` path → clean failure, no partial writes, settings untouched.
- Missing image file referenced by `data.json` → row imported, warning reported.
- Old `schemaVersion` fixture kept in `src/test/resources/` and imported in a test — including one written before `BackupSettings` existed, to prove the default `BackupSettings()` keeps it importable.

## Checklist

- [ ] Backup includes **domain data, settings, and photos** in one file; opens in any zip tool.
- [ ] Every new data type or setting added to the app is added to `BackupData`/`BackupSettings` in the same commit — nothing is left "for later".
- [ ] Import validates before writing and is transactional for domain data.
- [ ] Merge is uid-based and idempotent for domain data; settings are always reapplied.
- [ ] Reminders rescheduled after restore.
- [ ] All messages come from `AppStrings` (see `android-i18n-theme`), in all three languages; progress and errors are human-readable.
- [ ] `./gradlew :app:testDebugUnitTest` reported; real export → new device → import tried, or stated as untested.