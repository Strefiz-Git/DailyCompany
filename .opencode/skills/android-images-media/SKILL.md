---
name: android-images-media
description: Handle photos in the DailyCompany Android app - Photo Picker and camera capture without storage permissions, copying images to filesDir/images, downscaling to <=1600px with ~400px thumbnails off the main thread, EXIF orientation normalization, Coil AsyncImage display, and deleting/migrating image files with their items. Use this skill whenever the task involves photos, camera, gallery, EXIF, thumbnails, image storage or Coil.
license: MIT
compatibility: opencode
metadata:
  stack: kotlin, compose, coil
---

# Images and media (DailyCompany)

Use with `android-wardrobe-outfits` (item flow), `android-backup-restore` (photos travel inside the zip) and `android-i18n-theme` (content descriptions).

## Rules

- **No storage permission.** Pick with the system Photo Picker: `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia())`. Camera only when the user explicitly asks to shoot from the editor: `TakePicture()` + `FileProvider` URI (camera permission only if using CameraX directly — otherwise none).
- **Copy, don't reference:** move the chosen image into `context.filesDir/images/` and store only the **filename** in the database (`imageFile`/`thumbFile`). Never persist a `ContentResolver` `Uri` as the only reference (access can be lost).
- **Size budget (PROJECT.md §14):** originals downscaled to ≤1600 px (longest side), JPEG quality ~85; thumbnails ~400 px. Decode/resize **off the main thread** (`Dispatchers.IO`), normalize EXIF orientation on save (many camera photos are rotated 90°).
- Display with Coil `AsyncImage`; always a meaningful `contentDescription` (the item name) and sensible `contentScale` (grid cards `Crop`).
- **Deleting:** delete the image files when the item is deleted. Orphan cleanup also runs after backup import.
- Backup/export: photos are always inside the single `.zip` (R13) — never a sidecar folder.
- Type: pick/compress to JPEG consistently; name files predictably (`<uid>.jpg`) so backup and orphan detection are trivial.

## Verification

Build + unit tests for the pure parts (downscale params, filename mapping). Real picking/camera/EXIF behaviour needs a device or emulator — say so, don't claim it works from compiling alone.