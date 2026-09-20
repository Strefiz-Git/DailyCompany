---
name: android-testing
description: Write and run tests for Kotlin Android apps with Jetpack Compose - JUnit unit tests for ViewModels and repositories, coroutine and Flow testing (runTest, Turbine), fakes vs MockK, Room in-memory database tests, Compose UI tests with createComposeRule, and Gradle test commands. Use this skill whenever the user asks for tests, coverage, "make sure it works", TDD, fixing a failing test, or when you finish a feature with non-trivial logic in a ViewModel, repository, mapper or use case.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, junit, turbine, compose-ui-test
---

# Android testing

Use together with `kotlin-compose-android`. Reuse the test libraries already in `libs.versions.toml`; add new ones only when needed and say so.

## What to test (priority order)

1. **ViewModels** - state transitions for each user action, loading/success/error.
2. **Repositories and mappers** - DTO -> Entity -> Domain, error handling.
3. **Pure logic** (sorting, filtering, distance, time calculations) - fast plain JUnit tests.
4. **Room DAOs** - instrumented tests with an in-memory database.
5. **Critical UI flows** - Compose UI tests for key screens (not every composable).

Do not write tests that only re-implement the code under test. Test behavior, not implementation details.

## Locations and commands

- Unit tests: `app/src/test/java/...` -> `./gradlew :app:testDebugUnitTest`
- Instrumented/UI tests: `app/src/androidTest/java/...` -> `./gradlew :app:connectedDebugAndroidTest` (needs an emulator or device; if none is available, say so, do not claim they passed)
- Run one class: `./gradlew :app:testDebugUnitTest --tests "com.example.HomeViewModelTest"`
- Reports: `app/build/reports/tests/`. Read the failure message and the first stack frame in your own code.

## Coroutines and Flow

- Dependency: `org.jetbrains.kotlinx:kotlinx-coroutines-test`. Use `runTest { }`.
- ViewModels use `Dispatchers.Main`; replace it in tests with a rule:

```kotlin
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

- Test Flows with Turbine (`app.cash.turbine`): `flow.test { assertEquals(expected, awaitItem()); cancelAndIgnoreRemainingEvents() }`.
- For `StateFlow` UI state, assert the sequence of meaningful states (loading -> content) or the final state after `advanceUntilIdle()`.

## Fakes over mocks

- Prefer **hand-written fakes** for repositories and data sources (`FakeStopRepository` holding a `MutableStateFlow`). They are simple, fast and survive refactors.
- Use MockK only for things that are hard to fake (Android framework classes, third-party clients). Do not mock data classes.
- Make failure injectable: `var shouldFail = false` in the fake so error paths are tested.

```kotlin
class HomeViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `load shows items when repository succeeds`() = runTest {
        val repo = FakeItemRepository(items = listOf(Item("1", "A")))
        val vm = HomeViewModel(repo)

        assertEquals(listOf(Item("1", "A")), vm.uiState.value.items)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `load shows error when repository fails`() = runTest {
        val repo = FakeItemRepository(shouldFail = true)
        val vm = HomeViewModel(repo)

        assertNotNull(vm.uiState.value.error)
    }
}
```

Test names use backticks with a readable sentence.

## Room tests (instrumented)

```kotlin
val db = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(), AppDatabase::class.java
).allowMainThreadQueries().build()
```

Close the database in `@After`. Test inserts, updates (upsert), queries with ordering/filters and `Flow` emissions.

## Compose UI tests

- Rule: `createComposeRule()`. Test the **stateless** `Screen` composable with fake state, not the ViewModel/Hilt graph.
- Find nodes by text, content description or `Modifier.testTag("...")` (add tags only where semantics are not enough).

```kotlin
@get:Rule val composeRule = createComposeRule()

@Test
fun errorState_showsRetryButton() {
    composeRule.setContent { AppTheme { HomeScreen(state = HomeUiState(error = "Falha"), onItemClick = {}, onRetry = {}) } }
    composeRule.onNodeWithText("Tentar novamente").assertIsDisplayed()
}
```

- Use `composeRule.waitUntil { }` or idling instead of `Thread.sleep`.
- Add `androidx.compose.ui:ui-test-junit4` (androidTestImplementation) and `ui-test-manifest` (debugImplementation).

## Rules

- Tests must be deterministic: no real network, no real clock (inject a clock/time provider), no random data without a fixed seed.
- One logical assertion group per test; keep tests small.
- When fixing a bug, first write a test that fails, then fix, then confirm it passes.
- Never delete or weaken an existing test just to make the build pass; explain to the user if a test is obsolete.

## Report

State which tests were added, the exact command run, and the result (passed/failed counts). If instrumented tests could not run, say that clearly.
