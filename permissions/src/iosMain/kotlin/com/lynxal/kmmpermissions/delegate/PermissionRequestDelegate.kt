package com.lynxal.kmmpermissions.delegate

import com.lynxal.kmmpermissions.PermissionState

/**
 * This interface provides the main
 */
interface PermissionRequestDelegate {
    suspend fun requestPermission()
    suspend fun requestPermissionState() : PermissionState
}