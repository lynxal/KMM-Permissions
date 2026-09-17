package io.cnvs.example.app

import android.app.Application
import com.lynxal.kmmpermissions.PermissionControllerImpl
import com.lynxal.kmmpermissions.PermissionsController
import com.lynxal.logging.DebugLoggerImplementation
import com.lynxal.logging.Logger
import io.cnvs.example.localnetwork.AndroidLocalNetworkTester
import io.cnvs.example.localnetwork.LocalNetworkTester
import io.cnvs.example.storage.AndroidStorageWriteTester
import io.cnvs.example.storage.StorageWriteTester

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

    override fun createLocalNetworkTester(): LocalNetworkTester = AndroidLocalNetworkTester(this)

    override fun createStorageWriteTester(): StorageWriteTester = AndroidStorageWriteTester(this)
}