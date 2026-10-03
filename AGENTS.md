# ElainaStream — Agent Instructions

## Project Overview

ElainaStream is an Android anime streaming app built with Kotlin and Jetpack
Compose (Material3). Single-activity architecture with manual screen-stack
navigation (no Navigation-Compose).

- Kotlin 2.1.0 | AGP 8.13.0 | Gradle 9.0.0 | JDK 17
- minSdk 21, targetSdk 34, compileSdk 36
- Video: Media3 ExoPlayer 1.4.1
- Network: Retrofit 2.11 + OkHttp 4.12 + Gson
- Images: Coil 2.7 | Async: Coroutines 1.8.1
- No DI framework — dependencies are wired manually (each ViewModel has a `Factory`).

## Data Sources

Two API backends behind the `AnimeSource` interface, selected at runtime via
`SourcePreferences` / `AnimeRepository`:

- **AnimeX** (`AnimeXSource`): POST form-encoded API, AES-GCM encrypted video
  URLs decrypted with `CryptoUtils`.
- **Animeku** (`AnimekuSource`): GET API with an `api_key` query param.

Remote config (credentials, server URLs) is synced by `AppConfigManager`.

## Build & Test — READ FIRST

- **DO NOT build the APK in a local terminal.** This environment has no Android SDK.
- Push changes to the `dev` branch. GitHub Actions
  (`.github/workflows/build-apk.yml`) automatically builds the debug APK and
  runs unit tests on every push to `main` or `dev`.
- Download the built APK from the repo's Actions tab → Artifacts
  (`ElainaStream-debug`) to test on a device.
- Never commit code that breaks `./gradlew testDebugUnitTest`.

## Git Workflow

- `main` — stable, always green. Never push experiments directly here.
- `dev` — active development and trial-and-error. Merge to `main` only when stable.
- Commit messages: short imperative summary,
  e.g. "Fix resume position leaking between episodes".

## Code Conventions

- UI state uses the `UiState<T>` sealed interface (Loading / Success / Error)
  combined with `collectAsStateWithLifecycle()`.
- ViewModels expose `StateFlow`. Cancel the previous `Job` before starting a new
  load so stale responses can't overwrite fresh ones.
- Heavy I/O (disk, network, Gson parsing) must run off the main thread
  (`Dispatchers.IO`). Never do heavy work directly inside a `@Composable`.
- Network DTOs are nullable — map them to domain models with `mapNotNull`.
  Never force-unwrap network data with `!!`.
- Keep user-visible strings in Indonesian (the app's language).
- Playback progress is throttled (max one save per 15s) — keep it that way.
