---
name: android-debugging-adb
description: Debug Android apps using adb, logcat, emulator commands and stack-trace analysis - installing and launching the app, reading crashes (FATAL EXCEPTION), ANRs, Compose runtime errors, lifecycle/state bugs, network and permission problems, and clearing app data. Use this skill whenever the user reports a crash, "the app closes", a blank screen, an ANR, a stack trace, logcat output, something that works in preview but fails on device, or asks how to run and inspect the app on an emulator or phone.
license: MIT
compatibility: opencode
metadata:
  stack: adb, logcat, emulator
---

# Android debugging with adb and logcat

## Method (follow in order)

1. **Get the evidence.** Ask for or capture the stack trace / logcat. Do not guess the cause from the symptom alone.
2. **Find the root cause.** In a `FATAL EXCEPTION`, read the exception type and message first, then the first stack frame that belongs to the app's own package. Look at every `Caused by:` - the last one is usually the real cause.
3. **Reproduce** with the smallest steps possible.
4. **Fix the cause, not the symptom** (do not just wrap in `try/catch`).
5. **Verify** by rebuilding, reinstalling and repeating the reproduction steps.

## Essential adb commands

Replace `com.example.app` with the real `applicationId` (see `app/build.gradle.kts`).

```bash
adb devices                                   # list devices/emulators
./gradlew :app:installDebug                   # build and install
adb shell am start -n com.example.app/.MainActivity
adb shell am force-stop com.example.app
adb shell pm clear com.example.app            # wipe app data (fresh install state)
adb uninstall com.example.app
adb shell pm grant com.example.app android.permission.ACCESS_FINE_LOCATION
adb shell pm revoke com.example.app android.permission.ACCESS_FINE_LOCATION
adb exec-out screencap -p > screen.png
```

If `adb` is not found on Windows, it lives in `%LOCALAPPDATA%\Android\Sdk\platform-tools`. Tell the user to add it to PATH instead of hardcoding the path in scripts.

## logcat

```bash
adb logcat -d -t 200                          # last 200 lines and exit
adb logcat -c                                 # clear buffer before reproducing
adb logcat *:E                                # errors only
adb logcat -s MyTag:D                         # one tag
adb logcat --pid=$(adb shell pidof -s com.example.app)   # only this app (bash)
```

On PowerShell use `$pid = adb shell pidof -s com.example.app; adb logcat --pid=$pid`.

- Useful search strings: `FATAL EXCEPTION`, `AndroidRuntime`, `ANR in`, `StrictMode`, `Choreographer` (skipped frames).
- Add your own logs with `android.util.Log` or Timber using a clear tag; remove noisy logs before finishing and never log passwords, tokens or personal data.

## Frequent causes

| Symptom in log | Usual cause |
|---|---|
| `NullPointerException` / `lateinit property ... not initialized` | Value used before initialization; missing null-handling for API data. |
| `IllegalStateException: ViewModelStore ...` / `No ViewModelStoreOwner` | Composable used outside an Activity/NavGraph owner, or wrong `viewModel()` scope. |
| `NoSuchElementException` / `IndexOutOfBounds` in a list | Empty list assumed non-empty; use `firstOrNull()` and handle empty state. |
| `Composables can only be invoked from @Composable` (compile-time) | Composable called inside a normal lambda (`onClick`, coroutine). |
| `Duplicate key ... in LazyColumn` | Non-unique `key`; use a stable unique id. |
| `SecurityException: Permission Denial` / missing permission | Permission not declared in the manifest or not granted at runtime. |
| `NetworkOnMainThreadException` | Network call on the main thread; move to a suspend/`Dispatchers.IO` path. |
| `UnknownHostException` / cleartext not permitted | No connectivity, wrong URL, or `http://` blocked by network security config. Use HTTPS. |
| `SerializationException: Field ... is required` | DTO does not match JSON. Make the field nullable/defaulted or fix the DTO. |
| `SQLiteException: no such table` / `Migration didn't properly handle` | Schema changed without migration; uninstall for dev, add a migration for real data. |
| `ANR in <package>` | Main thread blocked (I/O, heavy loop, lock). Read the main-thread stack in the ANR trace. |
| `ClassNotFoundException` / `NoSuchMethodError` only in release | R8 removed or renamed something: add keep rules (see `android-release-security`). |

## Compose-specific tips

- Blank or stuck UI: check that state is actually being updated (`StateFlow` value changes) and that the composable reads it (`collectAsStateWithLifecycle`).
- State lost on rotation: use `rememberSaveable` or keep state in the ViewModel.
- Infinite recomposition or jank: look for allocations or state writes in the composable body; use Layout Inspector / recomposition counts in Android Studio and report findings instead of guessing.
- Works in `@Preview` but crashes on device: preview used fake data; the real path likely has null/empty/error data. Reproduce with those inputs.

## Reporting

Give the user: the root cause in one sentence, the exact file/line changed, how you verified (commands run and what you observed), and anything you could not test because no device or emulator was available. Do not claim a crash is fixed without re-running the failing scenario.
