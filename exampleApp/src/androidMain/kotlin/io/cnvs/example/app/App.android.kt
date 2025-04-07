package io.cnvs.example.app

import android.app.Application
import com.lynxal.kmmpermissions.PermissionControllerImpl
import com.lynxal.kmmpermissions.PermissionsController
import com.lynxal.logging.DebugLoggerImplementation
import com.lynxal.logging.Logger

private lateinit var _appInfoInstance: AppInfo
actual val appInfoInstance: AppInfo
    get() = _appInfoInstance

class AndroidApp : Application(), AppInfo {
    override fun onCreate() {
        super.onCreate()
        _appInfoInstance = this
        Logger.add(DebugLoggerImplementation())
    }

    override fun createPermissionController(): PermissionsController =
        PermissionControllerImpl(this)
}