---
name: android-release-security
description: Prepare Android apps for release and keep them secure - R8/ProGuard shrinking and keep rules, release signing with a keystore, versionCode/versionName, AAB vs APK, network security config, secrets handling, minimal permissions, Play Store data-safety and privacy considerations, and testing the release build. Use this skill whenever the user mentions release, publish, Play Store, APK/AAB, signing, keystore, ProGuard/R8, obfuscation, API keys, secrets, permissions review, security, or hands the app in for delivery (e.g. school/final project demos on a real device).
license: MIT
compatibility: opencode
metadata:
  stack: r8, signing, play-store, security
---

# Android release and security

## Never do

- Never commit keystores (`*.jks`, `*.keystore`), `keystore.properties`, `local.properties`, `google-services.json` with private data policies you have not checked, or API keys.
- Never print passwords or keys in logs, chat output or scripts.
- Never generate a keystore and hide the password from the user: tell the user to store the keystore and passwords safely; losing them means the app cannot be updated on Play.

## Secrets and configuration

- Keep secrets in `local.properties` or environment variables and inject through Gradle (`buildConfigField`, manifest placeholders, Secrets Gradle Plugin). Add these files to `.gitignore`.
- Anything shipped inside the APK can be extracted. Client-side keys must be **restricted** (package name + SHA-1 for Google keys). True secrets belong on a backend, not in the app.
- If a secret was already committed, tell the user to revoke and rotate it; deleting the file in a new commit is not enough.

## Signing (release)

```kotlin
// app/build.gradle.kts
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

The build must still work for people without the keystore (debug builds must not depend on it).

Create a keystore (user runs this and chooses their own passwords):

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

## Versioning and artifacts

- Increase `versionCode` (integer, must strictly grow) for every uploaded build; `versionName` is the human-readable version.
- Play Store requires an **AAB**: `./gradlew :app:bundleRelease`. For direct install/demo use an APK: `./gradlew :app:assembleRelease`.
- Check the current `targetSdk` requirement of Google Play before publishing; it changes yearly. Tell the user to verify it instead of assuming.

## R8 / ProGuard

- Enable `isMinifyEnabled` for release, then **test the release build** - shrinking breaks reflection-based code.
- Libraries like Retrofit, Room, Hilt, kotlinx.serialization and Compose ship their own consumer rules; add manual keep rules only for problems you actually observe:

```
# Example: keep DTOs used with reflection-based parsing
-keep class com.example.app.data.remote.dto.** { *; }
```

- Symptoms of missing rules: works in debug, crashes in release with `ClassNotFoundException`, `NoSuchMethodError`, or JSON fields ending up empty. Read the stack trace, add the narrowest rule, rebuild.
- Keep `mapping.txt` (in `app/build/outputs/mapping/release/`) for each shipped version so crashes can be de-obfuscated.

## Network and data security

- HTTPS only. Do not enable `usesCleartextTraffic` for production; if local dev needs HTTP, restrict it to debug via a debug-only `network_security_config.xml`.
- Do not disable SSL verification or add trust-all `TrustManager`s, ever.
- Set `android:allowBackup="false"` (or configure backup rules) when the app stores tokens or sensitive data.
- Store sensitive values (tokens) in encrypted storage; do not put them in plain DataStore/SharedPreferences.
- Validate and sanitize any data coming from intents, deep links, and the network. Set `android:exported` explicitly on every component and keep it `false` unless another app must call it.
- WebViews: keep JavaScript disabled unless required and never load untrusted URLs with a JS bridge.

## Permissions and privacy

- Request only permissions the app truly needs; remove unused ones from the manifest (libraries may add some - check the merged manifest).
- Explain sensitive permissions (location, camera) in the UI before requesting.
- Apps that collect location or personal data need a **privacy policy** and an accurate Play **Data safety** form. Remind the user; do not fill it in for them without asking.
- For school projects with real users (e.g. a partner company), remind the user about GDPR basics: collect the minimum, say what is stored and why, and provide a way to delete data.

## Pre-release checklist

- [ ] `applicationId`, `versionCode`, `versionName` correct.
- [ ] Release build compiled: `./gradlew :app:assembleRelease` (or `bundleRelease`).
- [ ] Release build installed and manually tested on a real device or emulator: login/main flows, rotation, offline, permission denied.
- [ ] No debug logging, test endpoints or dev keys in the release build.
- [ ] R8 enabled and no reflection-related crashes.
- [ ] Keystore and passwords backed up by the user outside the repo.
- [ ] `.gitignore` covers keystores, `local.properties`, `keystore.properties`.

In the final report, list what you verified and what still needs to be tested on a real device.
