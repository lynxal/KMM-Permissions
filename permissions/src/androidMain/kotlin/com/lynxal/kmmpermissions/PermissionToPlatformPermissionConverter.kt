package com.lynxal.kmmpermissions

fun Permission.toPlatformPermission(): List<String> = when(this) {
    Permission.FINE_LOCATION -> PlatformPermissions.fineLocation
    Permission.COARSE_LOCATION -> PlatformPermissions.coarseLocation
    Permission.BLUETOOTH_LE -> PlatformPermissions.bluetoothLe
    Permission.BLUETOOTH_SCAN -> PlatformPermissions.bluetoothScan
    Permission.BLUETOOTH_ADVERTISE -> PlatformPermissions.bluetoothAdvertise
    Permission.BLUETOOTH_CONNECT -> PlatformPermissions.bluetoothConnect
    Permission.NOTIFICATIONS -> PlatformPermissions.notifications
    Permission.READ_STORAGE -> PlatformPermissions.storage
    Permission.WRITE_STORAGE -> PlatformPermissions.writeStorage
    Permission.GALLERY -> PlatformPermissions.gallery
    Permission.CAMERA -> PlatformPermissions.camera
}
