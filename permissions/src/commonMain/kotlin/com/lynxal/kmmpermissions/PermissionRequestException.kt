package com.lynxal.kmmpermissions

open class PermissionRequestException(
    open val permission: Permission,
    message: String? = null
) : PermissionException(message)
