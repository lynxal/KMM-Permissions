package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.Permission
import com.lynxal.kmmpermissions.PermissionDeniedPermanentlyException
import com.lynxal.kmmpermissions.PermissionState
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class CameraPermissionRequestDelegate : PermissionRequestDelegate {
    override suspend fun requestPermission() {
        when (val state = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> return
            AVAuthorizationStatusNotDetermined -> {
                val isGranted = suspendCoroutine { continuation ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) {
                        continuation.resume(it)
                    }
                }

                if (isGranted) {
                    return
                } else {
                    throw PermissionDeniedPermanentlyException(permission = Permission.CAMERA)
                }
            }

            AVAuthorizationStatusDenied -> throw PermissionDeniedPermanentlyException(permission = Permission.CAMERA)
            AVAuthorizationStatusRestricted -> PermissionState.GRANTED
            else -> error("Unknown authorization state $state")
        }
    }

    override suspend fun requestPermissionState(): PermissionState =
        when (val state = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> PermissionState.GRANTED
            AVAuthorizationStatusNotDetermined -> PermissionState.UNDETERMINED
            AVAuthorizationStatusDenied -> PermissionState.DENIED_ALWAYS
            AVAuthorizationStatusRestricted -> PermissionState.GRANTED
            else -> error("Unknown authorization state $state")
        }
}