# Pokémon Quiz

Guess the Pokémon from its picture, beat the timer and climb the leaderboard. Pokémon data comes from the [Tyradex API](https://tyradex.vercel.app).

The same quiz is built twice:

| Folder | Stack | Platforms |
| --- | --- | --- |
| [`www/`](./www) | Apache Cordova (HTML / CSS / JS) | Android, iOS, browser |
| [`tp2/`](./tp2) | Kotlin Multiplatform + Compose Multiplatform | Android, iOS |

## Features

- Timed quiz with score
- Local leaderboard
- Offline detection (network status)
- Vibration feedback through a custom Cordova plugin ([`cordova-vibration-plugin/`](./cordova-vibration-plugin))

## Cordova app

```bash
npm install
npx cordova platform add android   # or ios / browser
npx cordova run android
```

## Kotlin Multiplatform app

```bash
cd tp2
./gradlew :composeApp:assembleDebug
```

Open `tp2/iosApp` in Xcode to run the iOS app. See [`tp2/README.md`](./tp2/README.md) for details.

## UI tests

End-to-end scenarios for the KMP app are written with [Maestro](https://maestro.mobile.dev) in [`tp2/maestro/`](./tp2/maestro):

```bash
maestro test tp2/maestro
```
