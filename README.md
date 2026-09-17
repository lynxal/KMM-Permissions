# KMMPermissions

**KmmPermissions** is inspired by the [Moko-Permissions](https://github.com/icerockdev/moko-permissions) library, sharing many similarities. However, the key difference is its focus on modern platforms—dropping support for older iOS devices and adapting to the latest changes introduced by Apple.

> **Note:** A bug has been reported to the Moko team but remains unresolved ([issue #119](https://github.com/icerockdev/moko-permissions/issues/119)).

This library consists of three core components:
- **`PermissionController`**
- **`PermissionState`**
- **`BindEffect`** composable function

## Key Considerations

- The library is built for **Compose Multiplatform / Jetpack compose** and relies heavily on composable functions.
- It is designed for **Kotlin Multiplatform Mobile (KMM)** projects targeting both Android and iOS.

## Supported targets

| Platform | Targets |
|---|---|
| Android | `android` |
| iOS | `iosArm64`, `iosSimulatorArm64` |

> **Note:** `iosX64` (Intel simulator) is no longer supported as of version 0.0.7.
> Compose Multiplatform and the JetBrains androidx artifacts stopped publishing it,
> and all current Macs run Apple Silicon. Intel-simulator consumers should stay on 0.0.6.

---

## Installation

Add the following dependency to your shared module's `build.gradle.kts` file:

```kotlin
implementation("com.lynxal.permissions:permissions:0.0.7")
```


---

## Getting Started

### 1. Create a Platform-Specific `PermissionControllerImpl`

You need to create a `PermissionControllerImpl` for your specific platform (Android or iOS). You can use any Dependency Injection (DI) framework or instantiate it manually.

- **For Android**, ensure that the `appContext` is provided when creating the controller.

Here’s an example implementation in a `ViewModel` or `ScreenModel`:

```kotlin
// For Android
val permissionController = PermissionControllerImpl(appContext)

// For iOS
val permissionController = PermissionControllerImpl()
```
***Note:*** Holding a reference to appContext is acceptable. However, the BindEffect composable will clear its internal (weak) reference to appContext after the screen disappears.

### 2. Use the BindEffect Composable Function

The BindEffect composable function should be called from the root of your screen’s composition function. This ensures proper lifecycle handling. There’s no need to hoist the call to higher levels.

```kotlin
@Composable
override fun LocationPermissionRequestScreen() {
    val viewModel = ...

    // Place the BindEffect call at the root of the screen
    BindEffect(viewModel.permissionController)
}
```

***Note:*** The PermissionController instance remains constant and does not change over time.

### 3. Track Permission State in Your ViewModel/ScreenModel

In your ViewModel or ScreenModel, define an instance variable to track the permission request result. Typically, you can initialize it as follows:

```kotlin
val permissionState = mutableStateOf<PermissionState>(PermissionState.UNKNOWN)
```

### 4. Check Permission State

Use the requestPermissionState method to check the current permission state. For example, to verify access to Bluetooth Low Energy (BLE) functionality:

```kotlin
permissionController.requestPermissionState(Permission.BLUETOOTH_LE)
```

### 5. Request Permissions

Create a method in your ViewModel or ScreenModel to request permissions for a specific resource. Handle exceptions as needed and update the permission state accordingly.

```kotlin
fun requestPermission() {
    try {
        permissionController.requestPermission(Permission.BLUETOOTH_LE)
    } catch (e: PermissionException) {
        Logger.error("Failed to request Bluetooth permissions", e)
    }
    permissionState.value = permissionController.requestPermissionState(Permission.BLUETOOTH_LE)
}
```

### 6. Observe Permission State in a Composable

Observe the permissionState from your composable function. Ensure that the observation is as close as possible to where the value is being used. Any change in permissionState will trigger recomposition of the relevant UI segment.

```kotlin
val permissionState by remember { homeViewContract.permissionState }

// Adjust the UI based on the permission state
when (permissionState) {
    PermissionState.DENIED, PermissionState.DENIED_ALWAYS -> { /* Handle denied state */ }
    PermissionState.UNKNOWN, PermissionState.UNDETERMINED -> { /* Handle unknown state */ }
    PermissionState.UNAVAILABLE -> { /* Handle unavailable state */ }
    PermissionState.GRANTED -> { /* Handle granted state */ }
}
```

## Supported Permissions

```
FINE_LOCATION
COARSE_LOCATION
BLUETOOTH_LE
BLUETOOTH_SCAN
BLUETOOTH_ADVERTISE
BLUETOOTH_CONNECT
NOTIFICATIONS
READ_STORAGE
WRITE_STORAGE - see the section below: granted without a dialog on Android 10 and above
GALLERY
CAMERA
LOCAL_NETWORK
```

### WRITE_STORAGE

Writing to shared storage. On Android it needs a manifest entry, capped at the last version where
the permission still means anything:

```xml
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="29" />
```

`WRITE_EXTERNAL_STORAGE` has no effect from Android 10 (API 29) on. An app that writes through the
MediaStore owns the files it creates and needs no storage permission at all, so from there the
library reports `GRANTED` and `requestPermission` returns without raising a dialog the system would
ignore. Below that the permission is requested and reported as it always was, and so it is for an
app that still holds the legacy view of external storage, which an app targeting API 29 can keep on
a much newer device. The library reads the view itself (`Environment.isExternalStorageLegacy()`)
rather than the device API level, so both cases answer correctly.

#### What counts as yours

From Android 10 on, what an app may do with a file depends on who owns it, not on a permission:

- Your app can create files in the shared folders (Downloads, Documents, DCIM, Pictures) with no
  permission at all, and it owns what it creates. It can read, change and delete its own files.
- Files another app created are closed to it. Reading them needs the `READ_MEDIA_*` permissions,
  and changing or deleting them needs the user to confirm, through `MediaStore.createDeleteRequest`
  or `createWriteRequest`.
- Uninstalling drops that ownership. Files the app leaves behind are no longer its own, so a
  reinstalled app cannot delete them by itself. This surprises people during testing, when the same
  app is installed over and over.

So `GRANTED` means "this app can write its own files", which is all the old permission still
covers. An app that needs to reach every file on the device wants All files access
(`MANAGE_EXTERNAL_STORAGE`), which this library does not offer yet.

On iOS the permission is always granted: the app's Documents directory is its own sandbox and iOS
asks for nothing. The example app's `StorageWriteTester` writes a real file on both platforms, which
is the only way to see the difference between a granted state and a write that actually lands.

### LOCAL_NETWORK

Local network access: mDNS/Bonjour discovery and plain LAN sockets. It needs setup on both platforms.

**Android.** Declare the permission in the app manifest:

```xml
<uses-permission android:name="android.permission.ACCESS_LOCAL_NETWORK" />
```

It is a runtime permission from Android 17 (API 37) on, and only for apps that target API 37 or
higher. On an older device, or while the app targets an older SDK, access is granted implicitly:
the library then reports `GRANTED` and never shows a dialog, which is what Android asks for: an
app targeting API 36 or lower must not request the permission.

**iOS.** Add both keys to the app's `Info.plist`:

```xml
<key>NSLocalNetworkUsageDescription</key>
<string>Explain here why your app needs the local network.</string>
<key>NSBonjourServices</key>
<array>
    <string>_kmmpermissions._tcp</string>
</array>
```

iOS publishes no API for reading local network authorization, so the library infers it: it
advertises a throwaway `_kmmpermissions._tcp` service and browses for that same service. Finding it
means access is allowed; a "policy denied" answer from the DNS layer means the user said no. Four
things follow from that:

- The first `requestPermission` is what raises the system dialog. A check never prompts: until the
  app has asked, `requestPermissionState` answers `UNDETERMINED` instead of probing. After that it
  probes on every read, which no longer raises a dialog because the system has already answered.
- What iOS cannot tell the library: if your own code reaches the local network first (an mDNS
  browse, a socket to a LAN address), the system raises the dialog itself and nothing reports the
  answer back. Until the library probes, the check still says `UNDETERMINED`. Either ask through
  `requestPermission` before touching the network, or take your own network result as the answer:
  services discovered means access works, a "policy denied" error means it does not. The example
  app's `LocalNetworkTester` shows the second pattern.
- A denial reads back as `DENIED_ALWAYS`. iOS asks once, and only the Settings app can change the
  answer afterwards.
- Without the two `Info.plist` keys the probe cannot run and the permission reports `UNAVAILABLE`.
  On the iOS Simulator the restriction is not enforced, so the probe always reports `GRANTED`, so
  test this permission on a real device.

The KmmPermissions library simplifies permission management in KMM projects. It provides a Compose-friendly API and platform-specific support for Android and iOS.

*For issues, suggestions, or contributions, feel free to open a pull request or issue on the repository.*