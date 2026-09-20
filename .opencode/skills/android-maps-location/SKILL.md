---
name: android-maps-location
description: Add maps, markers, routes and device location to Android apps built with Kotlin and Jetpack Compose - Google Maps Compose (or OpenStreetMap-based alternatives), runtime location permissions, FusedLocationProvider, camera control, polylines, marker clustering, live-updating positions and stop/route displays for transit or navigation-style apps. Use this skill whenever the task mentions maps, GPS, location, nearby places, stops, routes, geofencing, "show on a map", API keys for maps, or location permissions.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, google-maps, location
---

# Android maps and location

Use together with `kotlin-compose-android`. Check what the project already uses (Google Maps, osmdroid, MapLibre) and keep it. If nothing exists, ask the user which provider they want before adding one.

## Choosing a provider

| Option | Pros | Cons |
|---|---|---|
| Google Maps Compose (`com.google.maps.android:maps-compose`) | Native Compose API, best Android integration | Needs API key and billing account |
| osmdroid / MapLibre (OpenStreetMap data) | No Google key, works offline with tiles | Compose wrapper via `AndroidView`, less polished |

Do not mix providers in one screen.

## API keys (Google Maps)

- Never commit the key. Put it in `local.properties` (which is git-ignored) and inject it into the manifest, for example with the Secrets Gradle Plugin (`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`), and reference it as `${MAPS_API_KEY}` in `<meta-data android:name="com.google.android.geo.API_KEY" .../>`.
- Tell the user to restrict the key in Google Cloud Console (package name + SHA-1).
- Provide a `local.defaults.properties` with a placeholder so the project still builds without the real key.

## Map composables

```kotlin
@Composable
fun StopsMap(
    stops: List<Stop>,
    onStopClick: (Stop) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.75, -8.93), 13f)
    }
    GoogleMap(modifier = modifier, cameraPositionState = cameraPositionState) {
        stops.forEach { stop ->
            Marker(
                state = MarkerState(LatLng(stop.lat, stop.lng)),
                title = stop.name,
                onClick = { onStopClick(stop); false },
            )
        }
    }
}
```

- The map composable is **stateless**: data comes in as parameters, camera events go out as lambdas. Keep the `CameraPositionState` in the screen/route composable.
- Move the camera with `cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(...))` inside a `LaunchedEffect` or coroutine, not during composition.
- For many markers (more than roughly 100) use clustering from `maps-compose-utils` instead of thousands of `Marker`s.
- Draw routes with `Polyline(points = latLngList)`. Fit a route in view with `LatLngBounds.Builder` and `CameraUpdateFactory.newLatLngBounds(bounds, padding)`.
- Do not create `MarkerState(...)` / bitmaps in a hot recomposition path without `remember`.
- Keep the map in a `Box` with UI overlays (search bar, bottom sheet) on top; apply `contentPadding` to the map so controls are not hidden by overlays or system bars.

## Location permission (runtime)

- Declare `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` in the manifest.
- Request at runtime with `rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions())`, only when the feature needs it, and explain why in the UI first.
- Handle all outcomes: granted (precise), granted (approximate only), denied, denied permanently. The app must remain usable when location is denied (default camera position, manual search).
- **Do not request background location** unless the user explicitly needs it; it adds strict Play Store review requirements.

## Getting the location

- Use `FusedLocationProviderClient` (`com.google.android.gms:play-services-location`).
- For one-off position: `getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)`.
- For continuous updates: wrap `requestLocationUpdates` in a `callbackFlow`, always `removeLocationUpdates` in `awaitClose`, and collect it lifecycle-aware in the ViewModel/UI so updates stop when the screen is not visible.
- Choose the lowest accuracy and slowest interval that satisfy the feature (battery).
- Foreground service for tracking with the screen off requires `foregroundServiceType="location"` and the `FOREGROUND_SERVICE_LOCATION` permission (Android 14+). Ask before adding it.
- Show the blue "my location" dot with `properties = MapProperties(isMyLocationEnabled = hasPermission)` only when permission is granted.

## Transit-style patterns

- Model `Stop`, `Route`, `Trip` and `StopTime` separately (GTFS-like); load them from Room and show on the map via Flows.
- "Nearest stops": compute distance with `Location.distanceBetween` or an approximate bounding-box query in Room first, then sort a small candidate set.
- Live vehicle positions: poll or stream in the ViewModel, update a `StateFlow<List<VehiclePosition>>`, and animate markers instead of teleporting them. Stop polling when the screen is not in the foreground.
- Show timestamps ("updated 20 s ago") and degrade gracefully when data is stale or offline.

## Checklist

- [ ] API key not committed; placeholder provided.
- [ ] Permission flow handles denied and approximate-only.
- [ ] Location updates are stopped when the UI is not visible.
- [ ] Map composable is stateless and previews still work (use a fake or skip preview for the map itself).
- [ ] Ran `./gradlew :app:assembleDebug` and reported the result; said if it was not tested on a device/emulator.
