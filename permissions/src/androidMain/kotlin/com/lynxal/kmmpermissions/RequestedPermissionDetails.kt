package com.lynxal.kmmpermissions

class RequestedPermissionDetails (
    val permission: Permission,
    val callback: (Result<Unit>) -> Unit
)