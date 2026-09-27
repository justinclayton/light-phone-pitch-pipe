# Pitch Pipe for the Light Phone III

A one-octave chromatic pitch pipe built as a LightOS tool. Tap a note and it drones until you tap it again or pick another note. That's the whole tool.

It exists for the obvious use case: you sing, or you tune by ear, and you'd rather carry the Light Phone than a plastic pipe.

## What it does

- **Twelve notes, C4 through B4**, in a 3×4 grid. A4 is 440 Hz.
- **Tap to sound, tap again to stop.** Tapping a different note switches immediately.
- **Sustained drone.** The note holds for up to ten minutes, long enough to find your starting pitch and then some.
- **No permissions, no network, no accounts.** The tool declares an empty permission list and only reads its own bundled audio.
- **Follows the system theme.** Light and dark themes come from the SDK's theme controller, so the grid matches the rest of LightOS.

## How the drone works

The Light audio player has no repeat mode, so a looping tone can't be expressed as "play this file forever." Instead, each note is a 3‑second, 16‑bit, 44.1 kHz mono WAV cut to an exact integer number of cycles, so the end of the file lines up phase‑perfectly with the start. The view model queues the same file 200 times back to back. PCM transitions between queue items are gapless, which turns 200 identical clips into one continuous tone for ten minutes.

Tone files live in [`tool/src/main/assets/tones/`](tool/src/main/assets/tones/), named by pitch class (`cs4.wav` is C♯4, and so on).

## Project layout

This repository is a fork of the [Light SDK](https://github.com/lightphone/light-sdk) scaffold. The SDK ships a placeholder `tool` module that you replace with your own code, and that is exactly what happened here. Everything specific to Pitch Pipe lives in one place:

| Path | What it is |
|------|------------|
| [`tool/lighttool.toml`](tool/lighttool.toml) | Tool metadata: id, label, version, permissions, which LightOS server package to bind to |
| [`tool/src/main/kotlin/com/thelightphone/pitchpipe/PitchPipeScreen.kt`](tool/src/main/kotlin/com/thelightphone/pitchpipe/PitchPipeScreen.kt) | The single screen and its view model |
| [`tool/src/main/kotlin/com/thelightphone/pitchpipe/ToolEntryPoint.kt`](tool/src/main/kotlin/com/thelightphone/pitchpipe/ToolEntryPoint.kt) | Lifecycle entry point (no-op; this tool has no server data or push handling) |
| [`tool/src/main/assets/tones/`](tool/src/main/assets/tones/) | The twelve WAV files |

Everything else (`sdk/`, `plugin/`, `lint-rules/`, `examples/`, `docs/`) is upstream SDK scaffolding, kept intact so the tool builds and so upstream changes can be pulled in. The [`docs/`](docs/) folder is the SDK's own documentation and is the right place to look for how the primitives work.

## Building and running

You need Android Studio (or a JDK 17 plus the Android SDK) and either a Light Phone III or an Android emulator. The SDK's Gradle plugin generates the Android manifest from `lighttool.toml`, so there is no manifest to edit by hand.

### On the LightOS emulator

The emulator path is the one that works out of the box. `lighttool.toml` ships pointed at the emulator's server package.

1. Set up an Android emulator running the LightOS emulator app as a system app. The SDK's [emulator guide](docs/system_app) walks through it. A 1080×1240 AVD on API 34 without Google Play services gets you close to real hardware.
2. Install and launch:

```bash
./gradlew :tool:installDebug
adb shell am start -n com.thelightphone.pitchpipe/com.thelightphone.sdk.LightActivity
```

### On a real Light Phone III

Sideloading over ADB works today, but the tool has to bind to the real LightOS package rather than the emulator. In [`tool/lighttool.toml`](tool/lighttool.toml), swap the `serverPackage` lines:

```toml
# serverPackage = "com.thelightphone.sdk.emulator"
serverPackage = "com.lightos"
```

Then install with the same two commands above. Builds are signed with the SDK's shared dev key, so LightOS will treat this as an unsigned community tool until Light's build-and-sign service is available. The upstream README and [`CONTRIBUTING.md`](CONTRIBUTING.md) describe where that process stands.

### Checks

CI runs `./gradlew check` on every pull request. The same command works locally.

## Roadmap, loosely

Things that would make this a better pitch pipe without making it a bigger one:

- A second octave, or an octave toggle, for lower voices.
- A concert pitch setting (A = 442 Hz, 415 Hz for early music).
- Remember the last note between launches.

## License

MIT, same as the SDK it's built on. See [`LICENSE`](LICENSE).
