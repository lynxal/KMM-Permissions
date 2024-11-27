package com.lynxal.kmmpermissions

internal class RequestedPermissionDetails (
    val permission: Permission,
    val callback: (Result<Unit>) -> Unit
)