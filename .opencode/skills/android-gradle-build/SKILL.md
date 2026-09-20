---
name: android-gradle-build
description: Configure and fix Gradle builds for Android projects (Kotlin DSL) - version catalog (libs.versions.toml), Android Gradle Plugin, Kotlin, KSP and Compose compiler plugin compatibility, Compose BOM, dependencies, build types and flavors, BuildConfig, sync and compile errors, duplicate classes and dependency conflicts. Use this skill whenever a Gradle sync or build fails, when adding or updating a dependency or plugin, when editing build.gradle.kts, settings.gradle.kts, gradle.properties or libs.versions.toml, or when the user reports "it doesn't compile" / "sync failed".
license: MIT
compatibility: opencode
metadata:
  stack: gradle, kotlin-dsl, agp, ksp
---

# Android Gradle and build configuration

## Golden rules

1. **Do not upgrade AGP, Kotlin, Gradle, compileSdk or the Compose BOM unless asked.** Upgrades cascade. If an upgrade is required, explain why and list what else must change.
2. **Use the version catalog.** Add libraries and plugins to `gradle/libs.versions.toml` and reference them as `libs.xxx` / `alias(libs.plugins.xxx)`. Do not hardcode version strings in `build.gradle.kts` when a catalog exists.
3. **Change one thing at a time** and rebuild, so the cause of a failure is obvious.
4. **Never guess a version number.** If you need a new dependency, prefer one already used in the project or its BOM. If you must pick a version, tell the user the exact value and that they should confirm it is current and compatible.
5. **Do not run `clean` or delete `.gradle` / `build` as a first reaction.** Read the error first.

## Windows/Linux commands

Use `./gradlew` on macOS/Linux and `.\gradlew.bat` (or `gradlew`) on Windows PowerShell.

```bash
./gradlew :app:assembleDebug                       # build
./gradlew :app:assembleDebug --stacktrace          # more detail
./gradlew :app:dependencies --configuration debugRuntimeClasspath   # dependency tree
./gradlew :app:dependencyInsight --dependency okhttp --configuration debugRuntimeClasspath
./gradlew --refresh-dependencies :app:assembleDebug # only if artifacts look stale
./gradlew tasks --all                              # discover tasks
```

## Version catalog layout

```toml
[versions]
kotlin = "..."          # keep whatever the project has
ksp = "..."             # KSP version must match the Kotlin version prefix
composeBom = "..."

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

In `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)   // no version: BOM controls it
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

## Key configuration

- `android { compileSdk = ...; defaultConfig { minSdk = ...; targetSdk = ... } }` - keep existing values; ask before changing `minSdk`.
- `compileOptions` and `kotlin { jvmToolchain(17) }` (or the project's JDK version) must be consistent.
- Compose with Kotlin 2.0+: apply the `org.jetbrains.kotlin.plugin.compose` plugin and **do not** set `composeOptions.kotlinCompilerExtensionVersion`. With older Kotlin the opposite applies; check the project's Kotlin version first.
- `buildFeatures { compose = true; buildConfig = true }` (enable `buildConfig` only if needed).
- Annotation processors (Room, Hilt) use KSP: `ksp(libs.xxx)`. Do not add `kapt` to new code unless a library requires it.
- Values that differ per environment (API base URL) go in `buildConfigField` per build type or flavor.
- Repositories: `google()`, `mavenCentral()` in `settings.gradle.kts` `dependencyResolutionManagement`. Add other repos only when necessary.

## Diagnosing errors

Read the **first** error and the "What went wrong" block.

| Symptom | Likely cause and fix |
|---|---|
| `Could not resolve com.x:y:ver` | Wrong coordinates/version or missing repository. Check spelling, the catalog and repositories. |
| `Unresolved reference` after adding a dependency | Sync not done, wrong artifact, or dependency added to the wrong module. |
| `Duplicate class ...` | Two libraries bring the same class. Use `dependencyInsight`, align versions or remove the redundant one. Do not blindly `exclude`. |
| `Plugin ... was not found` | Missing `pluginManagement` repository or wrong plugin id/version. |
| KSP/Room/Hilt errors like `[ksp] ...` | KSP version does not match Kotlin, or an annotation is wrong. Read the first `[ksp]` line. |
| `Inconsistent JVM-target compatibility` | Align `compileOptions`, `kotlin.jvmToolchain` and JDK. |
| `Manifest merger failed` | Conflicting attributes (`tools:replace`), missing `android:exported`, or duplicate permissions. Read the merger report. |
| `Execution failed for task ':app:lintVitalRelease'` | A real lint error in a release build; fix the code, do not disable lint. |
| `OutOfMemoryError: Java heap space` | Raise `org.gradle.jvmargs=-Xmx4g` in `gradle.properties` and tell the user. |

## After every change

- Run `./gradlew :app:assembleDebug` and report the result.
- Show the user a short list of the exact lines changed in Gradle files and why.
- Do not commit generated folders (`build/`, `.gradle/`, `.idea/`) and keep `local.properties` out of version control.
