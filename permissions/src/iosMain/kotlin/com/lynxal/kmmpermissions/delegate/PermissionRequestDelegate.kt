package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.PermissionState

/**
 * This interface provides the main
 */
internal interface PermissionRequestDelegate {
    suspend fun requestPermission()
    suspend fun requestPermissionState() : PermissionState
}