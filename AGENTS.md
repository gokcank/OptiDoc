# OptiDoc

Offline-first Android document scanner (`com.gokcank.optidoc`): ML Kit scanning and on-device OCR, folders,
signatures, watermarks, export to PDF/TXT/JPEG. English + Turkish.

## Build & test
- JDK 17; AGP 9.0.1; Kotlin 2.2.0; compile/target SDK 36, minSdk 26.
- Debug APK: `./gradlew assembleDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Lint: `./gradlew lintDebug` (non-blocking: `abortOnError = false`)
- Room migration test (device/emulator): `./gradlew connectedDebugAndroidTest`
- Release AAB: `./gradlew bundleRelease` (R8 full mode, resource shrinking, full native debug symbols).
  Needs `KEYSTORE_PASSWORD` and `KEY_PASSWORD` in `local.properties` (or as environment variables) and
  `optidoc.jks` in the repo root (git-ignored). AdMob IDs (`ADMOB_APP_ID`, `ADMOB_BANNER_ID`,
  `ADMOB_INTERSTITIAL_ID`) fall back to Google test IDs.
- CI: `.github/workflows/ci.yml` runs testDebugUnitTest, lintDebug, assembleDebug on push/PR to `main`; runs queue.
- All dependency versions live in `gradle/libs.versions.toml`.

## Layout
- Clean Architecture + MVVM + Hilt under `app/src/main/java/com/gokcank/optidoc/`:
  `data/` (`local/` Room, `scanner/`, `ocr/`, `export/`, `storage/`, `settings/`, `repository/`),
  `domain/` (`model/`, `repository/`, `usecase/`), `ui/` (screens + `components/` incl. ads), `di/`.
- `app/schemas/` — exported Room schemas (v1, v2). Never delete them.

## Project rules
- Everything stays on the device: no cloud, accounts, analytics or servers.
- PDFs use Android's native `PdfDocument`; do not add third-party PDF libraries.
- Room changes: bump the version, write an explicit migration, export the schema, extend `AppDatabaseMigrationTest`.
  Never use destructive migration.
- Navigation passes only IDs (e.g. `documentId: Long`); screens load paths from Room. Never put file paths or URIs in routes.
- Large images: decode with bounds check and `inSampleSize`; never decode full-resolution camera bitmaps directly.
- Exports go through `MediaStore` (JPEG → `Pictures/<App>`, PDF/TXT → `Documents/<App>`); ASCII-only folder names.
- `file://` URIs from internal storage are read as files, not via `ContentResolver`; share with `FileProvider`.
- R8 keep rules for `com.google.mlkit.**` and `com.google.android.gms.**` must stay; test a minified release build
  before any upload (release crashed on "New Scan" without them).
- String placeholders must be typed (`%1$s`, `%1$d`); resource XML is UTF-8 without BOM.
- Ads: adaptive banner; interstitial only after save/export with a 2-minute cooldown. Debug builds always use Google
  test ad IDs.
- Every user-visible string in both `values/strings.xml` and `values-tr/strings.xml`.

## Status (as of 2026-11-07)
- v1.3.1 (`versionCode = 7`, 2026-08-26). One local commit (version catalog migration, 2026-09-26) not yet pushed.

## Open items
- Backlog ideas, only on request: ID-card mode, PDF compression, encrypted vault, QR/barcode scanner.

## More context
Background, decisions and history are in the second brain: `/home/gokcank/ikinci-beyin/projects/optidoc.md` (read on demand).
