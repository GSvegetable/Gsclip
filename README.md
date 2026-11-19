# Gsclip

Gsclip is a personal Android notes app built around two complementary ways to capture and organize thoughts:

- **Quick notes** — a calm, searchable list for writing something down immediately.
- **Free canvas** — a spatial board where note cards can be moved and arranged.

## Design direction

The visual language takes inspiration from [Glasmio](https://github.com/NEORUAA/Glasmio): restrained Liquid Glass surfaces, soft depth, rounded geometry, and deliberate motion. Glasmio provides the UI foundation; Gsclip owns the note experience, information architecture, and product identity.

## Current implementation

- Jetpack Compose Android application.
- Glasmio `0.1.2` scaffold and preference libraries.
- Search across note titles and bodies.
- Create and edit notes.
- List / canvas mode switch with draggable note cards.
- Local persistence for note content and canvas positions using Android SharedPreferences.
- GitHub Actions workflow for building a debug APK.

## Build

The GitHub Actions workflow installs JDK 21, Android SDK 37, and Gradle 9.6.0, then runs:

```bash
gradle :app:assembleDebug
```

The repository currently contains the Gradle wrapper properties but not the wrapper scripts or wrapper JAR. You can open the project in Android Studio with JDK 21 and Android SDK 37 installed, or use a locally installed Gradle 9.6.0.

## Next

The next development pass should focus on richer note editing, deleting and undoing changes, improved canvas gestures and layout, and more deliberate Glasmio surfaces and transitions. Device-level interaction testing is still required.

## License

Glasmio is Apache-2.0 licensed. Its license and third-party notices apply to the upstream library; Gsclip's own source is maintained separately.
