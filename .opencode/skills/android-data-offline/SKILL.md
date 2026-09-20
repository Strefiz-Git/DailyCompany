---
name: android-data-offline
description: Build the data layer of Android apps in Kotlin - Room database, Retrofit + kotlinx.serialization networking, DataStore preferences, repositories, offline-first caching and background sync with WorkManager. Use this skill whenever the task involves saving data locally, calling a REST API, caching, syncing, settings persistence, DTO/entity mapping, database migrations or loading/error states coming from data sources, even if the user only says "fetch the data" or "save this on the phone".
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, room, retrofit, datastore, workmanager
---

# Android data layer (Room, Retrofit, DataStore, offline-first)

Use together with `kotlin-compose-android`. Follow the project's existing libraries and versions; never guess versions.

## Rules

1. **Three model types, kept separate:** `XxxDto` (network), `XxxEntity` (Room), `Xxx` (domain/UI). Map between them with small extension functions (`fun XxxDto.toEntity()`, `fun XxxEntity.toDomain()`). Never expose DTOs or entities to composables.
2. **Repository is the only thing the ViewModel talks to.** ViewModels never touch Retrofit or DAOs directly.
3. **Offline-first by default:** the database is the single source of truth. The UI observes a `Flow` from Room; the repository refreshes from the network and writes into Room.
4. **Never block the main thread.** DAO/network calls are `suspend` or return `Flow`. Room and Retrofit already switch threads for suspend functions; do not wrap them in `Dispatchers.IO` unnecessarily.
5. **Never swallow exceptions silently** and never crash on network errors: catch at the repository boundary and return a result the UI can show.

## Room

- Use KSP (`ksp(libs.androidx.room.compiler)`), plus `room-runtime` and `room-ktx`.
- Set `exportSchema = true` and configure the schema directory so migrations can be tested.
- DAO conventions: reads return `Flow<List<Entity>>`; writes are `suspend`; use `@Upsert` for insert-or-update; use `@Transaction` for multi-step operations.
- Any change to an entity requires: bump `version`, add a `Migration` (or `autoMigrations` when supported), and tell the user. Do not use `fallbackToDestructiveMigration()` unless the user explicitly accepts data loss.
- Store dates as `Long` (epoch millis) or use a `TypeConverter`; store enums as strings.
- Add indexes (`@Index`) on columns used in `WHERE`/`JOIN`.

```kotlin
@Dao
interface StopDao {
    @Query("SELECT * FROM stops ORDER BY name")
    fun observeAll(): Flow<List<StopEntity>>

    @Upsert
    suspend fun upsertAll(stops: List<StopEntity>)
}
```

## Networking (Retrofit + kotlinx.serialization)

- API interface with `suspend` functions returning DTOs. Base URL comes from `BuildConfig` or Gradle config, never hardcoded in code that is shared.
- Use `@Serializable` DTOs with `@SerialName` for API field names. Configure `Json { ignoreUnknownKeys = true }` so backend additions do not break the app.
- Add an OkHttp `HttpLoggingInterceptor` **only in debug builds**, and never log tokens or personal data.
- Set sensible timeouts. Use HTTPS only.
- Auth tokens: add via an OkHttp `Interceptor`; store tokens in DataStore/EncryptedSharedPreferences-equivalent, never in plain source files.

## Repository pattern

```kotlin
class StopRepository @Inject constructor(
    private val api: StopApi,
    private val dao: StopDao,
) {
    val stops: Flow<List<Stop>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Returns Result so the ViewModel can show an error without crashing. */
    suspend fun refresh(): Result<Unit> = runCatching {
        val remote = api.getStops()
        dao.upsertAll(remote.map { it.toEntity() })
    }
}
```

- Catch `CancellationException` properly: if you use `runCatching` around suspend code, rethrow `CancellationException` so coroutine cancellation still works.
- The ViewModel calls `refresh()` (init, pull-to-refresh, retry button) and collects `stops`. Show cached data immediately and an unobtrusive error if the refresh fails.

## DataStore (settings and small key-value data)

- Use Preferences DataStore for user settings (theme, language, last selected item). Create it once (top-level `Context.dataStore by preferencesDataStore(...)` or via DI).
- Expose settings as `Flow` and update with `dataStore.edit { }`. Do not use `SharedPreferences` in new code.
- Do not store large or relational data in DataStore; use Room.

## Background sync (WorkManager)

- Use `WorkManager` for deferrable sync, not raw services or `AlarmManager`. Periodic work minimum interval is 15 minutes.
- Add constraints (`NetworkType.CONNECTED`) and a backoff policy. Make workers idempotent.
- With Hilt use `@HiltWorker` + `@AssistedInject` (needs `androidx.hilt:hilt-work`).
- For data that must be very fresh while the screen is open (e.g. live positions), poll from the ViewModel with a lifecycle-aware flow instead of WorkManager.

## Checklist before finishing

- [ ] DTO / Entity / domain models are separate and mapped.
- [ ] No network or DB call from a composable.
- [ ] Errors handled and surfaced as UI state; loading/empty/error states exist.
- [ ] Migration added if an entity changed.
- [ ] No secrets or hardcoded production URLs in committed code.
- [ ] Ran `./gradlew :app:assembleDebug` and reported the result.
