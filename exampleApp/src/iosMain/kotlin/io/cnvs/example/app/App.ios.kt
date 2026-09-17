package io.cnvs.example.app

import com.lynxal.kmmpermissions.PermissionControllerImpl
import io.cnvs.example.localnetwork.IosLocalNetworkTester
import io.cnvs.example.storage.IosStorageWriteTester

private lateinit var _appInfoInstance: AppInfo
actual val appInfoInstance: AppInfo
    get() = _appInfoInstance

fun initApp() {
    _appInfoInstance = object : AppInfo {
        override fun createPermissionController() = PermissionControllerImpl()
        override fun createLocalNetworkTester() = IosLocalNetworkTester()
        override fun createStorageWriteTester() = IosStorageWriteTester()
    }
}