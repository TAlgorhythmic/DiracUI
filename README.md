# DiracUI – Open-source Dirac Audio control app for Android

DiracUI is a lightweight, modern open-source client for the **Dirac Audio Control Service** (`se.dirac.acs`) on **Oplus phones (OPPO, OnePlus, Realme)**. It unlocks Dirac sound settings the stock app hides, like the multi-band equalizer, stereo width and tonal balance.

> ℹ️ **Compatibility:** DiracUI only works with the Dirac service version shipped on Oplus Ossi, or other phones with the same or a similar version. Other versions of the service use a different interface and won't work.

## Features

- Toggle **Dirac HD** audio on and off
- Pick headphone/device profile and use case
- Enable or disable the filter, SFX and equalizer
- Multi-band **equalizer** (-12 dB to +12 dB)
- Hidden **Stereo width**, **tonal balance** and **loudness** controls
- Separate presets for the internal speaker and headphones
- Switches presets when wired or Bluetooth headphones connect

## Requirements

- Android 6.0 (API 23) or newer
- The Oplus version of the Dirac Audio Control Service (`se.dirac.acs`), or a compatible one

## Build

```sh
./gradlew assembleDebug
```

JDK 21 is needed; Gradle picks it up automatically if it's installed. The APK is written to `build/outputs/apk/debug/`.

## How it works

DiracUI talks directly to the Dirac Audio Control Service over its AIDL interface (`src/main/aidl/se/dirac/acs/api`), documented inline. It is written from scratch rather than modeled on the stock app, so it exposes features the stock app leaves unused.

## License

The source code is released under the [MIT License](LICENSE).

## Disclaimer

The Dirac logo used as the app icon is owned by **Dirac Research AB**. It is **not** covered by the MIT License and all rights remain with its owner.

This project is not affiliated with, endorsed by or sponsored by Dirac Research AB. "Dirac" and related names are trademarks of their respective owners.
