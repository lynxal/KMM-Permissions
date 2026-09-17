0.0.7
WRITE_STORAGE works again on modern Android. Since 0.0.5 it reported UNAVAILABLE on Android 13 and
above, which PermissionState.isDenied() counts as denied, so consumers gated their saves off on
devices where writing was fine. It now reports GRANTED without raising a dialog once the app has
left the legacy view of external storage, since a MediaStore write needs no permission from
Android 10 on, and keeps the old runtime request below that. GRANTED means the app's own media
through the MediaStore, not an arbitrary path; see the README.
The example app writes a real file so the state can be checked against what actually happens, and
caps its WRITE_EXTERNAL_STORAGE declaration at maxSdkVersion="29".
Added the LOCAL_NETWORK permission: android.permission.ACCESS_LOCAL_NETWORK on Android 17 and above,
and a Bonjour-based check on iOS, where the system offers no API to read local network authorization.
See the README for the manifest entry and the two Info.plist keys each platform needs.
Dropped the iosX64 target and moved to Gradle 9.7.1, AGP 9.4.0 and Kotlin 2.4.20.
Note for consumers that persist Permission.ordinal: LOCAL_NETWORK is appended at the end of the enum,
so existing ordinals keep their meaning.

0.0.6
Updated dependencies and performed code cleanup

0.0.5
In this release, we have temporarily disabled the request for WRITE_STORAGE permission on devices
running Android 13 and higher.