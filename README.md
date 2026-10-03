# ElainaStream

Aplikasi streaming anime untuk Android, dibangun dengan Kotlin dan Jetpack Compose.

![Build](https://github.com/shirayukiimountain/ElainaStream/actions/workflows/build-apk.yml/badge.svg)

## Fitur

- 🎬 Nonton anime dengan ExoPlayer (multi-kualitas)
- 🔍 Cari anime & jelajahi berdasarkan genre
- ⏯️ Lanjutkan menonton — posisi terakhir tersimpan otomatis
- 📜 Riwayat tontonan
- 🔄 Sumber API ganda, bisa diganti lewat Settings

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- Media3 ExoPlayer
- Retrofit + OkHttp + Gson
- Coil (image loading)
- Coroutines

## Cara Build

Butuh JDK 17 dan Android SDK (atau Android Studio):

```bash
./gradlew assembleDebug
```

APK hasil build ada di `app/build/outputs/apk/debug/app-debug.apk`.

Alternatif: download APK terbaru dari tab [Actions](../../actions) → pilih run terakhir → bagian Artifacts.

## Unit Test

```bash
./gradlew testDebugUnitTest
```

## CI

Setiap push ke `main` otomatis menjalankan GitHub Actions: build debug APK + unit test. Workflow-nya ada di `.github/workflows/build-apk.yml`.
