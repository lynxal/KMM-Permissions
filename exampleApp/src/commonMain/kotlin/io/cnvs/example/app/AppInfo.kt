package io.cnvs.example.app

import com.lynxal.kmmpermissions.PermissionsController
import io.cnvs.example.localnetwork.LocalNetworkTester
import io.cnvs.example.storage.StorageWriteTester

interface AppInfo {
    fun createPermissionController(): PermissionsController
    fun createLocalNetworkTester(): LocalNetworkTester
    fun createStorageWriteTester(): StorageWriteTester
}

expect val appInfoInstance: AppInfo