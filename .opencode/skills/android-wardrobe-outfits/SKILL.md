---
name: android-wardrobe-outfits
description: Build a digital wardrobe and outfit suggestion feature for a Kotlin Jetpack Compose Android app - clothing items with photos, categories, warmth and formality attributes, saved outfits, weather-based suggestions (temperature, rain), rules by event type, avoiding recently worn items, photo picking without permissions, local image storage, and optional weather API integration with offline fallback. Use this skill whenever the task involves choosing clothes, "what should I wear", wardrobe/closet, outfits, clothing photos, or weather-dependent recommendations.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, room, coil, weather-api
---

# Wardrobe and outfit suggestions

Use with `kotlin-compose-android` and `android-data-offline`. Start **rule-based and offline-capable**; do not add AI/LLM calls unless the user asks.

## Data model (Room)

```kotlin
enum class Category { TOP, BOTTOM, ONE_PIECE, OUTERWEAR, SHOES, ACCESSORY }
enum class Formality { CASUAL, SMART_CASUAL, FORMAL, SPORT }

@Entity data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: Category,
    val imagePath: String?,          // app-private file path
    val warmth: Int,                 // 1 (very light) .. 5 (very warm)
    val isWaterproof: Boolean = false,
    val formality: Formality = Formality.CASUAL,
    val colors: String = "",         // comma-separated simple color names
    val seasons: String = "",        // e.g. "SPRING,AUTUMN" (empty = all)
    val lastWornDate: Long? = null,  // epoch day
    val isFavorite: Boolean = false,
    val isAvailable: Boolean = true, // false = in the wash / repair
)

@Entity data class OutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, val formality: Formality, val isFavorite: Boolean = false,
)
@Entity(primaryKeys = ["outfitId", "itemId"]) data class OutfitItemCrossRef(val outfitId: Long, val itemId: Long)
```

- Use `enum` converters, indexes on `category` and `isAvailable`.
- Allow the user to edit attributes any time; sensible defaults so adding an item takes seconds (photo + name + category + warmth slider).

## Photos (no storage permission needed)

- Pick with the **Photo Picker**: `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())` and `PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)`.
- For taking a new photo: `ActivityResultContracts.TakePicture()` with a `FileProvider` URI (ask for the camera permission only if using CameraX directly).
- **Copy the chosen image into app-private storage** (`context.filesDir/wardrobe/`) and store that path. Do not store the picker `Uri` as the only reference (access can be lost).
- Downscale before saving (for example longest side 1024 px, JPEG quality ~85) to avoid large files and OOM. Do it off the main thread.
- Display with Coil `AsyncImage`; always provide `contentDescription` (the item name).
- Delete the file when the item is deleted.

## Weather input

- Model a small, provider-independent interface so the source can change:

```kotlin
data class WeatherSummary(
    val minTempC: Double, val maxTempC: Double, val feelsLikeC: Double?,
    val rainProbabilityPercent: Int, val isRainLikely: Boolean, val windKmh: Double?,
)
interface WeatherRepository { suspend fun forDay(date: LocalDate, place: Place): Result<WeatherSummary> }
```

- A free option that needs no API key for non-commercial use is Open-Meteo (`https://api.open-meteo.com/v1/forecast` with `latitude`, `longitude`, `daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max`, `timezone=auto`). **Check the current terms and parameters in its documentation before implementing**; do not rely on memory for exact field names.
- Place selection: let the user type a city (geocoding API) or use approximate location (`ACCESS_COARSE_LOCATION`, see `android-maps-location` for the permission flow). Always support manual city so location permission is optional.
- Cache the last forecast in Room/DataStore. **When offline, still suggest** using the cached forecast or ask the user for "cold / mild / warm".
- Timeouts and errors must never block the suggestion screen.

## Suggestion engine (pure Kotlin, unit-testable)

```kotlin
data class OutfitContext(
    val weather: WeatherSummary?,
    val eventFormality: Formality?,   // from today's events, if any
    val today: LocalDate,
)
data class OutfitSuggestion(val items: List<ClothingItem>, val reasons: List<String>)

fun suggestOutfit(wardrobe: List<ClothingItem>, ctx: OutfitContext, random: Random): OutfitSuggestion
```

Rules (keep them explicit and documented in code):

1. Consider only `isAvailable` items in the right season.
2. **Target warmth** from feels-like/min temperature, for example: <8 C -> 4-5, 8-14 -> 3-4, 15-20 -> 2-3, >20 -> 1-2. Pick a top/bottom whose warmth sum is close to the target; add `OUTERWEAR` when cold or windy.
3. **Rain likely** -> prefer waterproof outerwear/shoes and mention it in `reasons`.
4. **Formality** must match the next event (formal event -> `FORMAL`/`SMART_CASUAL` items; sport -> `SPORT`).
5. **Variety:** penalize items worn in the last 3-4 days (`lastWornDate`), boost favorites slightly.
6. **Color harmony (simple):** avoid more than 3 strong colors; allow neutrals (black, white, grey, beige, navy) with anything. Keep this heuristic tiny and configurable.
7. Return **reasons** in plain PT-PT strings ("Vai chover, escolhi um casaco impermeavel") so the user understands the choice.
8. Use an injected `Random` (seed in tests) for tie-breaking; offer "Sugerir outra" to reroll while excluding the last suggestion.
9. If the wardrobe is missing categories (no shoes, no outerwear), still return a partial suggestion and say what is missing.

Actions: "Usar hoje" marks items `lastWornDate = today` (undo available), "Guardar como conjunto" creates an `Outfit`.

## UI

- **Today card:** thumbnails of the suggested items, one-line weather ("14-19 C, chuva 60%"), buttons "Sugerir outra" and "Usar hoje".
- **Wardrobe grid:** `LazyVerticalGrid` with category filter chips, search, and an "available/in the wash" toggle.
- **Item editor:** photo, name, category, warmth slider, waterproof switch, formality chips, color chips.
- Empty state teaches the first step ("Adiciona pelo menos uma parte de cima, de baixo e sapatos").

## Privacy

- Photos and wardrobe stay local. Say so in settings. Include images in the app's export only if the user chooses (larger file).

## Tests

- Unit tests for `suggestOutfit`: cold+rain, hot day, formal event, missing categories, recently worn exclusion, seeded randomness.
- Repository test with a fake `WeatherRepository` that succeeds, fails and returns cached data.

Report which parts you could test and note that visual/photo flows need a device or emulator.
