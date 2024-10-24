package com.lynxal.kmmpermissions

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface PermissionsController {
    actual suspend fun requestPermission(permission: Permission)
    actual suspend fun requestPermissionState(permission: Permission): PermissionState
    actual suspend fun isGranted(permission: Permission): Boolean
    actual suspend fun openAppSettings()
}