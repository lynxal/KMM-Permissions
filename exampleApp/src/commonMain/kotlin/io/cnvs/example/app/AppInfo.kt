package io.cnvs.example.app

import com.lynxal.kmmpermissions.PermissionsController
import io.cnvs.example.localnetwork.LocalNetworkTester

interface AppInfo {
    fun createPermissionController(): PermissionsController
    fun createLocalNetworkTester(): LocalNetworkTester
}

expect val appInfoInstance: AppInfo