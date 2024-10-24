package com.lynxal.kmmpermissions

class PermissionDeniedPermanentlyException(
    permission: Permission,
    message: String? = null
) : PermissionDeniedException(permission, message)