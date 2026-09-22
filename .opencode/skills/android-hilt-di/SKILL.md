---
name: android-hilt-di
description: Use this skill whenever the task involves dependency injection in this Android app - Hilt modules, @InstallIn, @HiltViewModel, qualifiers for dispatchers/context, KSP vs kapt, missing-binding or Dagger/Hilt compile errors, or wiring WorkManager workers with @HiltWorker.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, hilt, ksp
---

# Hilt DI (DailyCompany)

Use with `kotlin-compose-android` and `android-data-offline`. This project uses **Hilt + KSP** (no kapt). Keep the existing conventions in `di/`.

## Setup and rules

- `@HiltAndroidApp` on `DailyApp`, `@AndroidEntryPoint` on `MainActivity`. Modules live in `com.dailycompany.di` with `@Module @InstallIn(SingletonComponent::class)`.
- ViewModels: `@HiltViewModel` + `@Inject constructor(...)`. Only `XxxRoute` composables call `hiltViewModel()`; screens stay stateless (see `android-compose-ui-design`).
- Qualifiers that already exist (check `di/AppDiModule.kt` before adding new ones):
  - `@IoDispatcher` → `Dispatchers.IO.asLimitedParallelism(2)` (the module already imports `kotlinx.coroutines.asLimitedParallelism` — do not remove it).
  - `@ApplicationContext` for anything needing `Context`.
  - Bind `Clock` (injected — the app never reads `System.currentTimeMillis()` directly).
- Never add a dependency just to "make Hilt work"; add to `gradle/libs.versions.toml` and tell the user (AGENTS.md rule 7/10).
- Room DAOs are injected directly into repositories (repositories are the only thing ViewModels talk to). `DatabaseProvider`/`AppDatabase` live in `data/local/`.

## WorkManager

`@HiltWorker` + `@AssistedInject` + `@Assisted` params, and `HiltWorkerFactory` set up in `DailyApp` (`Configuration.Provider`). Requires `androidx.hilt:hilt-work` — only add after asking.

## Common compile errors

| Error | Likely cause / fix |
|---|---|
| `Dagger/MissingBinding` | A class needs `@Inject constructor`, a module `@Provides`/`@Binds`, or an `@Module` is not `@InstallIn(SingletonComponent::class)`. Read the first missing binding. |
| `[ksp] cannot be provided without an @Provides-annotated method` | Same as above; check qualifiers match (e.g. `@IoDispatcher` vs plain `CoroutineDispatcher`). |
| `Duplicate binding` | Two modules provide the same type — qualify one or remove the duplicate. |
| `@Unresolved` on `hiltViewModel()` | The composable is not inside an `@AndroidEntryPoint` Activity/NavHost, or the ViewModel constructor cannot be satisfied. |
| `Expected an activity context` crash when opening a screen | Do **not** replace `LocalContext` with a `createConfigurationContext(...)` wrapper — it breaks `hiltViewModel()`. Locale passes via `LocalConfiguration` + `LocalAppLocale` (see `android-i18n-theme`). |

## Verify

`./gradlew :app:assembleDebug` and `:app:testDebugUnitTest`; report results. Do not commit `local.properties` or any keystore.