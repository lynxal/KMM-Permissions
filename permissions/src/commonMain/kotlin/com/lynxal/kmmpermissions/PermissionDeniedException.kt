package com.lynxal.kmmpermissions

open class PermissionDeniedException(
    permission: Permission,
    message: String? = null
) : PermissionRequestException(permission, message)