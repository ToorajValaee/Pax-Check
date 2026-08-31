# Pax Check

Small Android hardware-test app for a **PAX A8900** using the bundled `NeptuneLiteApi_V4.15.00_20250606.jar`.

## Features

- NeptuneLite / DAL connection status
- Magnetic stripe reader test (Track 1/2/3)
- Thermal printer test
- On-screen hardware log

## Open the project

Open the **repository root** in Android Studio. There is only one Gradle project and one `:app` module.

## Requirements

- Android Studio with **JDK 17** selected as the Gradle JDK
- Android SDK 37
- PAX A8900 for hardware verification

The app currently targets API 37 and has `minSdk = 24`.

## Build

Windows PowerShell:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

macOS/Linux:

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

The debug APK is generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## PAX SDK

The Java API is committed at:

```text
app/libs/NeptuneLiteApi_V4.15.00_20250606.jar
```

`PaxSdkManager` uses the SDK directly:

```kotlin
NeptuneLiteUser.getInstance().getDal(applicationContext)
```

If the dashboard reports a native-runtime / `UnsatisfiedLinkError`, the Java project is loading correctly but the required PAX native runtime or device service is not available to the application. In that case inspect Logcat with the `PaxSdkManager` tag and verify the A8900 firmware/vendor SDK package and any native libraries supplied by PAX.

## Useful ADB commands

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb logcat -s PaxSdkManager PaxHardwareService
```
