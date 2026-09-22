---
name: android-compose-ui-design
description: Design and build Jetpack Compose screens and components for the DailyCompany Android app - stateless screens with UiState (Loading/Content/Empty/Error), Route pattern with hiltViewModel only in routes, design tokens from ui/theme, light/dark previews, Material 3, accessibility (48dp targets, contentDescription, 200% text), and reusable components in ui/components. Use this skill whenever the task involves screens, components, theming, states, animations or accessibility.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, material3
---

# Compose UI design (DailyCompany)

Use with `kotlin-compose-android` (architecture/API rules), `android-i18n-theme` (text, colors, locale) and `DESIGN.md` (tokens) / `SCREENS.md` (screens). Only build screens that exist in `SCREENS.md` (AGENTS.md rule 12).

## Pattern (MVVM + UDF)

- Every screen is two composables: **`XRoute`** (calls `hiltViewModel()`, collects `uiState` with `collectAsStateWithLifecycle()`) and **`XScreen`** (stateless: receives state + lambdas, pure UI, easily previewed/tested). Only routes touch ViewModels.
- `UiState` = immutable `data class` (or sealed for distinct modes) with the four states: **Loading · Content · Empty · Error(retry)**.
- One-off effects (snackbar, nav): `Channel`/`SharedFlow` collected in `LaunchedEffect`; don't pass `NavController` or ViewModels down.

## Tokens, not literals

- Zero hardcoded colors, `sp` sizes or corner radii in screens: read `MaterialTheme.colorScheme/typography/shapes`, `Spacing` and `AppShapes` from `ui/theme` (`DESIGN.md`). Theme (including dark mode) must work; preview both.
- All visible text via `strings()`/`AppStrings` — including dates, plurals and `contentDescription` (`android-i18n-theme`).

## Components

- Reuse shared components from `ui/components/` (e.g. `TopAppBarWithSettings`, `SectionHeader`, `EmptyState`). New shared component → `ui/components/`; component used only by one feature → `feature/x/components/`.
- Named exports in PascalCase; every public component takes `modifier: Modifier = Modifier` first and applies it to the outermost node.

## Lists, states, structure

- `LazyColumn`/`LazyRow`/`LazyVerticalGrid` with stable `key`s; don't nest a lazy list in a vertical scroll. Order states explicitly: loading spinner → content → empty (with a friendly message + action) → error (with retry).
- Extract blocks > ~40 lines; keep screens < ~400 lines. One `@Preview` per stateless screen (plus dark variant) with fake data — no ViewModel/DI/network in previews.
- `rememberSaveable` for rotation-safe UI state; local state only for purely visual things.

## Accessibility

- Touch targets ≥ 48 dp; `contentDescription` for meaningful icons/images (null for decorative); no information conveyed by color alone; text must stay legible at 200% font scale. These are acceptance criteria in `DESIGN.md`.

## Animation

- Prefer Material tokens/motion; animate only state transitions that matter (checklist toggle, swipe). No heavy work in composition; use `animate*AsState`/`AnimatedVisibility` sparingly and keyed on state.