# Gsclip

Gsclip is a personal Android notes app built around two complementary ways to capture and organize thoughts:

- **Quick notes** — a calm, searchable list for writing something down immediately.
- **Free canvas** — a spatial board where note cards can be moved and arranged.

## Design direction

The visual language takes inspiration from [Glasmio](https://github.com/NEORUAA/Glasmio): restrained Liquid Glass surfaces, soft depth, rounded geometry, and deliberate motion. Glasmio provides the UI foundation; Gsclip owns the note experience, information architecture, and product identity.

## Current implementation

- Jetpack Compose Android application (Android 13 / API 33 and newer).
- Glasmio `0.1.3` scaffold and preference libraries.
- Search across note titles and bodies.
- Create, edit, and delete notes.
- Undo the most recent deletion during the current app session.
- List / canvas mode switch with draggable note cards.
- Local persistence for note content and canvas positions using Android SharedPreferences.
- Empty states and a soft rotating note-color palette.
- GitHub Actions workflow that builds and uploads a debug APK artifact.

## Build

The GitHub Actions workflow installs JDK 21, Android SDK 37, and Gradle 9.6.0, then runs:

```bash
gradle :app:assembleDebug
```

The repository contains the Gradle wrapper properties but not the wrapper scripts or wrapper JAR. Open the project in Android Studio with JDK 21 and Android SDK 37 installed, or use a local Gradle 9.6.0 installation.

## Next

Planned work includes richer text editing, undo for edits, improved canvas gestures and layout, and more deliberate Glasmio surfaces and transitions. Device-level interaction testing and a successful CI build are still required before calling this release-ready.

## License

Glasmio is Apache-2.0 licensed. Its license and third-party notices apply to the upstream library; Gsclip's own source is maintained separately.
