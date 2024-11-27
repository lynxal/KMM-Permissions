package com.lynxal.kmmpermissions

class PermissionRequestUnsupportedException(
    permissions: Permission,
    message: String? = null
) : PermissionRequestException(permissions, message)