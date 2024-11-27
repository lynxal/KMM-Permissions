package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.PermissionState
import platform.Photos.PHAuthorizationStatus
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class GalleryPermissionRequestDelegate : PermissionRequestDelegate {
    override suspend fun requestPermission() {
        providePermission(PHPhotoLibrary.authorizationStatus())
    }

    override suspend fun requestPermissionState(): PermissionState =
        when(val status = PHPhotoLibrary.authorizationStatus()) {
            PHAuthorizationStatusAuthorized -> PermissionState.GRANTED
            PHAuthorizationStatusNotDetermined -> PermissionState.UNDETERMINED
            PHAuthorizationStatusDenied -> PermissionState.DENIED_ALWAYS
            else -> error("Unknown gallery authorization status $status")
        }
    private suspend fun providePermission(status: PHAuthorizationStatus) {
        return when(status) {
            PHAuthorizationStatusAuthorized -> return
            PHAuthorizationStatusNotDetermined -> {
                val newStatus = suspendCoroutine<PHAuthorizationStatus> { continuation ->
                    PHPhotoLibrary.requestAuthorization { continuation.resume(it) }
                }
                providePermission(newStatus)
            }
            else -> error("Unknown gallery authorization status $status")
        }
    }
}
