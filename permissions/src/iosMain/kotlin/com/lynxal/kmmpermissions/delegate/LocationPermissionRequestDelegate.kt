package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionDeniedPermanentlyException
import com.lynxal.kmmpermissions.PermissionState
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class LocationPermissionRequestDelegate(
    private val locationManager: LocationManagerDelegate, private val permission: Permission
) : PermissionRequestDelegate {
    override suspend fun requestPermission() {
        provideLocationPermission(CLLocationManager.authorizationStatus())
    }

    override suspend fun requestPermissionState(): PermissionState =
        when (val status = CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionState.GRANTED
            kCLAuthorizationStatusNotDetermined -> PermissionState.UNDETERMINED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED_ALWAYS
            else -> error("Unknown location authorization status $status")
        }

    private suspend fun provideLocationPermission(status: CLAuthorizationStatus) {
        when (status) {
            kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> return

            kCLAuthorizationStatusNotDetermined -> {
                val updatedStatus = suspendCoroutine { continuation ->
                    locationManager.requestLocationAccess { continuation.resume(it) }
                }

                provideLocationPermission(updatedStatus)
            }

            kCLAuthorizationStatusDenied -> throw PermissionDeniedPermanentlyException(permission)
            else -> error("Unknown location authorization status $status")
        }
    }
}