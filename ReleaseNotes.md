0.0.7
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