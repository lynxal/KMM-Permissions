package com.lynxal.kmmpermissions

class PermissionRequestCancelledException(
    val permission: Permission,
    message: String? = null
) : Exception(message)