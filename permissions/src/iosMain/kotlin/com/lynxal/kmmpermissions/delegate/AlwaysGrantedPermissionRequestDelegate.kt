package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.PermissionState

internal class AlwaysGrantedPermissionRequestDelegate : PermissionRequestDelegate {
    override suspend fun requestPermission() = Unit
    override suspend fun requestPermissionState(): PermissionState = PermissionState.GRANTED
}