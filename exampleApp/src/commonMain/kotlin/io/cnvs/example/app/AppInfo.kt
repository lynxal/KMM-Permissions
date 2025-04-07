package io.cnvs.example.app

import com.lynxal.kmmpermissions.PermissionsController

interface AppInfo {
    fun createPermissionController(): PermissionsController
}

expect val appInfoInstance: AppInfo