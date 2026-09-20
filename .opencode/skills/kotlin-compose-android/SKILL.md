---
name: kotlin-compose-android
description: Build, modify, debug and review native Android apps written in Kotlin with Jetpack Compose (Material 3, ViewModel, StateFlow, Navigation Compose, Hilt/Koin, Room, Retrofit, Coil). Use this skill whenever the task touches a Gradle Android project, .kt files with @Composable functions, screens, ViewModels, navigation, theming, lists, Gradle build errors or Compose previews, even if the user does not say "Compose" explicitly.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, jetpack-compose, android
  model-tuned-for: big-pickle
---

# Kotlin + Jetpack Compose (Android)

Follow this skill for any work in an Android project that uses Kotlin and Jetpack Compose.
Be precise and conservative: prefer stable, well-known APIs over clever or recent ones.

## 0. Working rules (read first)

1. **Inspect before writing.** Read `settings.gradle(.kts)`, `build.gradle(.kts)` (root and `app`), `gradle/libs.versions.toml`, `AndroidManifest.xml` and 2-3 existing screens. Match the project's package layout, naming, DI framework and navigation style. Do not introduce a new library if one already covers the need.
2. **Never invent APIs.** If unsure a Compose/AndroidX function, parameter or artifact exists, search the codebase (`grep`) for existing usage, or check the imports in the Gradle cache/sources. If still unsure, use a simpler API you are certain about and say so.
3. **Never guess dependency versions.** Reuse versions already in `libs.versions.toml`. Use the Compose BOM; do not pin individual `androidx.compose.*` versions when a BOM is present. If a new dependency is needed, add it to the version catalog and tell the user which version you chose and why.
4. **Small, complete changes.** One feature/fix per change. Every new screen must compile, have a `@Preview`, and be wired into navigation.
5. **Verify.** After edits, run the build (see section 9). Fix all errors and new warnings you introduced before reporting done. Never claim it builds without running it; if you cannot run it, say so explicitly.
6. **Preserve user code.** Do not reformat or rename unrelated files. Do not delete code you do not understand.

## 1. Project structure (default when starting fresh)

```
app/src/main/java/<package>/
├── MainActivity.kt            // setContent { AppTheme { AppNavHost() } }
├── App.kt                     // Application class (Hilt: @HiltAndroidApp)
├── di/                        // Hilt modules / Koin modules
├── data/
│   ├── local/                 // Room: Entity, Dao, Database
│   ├── remote/                // Retrofit API, DTOs
│   └── repository/            // Repository implementations
├── domain/                    // Models (+ use cases only if logic is non-trivial)
└── ui/
    ├── theme/                 // Color.kt, Type.kt, Theme.kt
    ├── navigation/            // Routes + NavHost
    ├── components/            // Reusable composables
    └── <feature>/             // XxxScreen.kt, XxxViewModel.kt, XxxUiState.kt
```

If the project already uses a different layout, follow it instead.

## 2. Architecture: unidirectional data flow

- **State flows down, events flow up.**
- One `UiState` per screen (immutable `data class` or `sealed interface`), exposed by the ViewModel as `StateFlow<UiState>`.
- User actions are plain lambdas (`onClick`, `onQueryChange`) or a sealed `UiEvent`/`Action` handled by one ViewModel function.
- Business/data logic lives in the ViewModel and repositories, never inside composables.

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getItems() }
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
```

Split every screen into two composables:

```kotlin
@Composable
fun HomeRoute(
    onItemClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state = state, onItemClick = onItemClick, onRetry = viewModel::load)
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) { /* stateless UI, easy to preview and test */ }
```

- Always use `collectAsStateWithLifecycle()` (dependency `androidx.lifecycle:lifecycle-runtime-compose`), not `collectAsState()`.
- Do not pass ViewModels down into child composables; pass state and lambdas.
- One-off effects (snackbar, navigation): expose via `Channel`/`SharedFlow` and collect in `LaunchedEffect`, or model as state that the UI consumes and clears.

## 3. Composable rules

- Every public composable takes `modifier: Modifier = Modifier` as the **first optional parameter**, and applies it to the **outermost** layout node only.
- Name composables that emit UI in `PascalCase` nouns (`ProfileCard`). Composables that return a value use `camelCase` (`rememberFoo`).
- Keep composables small; extract when a block exceeds ~40 lines or is reused.
- Hoist state: a composable that only displays data should not own it. Use `remember { mutableStateOf(..) }` only for purely local UI state (expanded, focus, text field before submit).
- Use `rememberSaveable` for state that must survive rotation/process death (input text, selected tab).
- Avoid heavy work in composition. Use `remember(key)` / `derivedStateOf` for computed values, and move real work to the ViewModel.
- Pass **stable** parameters: prefer immutable `data class` and `kotlinx.collections.immutable` (`ImmutableList`) or wrap lists in a `@Immutable` holder if recomposition of large lists is a measured problem. Do not add `@Stable`/`@Immutable` speculatively.
- Modifier order matters: `padding` before `background` vs after gives different results. Typical: `size/fillMaxWidth` -> `clip` -> `background` -> `clickable` -> `padding`.
- Do not use `Modifier.composed`; use `Modifier.Node` or plain modifier chaining.

## 4. Side effects

| Need | Use |
|---|---|
| Run suspend code when a key changes / on first composition | `LaunchedEffect(key)` |
| Cleanup on leave (listeners, callbacks) | `DisposableEffect(key)` with `onDispose` |
| Use latest lambda inside a long-lived effect | `rememberUpdatedState` |
| Coroutine from a click handler | `rememberCoroutineScope()` |
| Convert non-Compose state to Compose state | `produceState` / `collectAsStateWithLifecycle` |

Never launch coroutines or mutate state directly in the body of a composable. `LaunchedEffect(Unit)` is only correct if the effect must run exactly once; otherwise key it on the real inputs.

## 5. Lists

- Use `LazyColumn` / `LazyRow` / `LazyVerticalGrid`, never `Column` + `verticalScroll` for long or unbounded lists.
- Always provide stable `key` and a `contentType` when item types differ:

```kotlin
LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items(items = state.items, key = { it.id }, contentType = { "item" }) { item ->
        ItemRow(item = item, onClick = { onItemClick(item.id) })
    }
}
```

- Never nest a lazy list inside a vertically scrolling parent with unbounded height. Use one lazy list with multiple `item {}` / `items {}` blocks instead.
- Handle the three states explicitly: loading, empty, error (with retry).

## 6. Material 3 and theming

- Use `androidx.compose.material3`. Do not mix in Material 2 (`androidx.compose.material`).
- Read colors, typography and shapes from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`. No hardcoded hex colors or `sp` sizes inside screens.
- Support light and dark theme; dynamic color (Android 12+) is optional and must be guarded with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.
- Use `Scaffold` with `innerPadding` applied to the content. Handle edge-to-edge with `enableEdgeToEdge()` in `MainActivity` and `WindowInsets` (`imePadding()`, `navigationBarsPadding()`) where needed.
- All user-visible text goes in `res/values/strings.xml` and is read with `stringResource(R.string.x)`. Do not hardcode strings in composables. If the project has multiple locales, update every `values-xx/strings.xml`.
- Sizes in `dp`, text in `sp`. Reuse spacing constants instead of magic numbers repeated across files.

## 7. Navigation

- Use Navigation Compose. If the project's `navigation-compose` is 2.8.0 or newer, prefer **type-safe routes** with `@Serializable` objects/data classes; otherwise use string routes defined as constants.
- Define routes in one file. Create the `NavController` once (`rememberNavController()`) at the top and pass **lambdas** (`onNavigateToDetail`) to screens, not the controller itself.
- Pass IDs, not whole objects, between destinations. Load data in the destination's ViewModel (`SavedStateHandle` / `toRoute<T>()`).
- Bottom bar: keep `launchSingleTop = true`, `restoreState = true`, and `popUpTo(graph.findStartDestination().id) { saveState = true }`.

```kotlin
@Serializable object Home
@Serializable data class Detail(val id: String)

NavHost(navController, startDestination = Home) {
    composable<Home> { HomeRoute(onItemClick = { navController.navigate(Detail(it)) }) }
    composable<Detail> { DetailRoute(onBack = navController::navigateUp) }
}
```

## 8. Data, DI and networking (only if the project uses them)

- **DI:** follow the existing framework. Hilt: `@HiltAndroidApp`, `@AndroidEntryPoint` on `MainActivity`, `@HiltViewModel`, modules with `@Module @InstallIn(SingletonComponent::class)`. Use KSP (`ksp(...)`) rather than kapt for new processors.
- **Room:** DAOs return `Flow<T>` for observed data and `suspend` for writes. Never run queries on the main thread. Bump the database version and add a `Migration` when changing an entity; do not use `fallbackToDestructiveMigration()` in production code without telling the user.
- **Retrofit/OkHttp + kotlinx.serialization (or Moshi):** DTOs separate from domain models; map in the repository. Repository functions are `suspend` or return `Flow`, and wrap errors instead of leaking exceptions to the UI.
- **Images:** Coil (`AsyncImage`), always with a `contentDescription` and a sensible `contentScale`.
- Dispatchers: inject them or use `withContext(Dispatchers.IO)` inside repositories; ViewModels stay on `viewModelScope`'s default.
- Never hardcode API keys or secrets in source. Use `local.properties` + `BuildConfig` or a secrets plugin, and make sure the file is in `.gitignore`.

## 9. Build, lint, test (verification checklist)

Run from the project root (use `gradlew.bat` on Windows). Run in this order and stop at the first failure:

```bash
./gradlew :app:assembleDebug        # compiles everything
./gradlew :app:lintDebug            # Android lint
./gradlew :app:testDebugUnitTest    # unit tests
```

- Read the **first** error in the Gradle output, not the last; later errors are usually consequences.
- Common fixes:
  - `Unresolved reference` -> missing import or missing dependency in `build.gradle.kts`.
  - `@Composable invocations can only happen from the context of a @Composable function` -> you called a composable from a lambda that is not composable (e.g. inside `onClick` or a coroutine).
  - Compose compiler mismatch -> with Kotlin 2.0+ use the plugin `org.jetbrains.kotlin.plugin.compose`; do not set `composeOptions.kotlinCompilerExtensionVersion` alongside it.
  - Duplicate class / version conflicts -> check the BOM and `libs.versions.toml`; do not add exclusions blindly.
  - `minSdk` errors -> guard with `Build.VERSION.SDK_INT` or raise `minSdk` only after asking the user.
- Unit-test ViewModels with `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`) and fake repositories. UI tests use `createComposeRule()` and semantic finders (`onNodeWithText`, `onNodeWithTag`).

## 10. Performance and quality checklist

- No object allocations or sorting/filtering in the composition body; use `remember`/`derivedStateOf` or the ViewModel.
- Read frequently changing state as late as possible (lambda-based modifiers like `Modifier.offset { }`, `graphicsLayer { }`).
- Provide `contentDescription` for meaningful images/icons (`null` for decorative ones), minimum touch target 48dp, and adequate contrast.
- Add a `@Preview` (and a dark variant when it adds value) for each stateless screen and reusable component, using fake data. Previews must not need a ViewModel, DI or network.
- Remove unused imports and dead code you introduced. Keep functions and files focused.

## 11. Anti-patterns (do not do these)

- Business logic or `viewModelScope.launch` triggered from inside a composable.
- Passing `NavController`, `Context` or a `ViewModel` into reusable components.
- `LiveData` in new code (use `StateFlow`), `collectAsState()` for lifecycle-bound flows, `Accompanist` libraries that were merged into the platform (system UI controller, insets, pager, swipe refresh).
- Mixing Material 2 and Material 3 components.
- `mutableStateOf` mutated from background threads without proper coroutine context.
- Hardcoded strings, colors and dimensions inside screens.
- Giant single-file screens (>400 lines) instead of extracting components.
- Adding libraries or changing Gradle/AGP/Kotlin versions without being asked.

## 12. How to report back

When finished, give a short summary: files created/changed, any new dependency (with version), the exact Gradle commands you ran and their result, and anything you could not verify. Ask before doing large refactors or version upgrades.
