---
name: android-backup-restore
description: Export and import all user data of an offline Kotlin Jetpack Compose Android app - a single backup file (zip with JSON + photos), Storage Access Framework pickers, versioned schema, merge or replace strategies, uid-based deduplication, rescheduling reminders after import, Auto Backup configuration, and integrity checks. Use this skill whenever the task involves backup, export, import, restore, moving data to a new phone, losing data, or sharing the database and photos.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, room, kotlinx-serialization, saf
---

# Backup and restore

Use with `android-data-offline`, `android-images-media`, `android-localization-pt-pt`. Everything is local: no cloud, no account. The user chooses where the file goes.

## Format

One `.zip` written by the app, named `dailycompany-backup-YYYY-MM-DD-HHmm.zip`:

```
manifest.json      # { "app": "DailyCompany", "schemaVersion": 1, "appVersion": "1.2.0", "createdAt": "...", "counts": {...} }
data.json          # all entities, grouped by type
images/<uuid>.jpg  # originals and thumbnails referenced by data.json
```

Rules:
- Serialize **domain-level DTOs**, not Room entities, so refactors do not break old backups. Use `kotlinx.serialization` with explicit `@SerialName` and defaults on every optional field.
- Every exportable row carries a stable `uid` (UUID). Foreign keys in the file reference **uids**, never auto-increment ids.
- Dates as ISO-8601 strings or epoch millis — pick one and document it in `manifest.json`.
- `schemaVersion` is bumped whenever the shape changes; the importer must handle every older version or refuse clearly.

## Export flow

1. `ActivityResultContracts.CreateDocument("application/zip")` with a suggested name.
2. Run in a `CoroutineWorker` (WorkManager) or a ViewModel scope with a progress state; never on the main thread.
3. Read all DAOs inside a single read transaction, map to DTOs, write `data.json` into a `ZipOutputStream` opened over `contentResolver.openOutputStream(uri)`.
4. Stream each referenced image file into `images/`; skip missing files and collect a warning list.
5. Show a result message with counts ("Guardado: 124 peças, 38 outfits, 57 eventos") and any warnings.

## Import flow

1. `ActivityResultContracts.OpenDocument(arrayOf("application/zip"))`.
2. **Validate first, write second:** open the zip, read `manifest.json`, check the app name and `schemaVersion`; parse `data.json` fully into memory (or to a temp file) before touching the database. Reject with a clear PT-PT message if invalid or newer than supported.
3. Ask the user for a strategy in a dialog:
   - **Substituir tudo** — wipe tables and image dir, then insert. Ask for explicit confirmation.
   - **Juntar** (default) — insert rows whose `uid` is unknown; for known `uids`, keep the newer `updatedAt` (or keep local and report conflicts).
4. Import inside **one Room transaction** per phase: categories → items → item/category links → outfits → outfit links → tasks → events → reminders → wear logs. Map uid → new local id as you go.
5. Extract images to a temp dir, then move into `filesDir/images/`; guard against Zip Slip (reject entry names containing `..` or absolute paths) and against absurd sizes.
6. After a successful import: **cancel and reschedule all reminders and alarms** (see `android-alarms-notifications`), invalidate image caches, and run orphan cleanup.
7. On any failure, roll back the transaction and delete the temp files; the app must be left exactly as before.

## Auto Backup (Android system)

- Keep `android:allowBackup="true"` only with an explicit `data_extraction_rules.xml` (and `full_backup_content` for older APIs) that **includes the database and `files/images`** and **excludes** caches, temp dirs and anything sensitive.
- Say clearly in the UI that system backup depends on the user's Google settings and is not a substitute for the manual export.

## Tests (required)

- Round-trip: export a populated database, wipe, import, assert counts and relations are identical.
- Merge: importing the same file twice creates no duplicates (uid dedup).
- Corrupt/truncated zip, missing `manifest.json`, unknown `schemaVersion`, entry with `../` path → clean failure, no partial writes.
- Missing image file referenced by `data.json` → row imported, warning reported.
- Old `schemaVersion` fixture kept in `src/test/resources/` and imported in a test.

## Checklist

- [ ] Backup includes data **and** photos; file opens in any zip tool.
- [ ] Import validates before writing and is transactional.
- [ ] Merge is uid-based and idempotent.
- [ ] Reminders rescheduled after restore.
- [ ] All messages are PT-PT string resources; progress and errors are human-readable.
- [ ] `./gradlew :app:testDebugUnitTest` reported; real export/import tried on a device or stated as untested.
