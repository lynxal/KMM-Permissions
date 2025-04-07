package io.cnvs.example.app

import com.lynxal.kmmpermissions.PermissionControllerImpl

private lateinit var _appInfoInstance: AppInfo
actual val appInfoInstance: AppInfo
    get() = _appInfoInstance

fun initApp() {
    _appInfoInstance = object : AppInfo {
        override fun createPermissionController() = PermissionControllerImpl()
    }
}