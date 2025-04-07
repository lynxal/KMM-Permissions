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

---

## Installation

Add the following dependency to your shared module's `build.gradle.kts` file:

```kotlin
implementation("com.lynxal.permissions:permissions:0.0.5")
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
WRITE_STORAGE - At this moment write to external storage is not supported on Android. We are working on this
GALLERY
CAMERA
```

The KmmPermissions library simplifies permission management in KMM projects. It provides a Compose-friendly API and platform-specific support for Android and iOS.

*For issues, suggestions, or contributions, feel free to open a pull request or issue on the repository.*