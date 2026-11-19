# Gsclip

A calm, tactile note space for Android — combining quick notes with a freeform idea canvas.

## Direction

Gsclip takes visual inspiration from Apple's Liquid Glass language and the open-source [Glasmio](https://github.com/NEORUAA/Glasmio) Jetpack Compose library. The goal is not to imitate an iOS app screen-for-screen, but to bring its material depth, translucency, restrained motion and touch feedback into a useful note-taking product.

## Current foundation

- Native Android app built with Kotlin and Jetpack Compose
- Glasmio theme and backdrop integration
- Notes list with live search
- Note editor with title, body and color selection
- Local persistence using Android SharedPreferences
- Freeform canvas with draggable sticky notes
- Adaptive Compose layout foundation for phones and tablets

## Build

Open the project in Android Studio with JDK 21 and Android SDK 37 installed. Allow Gradle to sync, then run the app configuration on an Android 13+ device.

The project uses Glasmio 0.1.3. See its repository for library details and licensing: https://github.com/NEORUAA/Glasmio

## Roadmap

- Canvas pinch zoom and panning
- Rich text, attachments and image cards
- Undo/redo and archive
- Tablet two-pane workspace
- Export, backup and restore
- UI polish and accessibility pass
