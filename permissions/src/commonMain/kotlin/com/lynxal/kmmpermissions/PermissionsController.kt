package com.lynxal.kmmpermissions

/**
 * Interface describing the methods for checking and asking for permissions.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface PermissionsController {
    /**
     * Requests the given [Permission] if it's not granted yet
     * @param permission the [Permission] is being requested
     * @throws PermissionDeniedException if user decides to reject the request
     * @throws PermissionDeniedPermanentlyException if the system blocks the request automatically, because the user rejected the permission before. In this case we should navigate the user to the app settings
     * @throws PermissionRequestCancelledException if the user decides to cancel the permission request (on Android)
     */
    suspend fun requestPermission(permission: Permission)

    /**
     * Requests the permission state for the given permission
     * @param permission the [Permission] is being requested
     * @return the state ([PermissionState]) of permission, on Android it can't be [PermissionState.DENIED_ALWAYS], on iOS it can't be [PermissionState.DENIED]
     */
    suspend fun requestPermissionState(permission: Permission): PermissionState

    /**
     * Checks whenever the permission is granted or not, true if granted, false in all other cases
     */
    suspend fun isGranted(permission: Permission): Boolean

    /**
     * In case if the permission was rejected permanently, this function will help with navigating to the app settings so the user can provide the permission manually.
     * @throws CannotOpenSettingsException if the settings screen could not be opened
     */
    suspend fun openAppSettings()
}
