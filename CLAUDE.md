# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
./gradlew build

# Android app
./gradlew :app:assembleDebug
./gradlew :app:installDebug        # installs to connected device/emulator

# Desktop app
./gradlew :desktopApp:run
./gradlew :desktopApp:packageDistributionForCurrentOS

# Tests
./gradlew test                     # all unit tests
./gradlew :shared:desktopTest      # desktop-only tests
./gradlew connectedAndroidTest     # instrumented tests (requires device/emulator)

./gradlew clean
```

## Architecture Overview

**Kotlin Multiplatform** targeting Android (`:app`) and Desktop JVM (`:desktopApp`), with all shared logic in `:shared`.

### Module Responsibilities
- `:app` — Android `MainActivity`, Glance widget (`PromptStashWidget`), Dropbox OAuth redirect
- `:shared` — All UI (Compose Multiplatform), ViewModels, repositories, Room DB, DataStore, Ktor networking
- `:desktopApp` — Thin JVM entry point (`Main.kt`), 400×840 window

### Dependency Injection
Manual DI via `AppContainer.kt` (no Koin/Hilt). `createAppContainer(...)` is called once per platform entry point and injected into the Compose tree via `LocalAppContainer` (`CompositionLocal`). ViewModels pull dependencies from this container.

### Presentation Layer
MVVM with `StateFlow<UiState>` for screen state and `SharedFlow<Event>` for one-off events (navigation, toasts). Composables use `collectAsStateWithLifecycle()`. Each screen has a `*ViewModel`, `*Screen.kt` composable, and `*UiState`/`*Event` types.

### Repository Pattern
`PromptRepository` interface with two implementations:
- `RoomPromptRepository` — Room DAO + sync metadata fields (`modifiedAt`, `deletedAt`, `modifiedByDeviceId`)
- `SyncingPromptRepository` — decorator that wraps the above and triggers Dropbox sync on mutations

### Sync Architecture
`PromptSyncCoordinator` orchestrates sync between `PromptSyncLocalStore` (Room) and `DropboxPromptSyncRemote` (Ktor). Conflict resolution uses last-write-wins via `modifiedAt` timestamps + device UUIDs stored in DataStore.

### Navigation
Jetbrains Navigation3 (multiplatform, alpha). Routes are `@Serializable` sealed interface subclasses in `Screen.kt`: `Library`, `Editor(promptId: String?)`, `Settings`.

### Persistence
- **Room 2.8.4** — `PromptDatabase` with single `PromptEntity` table; schema tracked in `schemas/`
- **DataStore 1.2.1** — User preferences (theme, remote type, pinned prompts, device UUID, sync state)

### Platform Abstractions
`expect/actual` used for: `PlatformViewModel` factory, `SecureCredentialStore`, `DropboxAuthPlatform`, `createPlatformHttpClient()`.

## Key Configuration

Dropbox API key goes in `local.properties` (gitignored):
```
dropbox.app.key=YOUR_KEY_HERE
```
The build system generates `DropboxBuildConfig.kt` from this property.

## Conventions

- **Naming**: `*UiState`, `*Event`, `*ViewModel` per screen; `*Repository` interface + `Room*Repository` / `Syncing*Repository` impls
- **Kotlin version**: 2.2.21 — use KSP (not KAPT) for annotation processing
- **Compile/Target SDK**: 36, Min SDK: 24
