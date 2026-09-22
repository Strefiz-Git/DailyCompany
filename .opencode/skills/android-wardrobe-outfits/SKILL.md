---
name: android-wardrobe-outfits
description: Build a digital wardrobe and outfit suggestion feature for the DailyCompany Android app - clothing items with photos and multiple categories, slots (top, bottom, one-piece, outerwear, shoes, accessories), size systems with ordering, saved outfits with locks, weather-based suggestions, avoiding recently worn items, local image storage, and optional weather API with offline fallback. Use this skill whenever the task involves choosing clothes, "what should I wear", wardrobe/closet, outfits, clothing photos, or weather-dependent recommendations.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, room, coil
---

# Wardrobe and outfit suggestions (DailyCompany)

Use with `kotlin-compose-android`, `android-data-offline`, `android-images-media` and `android-i18n-theme`. This skill is **specific to DailyCompany**: the model below is the one in `PROJECT.md` §10 and the business rules R1–R5 of §9 are inviolable. Do not "simplify" the model to match a generic template.

## Business rules (non-negotiable)

- **R1** — an item has *several categories* (N:N) but **exactly one functional slot**.
- **R2** — items in the wash (`isAvailable = false`) never enter a random suggestion.
- **R3** — the generator never returns Top+Fundo *and* Peça única at the same time.
- **R4** — no candidates for a required slot → explanatory message, never a silent incomplete outfit.
- **R5** — deleting an item used in outfits reports how many outfits are affected before confirming.

## Data model (Room) — per `PROJECT.md` §10

```kotlin
CategoryEntity(id, uid, parentId?, name, nameKey?, slot?, sortOrder)          // 2 levels, editable
ClothingItemEntity(id, uid, name, slot, imageFile?, thumbFile?, sizeSystem, sizeValue?, sizeRank?,
                   brand?, colors, seasons, warmth, formality, isWaterproof, notes?,
                   isFavorite, isAvailable, isArchived, lastWornDate?, timesWorn, createdAt)
ItemCategoryCrossRef(itemId, categoryId)                                      // N:N
OutfitEntity(id, uid, name, notes?, formality?, isFavorite, createdAt, lastWornDate?, timesWorn)
OutfitItemCrossRef(outfitId, itemId, slot)
WearLogEntity(id, date, outfitId?, itemIds)
```

- **Slots:** `Top · Fundo · Peça única · Casaco · Calçado · Acessórios`. The slot is pre-filled from the category and editable per item (R1).
- **Categories by default** (2 levels, editable/renamable/reorderable): Tops · Fundos · Peça única · Casacos · Calçado · Acessórios, each with subcategories. Defaults are referenced by `nameKey` (translated at display time); user-created categories keep their own text (see `android-i18n-theme`).
- **Size systems** (`SizeSystem`, per item): letters (XXS…XXXL) · numeric EU · shoe EU (35–48, half sizes) · waist/length ("32/34") · one size · custom free text. Store `sizeRank` so items sort/merge correctly across systems — the parsing/ordering logic is **pure Kotlin with required unit tests** (letters S<M<L<XL, numeric 35<36<…<48, waist 32/34 > 30/32, custom falls back to rank by token).

## Photos (see also `android-images-media`)

- Pick with the **Photo Picker** (`PickVisualMedia`), or camera via `TakePicture()` + `FileProvider` — **no storage permission**.
- Copy the image into `context.filesDir/images/`; the database stores **only the filename** (`imageFile`/`thumbFile`).
- Downscale originals to ≤1600 px and build thumbnails ~400 px, decoded **off the main thread**; normalize EXIF orientation on save.
- Display with Coil `AsyncImage` + `contentDescription` (the item name). Delete the file when the item is deleted (and remember to delete it from backups/orphans on import — see `android-backup-restore`).

## Weather input (optional — Extras)

- Keep a provider-independent `WeatherRepository` interface (`forDay(date, place): Result<WeatherSummary>`). Offline fallback: cached forecast, or ask the user "frio / ameno / quente".
- Open-Meteo is a free option; **check its current docs before implementing** and always support manual city so location permission stays optional. Weather is not a v1 requirement (`PROJECT.md` §16 has it open).

## Suggestion engine (pure Kotlin, unit-tested)

```kotlin
fun suggestOutfit(wardrobe: List<ClothingItem>, ctx: OutfitContext, random: Random): OutfitSuggestion
```

1. Only `isAvailable` items (R2); respect the season and the "variety" rule (avoid items/outfits used in the last N days, default 4).
2. Warmth target from the temperature; add outerwear when cold or likely rain (waterproof preferred).
3. Formality matches the day's events, if any.
4. Respect locks (fixed slots) and filters; "Outro" rerolls without repeating the session's previous suggestion.
5. **R3**: one-piece mode excludes top+bottom. **R4**: missing required slot → return what is present *plus* an explicit list of what is missing (message via a string key, not silence).
6. Tie-breaking uses an **injected `Random`** (fixed seed in tests).
7. **Reasons are `AppStrings` keys** resolved through `strings()` (three languages) — never hardcoded PT-PT strings.

Outfit builder per `SCREENS.md` §6.1: one row per slot as a carousel with a lock, filter sheet, "Outro / Guardar / Usar hoje".
"Do you have what it takes" empty states: "Adiciona pelo menos uma parte de cima, uma de baixo e sapatos" (via `AppStrings`).

## Backup

**R13**: the backup is always ONE `.zip` with domain data + settings + photos together — never "images only if the user chooses". Every new attribute of the model must be added to the backup DTOs in the same change. See `android-backup-restore`.

## Tests (required)

- `suggestOutfit`: cold+rain, hot day, formal event, missing categories (R4), recently worn excluded, seeds, locks.
- Size parsing/ordering across all systems.
- Repository test with a fake `WeatherRepository` (success / failure / cached).
- Report what was tested; photo flows need a device or emulator.