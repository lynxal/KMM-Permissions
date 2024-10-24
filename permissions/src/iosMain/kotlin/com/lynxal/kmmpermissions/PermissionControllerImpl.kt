package com.lynxal.kmmpermissions

import com.lynxal.kmmpermissions.delegate.AlwaysGrantedPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.BluetoothPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.CameraPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.GalleryPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.LocationManagerDelegate
import com.lynxal.kmmpermissions.delegate.LocationPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.NotificationPermissionRequestDelegate
import com.lynxal.kmmpermissions.delegate.PermissionRequestDelegate
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class PermissionControllerImpl : PermissionsController {
    private val locationManagerDelegate by lazy { LocationManagerDelegate() }
    override suspend fun requestPermission(permission: Permission) {
        getPermissionRequestDelegate(permission).requestPermission()
    }

    override suspend fun requestPermissionState(permission: Permission): PermissionState =
        getPermissionRequestDelegate(permission).requestPermissionState()

    override suspend fun isGranted(permission: Permission): Boolean =
        getPermissionRequestDelegate(permission).requestPermissionState() == PermissionState.GRANTED

    override suspend fun openAppSettings() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.also { url ->
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            } else {
                throw CannotOpenSettingsException("Could not open the app settings")
            }
        }
    }

    private fun getPermissionRequestDelegate(permission: Permission): PermissionRequestDelegate {
        return when (permission) {
            Permission.FINE_LOCATION, Permission.COARSE_LOCATION -> LocationPermissionRequestDelegate(
                locationManagerDelegate, permission
            )

            Permission.BLUETOOTH_LE, Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_ADVERTISE, Permission.BLUETOOTH_CONNECT -> BluetoothPermissionRequestDelegate(
                permission
            )

            Permission.NOTIFICATIONS -> NotificationPermissionRequestDelegate()

            Permission.READ_STORAGE, Permission.WRITE_STORAGE -> AlwaysGrantedPermissionRequestDelegate()

            Permission.GALLERY -> GalleryPermissionRequestDelegate()
            Permission.CAMERA -> CameraPermissionRequestDelegate()
        }
    }
}