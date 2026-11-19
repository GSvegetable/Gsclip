# Gsclip

Gsclip is a personal Android notes app exploring two complementary ways to capture thoughts:

- **Quick notes** — a calm, searchable list for writing things down immediately.
- **Free canvas** — a spatial board where note cards can be moved around freely.

## Design direction

The visual language takes inspiration from [Glasmio](https://github.com/NEORUAA/Glasmio): restrained Liquid Glass surfaces, soft depth, rounded geometry, and deliberate motion. Glasmio is used as a UI foundation; Gsclip owns the note experience, information architecture, and product identity.

## Current foundation

- Jetpack Compose Android application scaffold.
- Glasmio `0.1.3` scaffold and preference libraries.
- List / canvas mode switch.
- Search across note titles and bodies.
- Create and edit note flow.
- Draggable note cards in the canvas view.

The current prototype keeps notes in memory. Persistent storage, undo, richer text editing, and polished glass interactions are planned next.

## Build

Open this repository in Android Studio with JDK 21 and Android SDK 37 installed. The project currently does not include the Gradle wrapper files; generate or add the wrapper before using `./gradlew`.

## License

Glasmio is Apache-2.0 licensed. Its license and third-party notices apply to the upstream library; Gsclip's own source is maintained separately.
