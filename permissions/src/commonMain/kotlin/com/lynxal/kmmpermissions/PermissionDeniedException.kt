package com.lynxal.kmmpermissions

open class PermissionDeniedException(
    val permission: Permission,
    message: String? = null
) : Exception(message)