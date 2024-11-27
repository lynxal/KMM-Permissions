package com.lynxal.kmmpermissions

class PermissionRequestCancelledException(
    permission: Permission,
    message: String? = null
) : PermissionRequestException(permission, message)